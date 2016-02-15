package com.ecop.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import config.Config;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import util.DB;
import util.XMLHandler;

public class DataTidy {
	protected static final SAXParserFactory factory = SAXParserFactory.newInstance();
	public static String parseData(String msg, String process_codein, JSONObject iJson){
		if ("PACKAGEMANAGEMENT".equals(process_codein)){
			JSONObject json = JSONObject.fromObject(xmlToJSON(msg));
			json.put("statistic", calcPackage(json));
			return json.toString();
		} else if("productorder".equals(process_codein)){
			String nMsg = msg.replace("<br />", "");
			nMsg = nMsg.replace("<p>", "");
			nMsg = nMsg.replace("</p>", "");
			insertSubscribeRecord(iJson, nMsg);
			return xmlToJSON(nMsg);
		}
		
		return xmlToJSON(msg);
	}
	
	/**
	 * 首次订购：type＝0在用，need_sync＝1需要，plan_sync_date=tomorrow，charge_date＝tomorrow
	 * @param iJson
	 * @param msg
	 */
	private static void insertSubscribeRecord(JSONObject iJson, String msg){
		JSONObject json = JSONObject.fromObject(xmlToJSON(msg));
		if (json.containsKey("retcode") && json.getString("retcode").equals("0")){
			String phone = iJson.getString("userinfo.servernum");
			String newPackage = json.getString("productinfo.productid");
			try {
				if (iJson.getString("productinfo.ordertype").equals("1")){
					//订购
					String[] params = {phone};
					String sql = "select * from usersubscriberecord where phone=?";
					List<Map<String, String>> has = DB.queryRows(sql, params);
					if (has == null){
						//新订购
						Date _date = new Date();
						_date.setDate(_date.getDate() + 1);
						int date = _date.getDate();
//						_date.setMonth(_date.getMonth() + 1);
						sql = "insert into usersubscriberecord(phone,package_code,need_sync,charge_date) values(?,?,?,?)";
						String[] paramsN = {phone, newPackage, "1", "" + date};
						DB.execute(sql, paramsN);
						//TODO 直接同步到aac
						syncToAac(phone, newPackage, _date);
					}else if(has.size() == 1){ //修改套餐
						//修改，定时同步到aac
						sql = "update usersubscriberecord set need_sync=?,package_code";
					}else{ //二次修改套餐
						
					}
				}else{
					//退订
					
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static void syncToAac(String phone, String newPackage, Date _date) {
		// TODO Auto-generated method stub
		
	}

	public static String xmlToJSON(String msg) {
		try {
			SAXParser saxParser = factory.newSAXParser();
			XMLHandler handl = new XMLHandler();
			InputStream is = new ByteArrayInputStream(msg.getBytes(Config.getValue("charset")));

			saxParser.parse(is, handl);
			is.close();
			return handl.toJson();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String calcPackage(JSONObject json){
//		JSONObject result = new JSONObject();
//		result.put("msgheader", json.getJSONObject("msgheader"));
		JSONObject result = json;
		if (!json.containsKey("msgbody"))
			return json.toString();
		JSONObject msgbody = json.getJSONObject("msgbody");
		
		if (!msgbody.containsKey("databeanlist"))
			return json.toString();
		
		JSONObject databeanlist = msgbody.getJSONObject("databeanlist");
		if (!databeanlist.containsKey("databean"))
			return json.toString();
		
		JSONArray databean = databeanlist.getJSONArray("databean");
		String[] lists = Config.getValue("locationPackageName").split(",");
		List<String> packageList = Arrays.asList(lists);
		
		float currContain = 0l;
		float currUsed = 0l;
		float nextContain = 0l;
		float nextUsed = 0l;
		for (int i = 0; i < databean.size(); i++){
			JSONObject onedata = databean.getJSONObject(i);
			String currname = onedata.getString("currname");
			if (!"".equals(currname) && packageList.contains(currname)){
				currContain += parseFlow(onedata.getString("contain"), onedata.getString("unit"));
				currUsed += parseFlow(onedata.getString("used"), onedata.getString("unit"));
			}
			
			String nextname = onedata.getString("nextname");
			if (!"".equals(nextname) && packageList.contains(nextname)){
				nextContain += parseFlow(onedata.getString("contain"), onedata.getString("unit"));
				nextUsed += parseFlow(onedata.getString("used"), onedata.getString("unit"));
			}
		}
		JSONObject msgbodyRet = new JSONObject();
		msgbodyRet.put("currcontain", currContain);
		msgbodyRet.put("currused", currUsed);
		msgbodyRet.put("nextcontain", nextContain);
		msgbodyRet.put("nextused", nextUsed);
		msgbodyRet.put("rescode", "success");
		msgbodyRet.put("resmsg", "");
		
//		result.put("statistic", msgbodyRet);
		return msgbodyRet.toString();//result.toString();
	}
	private static float parseFlow(String num, String unit){
		float result = Float.parseFloat(num);
		List<String> units = Arrays.asList("M", "G", "T");//"B", "K", "M", "G", "T"
		if (units.contains(unit)){
			for(int i = 0; i < units.size(); i++){
				if (unit.equals(units.get(i)))
					break;
				result *= 1024;
			}
		}
		return result;
	}
	public static void main(String args[]){
//		DB.execute("insert into usersubscriberecord(phone,charge_date,need_sync) values('138222922','2',1)");
//		DB.execute("update usersubscriberecord set phone='138' where phone = '138222922'");
//		DB.execute("update usersubscriberecord set create_time='2016-01-25 11:11:21', update_time='2016-01-25 11:11:16' where phone = '138'");
		Date _date = new Date();
		int date = _date.getDate();
		_date.setMonth(0);
		System.out.println(date);
		System.out.println(_date);
	}
}
