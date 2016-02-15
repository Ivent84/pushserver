package service;

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.qixin.api.Api;
import com.redis.RedisDB;

import config.Config;
import net.sf.json.JSONObject;
import redis.clients.jedis.JedisCommands;
import util.DB;

public class Notice {
	private static Object sendMsgLock = new Object();
	private static final Log log = LogFactory.getLog(Notice.class);
	public int notifyUser(String phone, String content) {
		String[] params = { phone };
		Map<String, String> data = DB.queryRow("select * from phoneostype where phone=?", params);
		if (data == null) {
			System.out.println(("{rescode:\"fail\",resmsg:\"wrong phone number\"}"));
			// notifyMsg(phone, content);
		} else {
			if ("0".equals(data.get("type"))) { // android
				notifyAndrUser(phone, content);
			} else { // ios
				notifyIosUser(phone, content, data.get("devicetoken"));
			}
		}
		return 0;
	}

	public void notifyMsg(String phone, String content) {
		// TODO Auto-generated method stub
		// 下发限制
		JedisCommands conn = RedisDB.getConn();
		try{
			String hKey = "hadsend" + phone;
			synchronized (sendMsgLock) {
				long currTime = System.currentTimeMillis();
				Set<String> keys = conn.hkeys(hKey);
				int timesPerHour = 0;
				int timesPerDay = 0;
				for(String key: keys){
					long sTime = Long.parseLong(key);
					if(sTime > currTime - 60 * 60 * 1000){//一小时以内
						timesPerHour += 1;
						timesPerDay += 1;
					}else if(sTime > currTime - 24 * 60 * 60 * 1000){//一天以内
						timesPerDay += 1;
					}else{
						conn.hdel(hKey, key);//超过一天，删除
					}
				}
//			例外
				if ("13922409554".equals(phone) || "17820710203".equals(phone)){
					timesPerDay = timesPerHour = 0;
				}else{
					if(conn.get("tensechadsend-" + phone) != null){//10秒内下发过
						return;
					}
				}
				if (timesPerDay > 0 || timesPerHour > 0){
					int[] limit = getSendLimit();
					if (timesPerDay >= limit[0])//超出每天的限制
						return;
					if (timesPerHour >= limit[1])//超出每小时的限制
						return;
				}
				conn.set("tensechadsend-" + phone,"1");//10秒内下发过
				conn.expire("tensechadsend-" + phone, 10);
				conn.hset(hKey, ""+currTime, "1");//记录下发
				log.info("send notice message：" + phone + ",time:" + currTime);
			}
			//下发
			JSONObject jo = new JSONObject();
			jo.put("option", "0");
			jo.put("mobile", phone);
			jo.put("business", "2");//模板有2，3，4
			Api.getContentFromServ("OAuth/sendValcode", jo);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			RedisDB.releaseConn(conn);//需释放连接
		}
	}

	public void notifyIosUser(String phone, String content) {
		String[] params = { phone };
		Map<String, String> data = DB.queryRow("select * from phoneostype where phone=?", params);
		if(data == null){
			System.out.println("mobile has not infomation: " + phone);
			return;
		}
		notifyIosUser(phone, content, data.get("devicetoken"));
	}
	public void notifyIosUser(String phone, String content, String devicetoken) {
		if ("1".equals(Config.getValue("useMessageNotice"))) {
			notifyMsg(phone, content);
		}
		if ("1".equals(Config.getValue("usePushNotice"))) {
			IosPublisher publisher = new IosPublisher();
			String deviceToken = "".equals(devicetoken) ? phone : devicetoken;
			String[] pushContent = { deviceToken, content };
			publisher.publish(pushContent);
		}
	}

	public void notifyAndrUser(String phone, String content) {
		if ("1".equals(Config.getValue("useMessageNotice"))) {
			notifyMsg(phone, content);
		}
		if ("1".equals(Config.getValue("usePushNotice"))) {
			String[] pushContent = { "notify:" + phone, content };
			Publisher.publish(pushContent);
		}
	}
	
	public int[] getSendLimit(){
		int[] limits = new int[2];
		JedisCommands conn = RedisDB.getConn();
		if (null == conn.get("messagesendlimitperday")){
			String params[] = {"messagesendlimitperday"};
			Map<String, String> dbres = DB.queryRow("select * from config where code=?", params);
			if (null == dbres){
				limits[0] = 10;
			}else{
				limits[0] = Integer.parseInt(dbres.get("value"));
			}
			params[0] = "messagesendlimitperhour";
			dbres = DB.queryRow("select * from config where code=?", params);
			if (null == dbres){
				limits[1] = 2;
			}else{
				limits[1] = Integer.parseInt(dbres.get("value"));
			}
			conn.set("messagesendlimitperday", ""+limits[0]);
			conn.expire("messagesendlimitperday", 3600); //没小时更新配置
			conn.set("messagesendlimitperhour", ""+limits[1]);
		}else{
			limits[0] = Integer.parseInt(conn.get("messagesendlimitperday"));
			limits[1] = Integer.parseInt(conn.get("messagesendlimitperhour"));
		}
		
		RedisDB.releaseConn(conn);
		return limits;
	}
}
