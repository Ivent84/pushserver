package servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ecop.util.ClientBaseServlet;
import com.ecop.util.DataTidy;
import com.ecop.util.UCUtils;
import com.redis.RedisDB;

import config.Config;
import net.sf.json.JSONObject;
import redis.clients.jedis.JedisCommands;
import util.DB;
import util.HTTPUtils;

/**
 * Servlet implementation class Setting
 */
@WebServlet("/setting")
public class Setting extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Setting() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setCharacterEncoding(Config.getValue("charset"));
		PrintWriter writer = response.getWriter();
		BufferedReader reader = request.getReader();
		String req = reader.readLine();
		JSONObject jo = JSONObject.fromObject(req);
		if(!jo.containsKey("cmd")){
			writer.append("{\"rescode\":\"fail\",\"resmsg\":\"cmd is need\"}");
			return;
		}
		switch(jo.getString("cmd")){
		case "getserverconfig":
			getServerConfig(jo, writer);
			break;
		case "getsystemconfig":
			getSystemConfig(jo, writer);
			break;
		case "getecop":
			getECOP(jo, writer);
			break;
		case "getecopflow":
			getEcopFlow(jo, writer);
			break;
		case "androidversionloctest":
			androidVersionLocTest(jo, writer);
			break;
		case "androidversionloc":
			androidVersionLoc(jo, writer);
			break;
		default:
			break;
		}
	}
	private void getServerConfig(JSONObject json, PrintWriter writer) {
		// TODO Auto-generated method stub
		if (!json.containsKey("code")){
			writer.append("{\"rescode\":\"fail\",\"resmsg\":\"code is need\"}");
			return;
		}
		String[] params = {json.getString("code")};
		String sql = "select * from config where code=?";
		Map<String, String> dbRes = DB.queryRow(sql, params);
		writer.append("{\"rescode\":\"success\",\"resmsg\":\"success\",\"title\":\""+dbRes.get("title")+"\","
				+ "\"value\":\""+dbRes.get("value")+"\","
				+ "\"status\":\""+dbRes.get("status")+"\"}");
	}

	private void getEcopFlow(JSONObject json, PrintWriter writer) {
		json.remove("cmd");
		String process_codein = "PACKAGEMANAGEMENT";
		json.put("serviceinfo.id", process_codein);
		json.put("process_code", process_codein);
		if (json.containsKey("mobile")){
			json.put("userinfo.servernum", json.getString("mobile"));
			json.remove("mobile");
		}
		Map<String, String> map = UCUtils.getRequestParam(json, process_codein);
		if (null == map){
			writer.append("{\"rescode\":\"fail\",\"resmsg\":\"parameter error\"}");
			return;
		}
		String operationRequest = json.getString("request_code");
		String channelid = json.getString("channelid");
		String unitid = json.getString("unitid");
		String  menuId = json.getString("menuid").isEmpty()?"0":json.getString("menuid");
		String  operatorId = json.getString("operatorid").isEmpty()?"0":json.getString("operatorid");
		String process_code = json.getString("process_code");
		String authReq = "";
		String clientIp = "";
		String verifycode = "";
		ClientBaseServlet cbs = new ClientBaseServlet();
		String xml = cbs.createStringXML(operationRequest, menuId,
				process_code, authReq,null, clientIp, operatorId, channelid,
				unitid, verifycode, map);
		String url = Config.getValue("ecopServiceUrl");
		String results = HTTPUtils.httpsDoPostXML(url , xml, "UTF-8", "UTF-8");
		if (null != results){
			JSONObject jsonResults = JSONObject.fromObject(DataTidy.xmlToJSON(results));
			writer.append(DataTidy.calcPackage(jsonResults));
		}else{
			writer.append("{\"rescode\":\"fail\",\"resmsg\":\"ecop interface error\"}");
		}
	}
	private void getECOP(JSONObject jo, PrintWriter writer) {
    	
		String mobile = "";
		if (jo.containsKey("mobile"))
			mobile= jo.getString("mobile");
		
		String type = "";
		if (jo.containsKey("type"))
			type = jo.getString("type");
		
    	String flow = "1.5";
    	String totalflow = "2";
    	
    	if(type.equals("2")){
    		totalflow = "2";
    		flow="1.5";
    	}else{
    		totalflow = "5";
    		flow="3.75";
    	}
    	JSONObject resultJson = new JSONObject();
    	resultJson.put("code", "0");
		resultJson.put("message", "查询成功!");
		resultJson.put("flow", flow);
		resultJson.put("totalflow", totalflow);
    	
		writer.append(resultJson.toString());
    }
    
    private void getSystemConfig(JSONObject jo, PrintWriter writer) {
    	
    	JSONObject resultJson = new JSONObject();
    	
    	resultJson.put("code", "0");
		resultJson.put("message", "查询成功!");
		resultJson.put("cmccopen", "1");//0:开  1:关
		resultJson.put("cmccopen1", "0");//0:开  1:关
		resultJson.put("cmccopen2", "0");//0:开  1:关
		resultJson.put("cmccopen3", "0");//0:开  1:关

		writer.append(resultJson.toString());
    }
    private void androidVersionLocTest(JSONObject jo, PrintWriter writer) {
        
//        JSONObject resultJson = new JSONObject();
//		
//    	String version = "2.3.1";
//    	boolean forced =  false;
//    	String url = "http://www.pgyer.com/locpatest";
//    	
//    	resultJson.put("code", "0");
//		resultJson.put("message", "查询成功!");
//		resultJson.put("version", version);
//		resultJson.put("forced", forced);
//		resultJson.put("url", url);
		
		String key = "androidVersionLocTest";
		String value = RedisDB.get(key);
		if (null == value){
			String[] params = {key};
			Map<String, String> row = DB.queryRow("select * from config where code=?", params);
			value = row.get("value");
			
			JedisCommands conn = RedisDB.getConn();
			conn.set(key, value);
			conn.expire(key, 60 * 60);
			RedisDB.releaseConn(conn);
		}
    	
		writer.append(value);
    }
    
    private void androidVersionLoc(JSONObject jo, PrintWriter writer) {
        
//    	JSONObject resultJson = new JSONObject();
//		
//    	String version = "2.2.9";
//    	boolean forced =  false;
//    	String url = "http://www.pgyer.com/locpa";
//    	
//    	resultJson.put("code", "0");
//		resultJson.put("message", "查询成功!");
//		resultJson.put("version", version);
//		resultJson.put("forced", forced);
//		resultJson.put("url", url);
    	
		String key = "androidVersionLoc";
		String value = RedisDB.get(key);
		if (null == value){
			String[] params = {key};
			Map<String, String> row = DB.queryRow("select * from config where code=?", params);
			value = row.get("value");
			
			JedisCommands conn = RedisDB.getConn();
			conn.set(key, value);
			conn.expire(key, 60 * 60);
			RedisDB.releaseConn(conn);
		}
    	
		writer.append(value);
    }
}
