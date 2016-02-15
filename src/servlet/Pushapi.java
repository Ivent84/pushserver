package servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import config.Config;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import service.IosPublisher;
import service.Publisher;
import util.DB;

/**
 * Servlet implementation class Pushapi
 */
@WebServlet("/pushapi")
public class Pushapi extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Pushapi() {
        super();
    }

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding(Config.getValue("charset"));
		response.setCharacterEncoding(Config.getValue("charset"));
		BufferedReader reader = request.getReader();
		String req = reader.readLine();
		JSONObject jo = JSONObject.fromObject(req);
		
		String operattion = "";
		if(jo.has("op"))
			operattion = jo.getString("op");
		switch (operattion) {
		case "report":
			//记录手机对应的类型，0:android;1:ios
			report(jo, request, response);
			break;
		case "notify":
			notify(jo, response);
			break;
		case "addfeedback":
			addFeedback(jo, response);
			break;
		case "queryfeedback":
			queryFeedback(jo, response);
			break;
		case "isinlist":
			isInList(jo, response);
			break;
		case "execute":
			exceute(jo, response);
			break;
		default:
			response.getWriter().append("{\"rescode\":\"fail\",\"resmsg\":\"op is wrong\"}");
			break;
		}
		
	}
	private void exceute(JSONObject jo, HttpServletResponse response) throws IOException {
		PrintWriter writer = response.getWriter();
		boolean res = DB.execute(jo.getString("sql"));
		if (res){
			writer.append("{\"rescode\":\"success\",\"resmsg\":\"operate success\"}");
		}else{
			writer.append("{\"rescode\":\"fail\",\"resmsg\":\"operate fail\"}");
		}
	}

	private void isInList(JSONObject jo, HttpServletResponse response) {
		try {
			PrintWriter writer = response.getWriter();
			if(!jo.containsKey("phone")){
				writer.write("{\"rescode\":\"fail\",\"resmsg\":\"phone is need\"}");
				return;
			}
			
			String sql = "select * from allowuserlist where phone=?";
			String[] params = {jo.getString("phone")};
			Map<String, String> data = DB.queryRow(sql, params);
			String res = "";
			if (null == data){
				res = "{\"rescode\":\"fail\",\"resmsg\":\"phone is deny\"}";
			}else{
				res = "{\"rescode\":\"success\",\"resmsg\":\"phone is allow\"}";
			}
			writer.append(res);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void queryFeedback(JSONObject jo, HttpServletResponse response) {
		try {
			PrintWriter writer = response.getWriter();
			if(!jo.containsKey("phone")){
				writer.write("{\"rescode\":\"fail\",\"resmsg\":\"phone is need\"}");
				return;
			}
			
			String sql = "select * from feedback where phone=?";
			String[] params = {jo.getString("phone")};
			List<Map<String, String>> data = DB.queryRows(sql, params);
			JSONObject res = new JSONObject();
			res.put("rescode", "success");
			res.put("resmsg", "operate success");
			JSONArray dataja = JSONArray.fromObject(data);
			res.put("data", dataja);
			
			writer.append(res.toString());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void addFeedback(JSONObject jo, HttpServletResponse response) {
		try {
			PrintWriter writer = response.getWriter();
			if(!jo.containsKey("phone")){
				writer.write("{\"rescode\":\"fail\",\"resmsg\":\"phone is need\"}");
				return;
			}
			if(!jo.containsKey("content")){
				writer.write("{\"rescode\":\"fail\",\"resmsg\":\"content is need\"}");
				return;
			}
			String sql = "insert into feedback(phone,content) values(?,?)";
			String[] params = {jo.getString("phone"), jo.getString("content")};
			boolean res = DB.execute(sql, params);
			if (res){
				writer.append("{\"rescode\":\"success\",\"resmsg\":\"operate success\"}");
			}else{
				writer.append("{\"rescode\":\"fail\",\"resmsg\":\"operate fail\"}");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void report(JSONObject jo, HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException{
		PrintWriter writer = response.getWriter();
		
		@SuppressWarnings("rawtypes")
		Set keys = jo.keySet();
		if(!keys.contains("phone")){
			writer.write("{\"rescode\":\"fail\",\"resmsg\":\"phone is need\"}");
			return;
		}
		String phone = jo.getString("phone");
		
		String type = null;
		if(keys.contains("type")){
			type = jo.getString("type");
		}
		
		String deviceToken = "";
		if(keys.contains("devicetoken")){
			deviceToken = jo.getString("devicetoken");
			if(null == type)
				type = "1";
		}else{
			if(null == type)
				type = "0";
		}
		
		boolean result = true;
		String[] params = {type, deviceToken, phone};
		List<String> count = DB.getColumn("select count(*) as c from phoneostype where phone='" + phone + "'");
		if(null == count || Integer.parseInt(count.get(0)) == 0){
			result = DB.execute("insert into phoneostype(type,devicetoken,phone) values(?,?,?)", params);
		}else{
			result = DB.execute("update phoneostype set type=?,devicetoken=? where phone=?", params);
		}
		if(result){
			writer.write("{\"rescode\":\"success\",\"resmsg\":\"operate success\"}");
		}else{
			writer.write("{\"rescode\":\"fail\",\"resmsg\":\"insert data fail\"}");
		}
		
	}
	private void notify(JSONObject jo, HttpServletResponse response) throws ServletException, IOException{
		boolean result = true;
		PrintWriter writer = response.getWriter();
		
		String phone = jo.getString("phone");
		String content = jo.getString("content");
		
		String[] params = {phone};
		Map<String, String> data = DB.queryRow("select * from phoneostype where phone=?", params);
		if(data == null){
			writer.write("{\"rescode\":\"fail\",\"resmsg\":\"wrong phone number\"}");
			return;
		}
		if("0".equals(data.get("type"))){ //android
			String[] pushContent = {"notify:" + phone, content};
			Publisher.publish(pushContent);
		}else{ //ios
			IosPublisher publisher = new IosPublisher();
			String deviceToken = "".equals(data.get("devicetoken"))?data.get("phone"):data.get("devicetoken");
			String[] pushContent = {deviceToken, content};
			publisher.publish(pushContent);
		}
		
		if(result){
			writer.write("{\"rescode\":\"success\",\"resmsg\":\"operate success\"}");
		}else{
			writer.write("{\"rescode\":\"fail\",\"resmsg\":\"server error\"}");
		}
	}
}
