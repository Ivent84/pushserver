package com.ecop.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.xml.sax.SAXException;

import config.Config;
import net.sf.json.JSONObject;
import util.DateUtil;
import util.HTTPUtils;
import util.XMLHandler;

@SuppressWarnings("serial")
public class ClientBaseServlet extends BaseServlet {
	
	private static final Log log = LogFactory.getLog(ClientBaseServlet.class);
	protected static final SAXParserFactory factory = SAXParserFactory.newInstance();
	// token相关的参数
	protected static int[] header_params = { 4, 13, 22, 31 };
	protected static int[] phone_params = { 2, 6, 10, 14 };
	protected static String url = Config.getValue("ecopServiceUrl");
//	protected static String url = "https://221.179.11.204:9081/eaop/EaopServerPort";
	protected static String reqModel = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body><ns2:handle xmlns:ns2=\"http://eaop.gd.chinamobile.com/\"><arg0><![CDATA[ToRepleaceString]]></arg0></ns2:handle></S:Body></S:Envelope>";
	protected static Map<Object,String> errorCode;
	protected static final String authReq = "";
	protected static final String clientIp = "";
	protected static final String verifycode = "";
	protected static final String channelid = "album"; 
	protected static final String unitid = "0";
	protected static final String  menuId = "0";
	protected static final String  operatorId = "0";
	/**
	 * 
	 * @param operationRequest   请求头结点
	 * @param menuId  菜单id
	 * @param processCode  业务代码
	 * @param authReq  短信认证标示
	 * @param mac  mac计算值
	 * @param operatorId  操作员 没有定义时填0
	 * @param clientIp  客户端ip
	 * @param channelid 渠道ID
	 * @param unitid 子渠道id 没有定义时填0
	 * @param verifycode  效验码
	 * @param param  xml报文体
	 * @return
	 */
	public String createStringXML(String operationRequest, String menuId,
			String processCode, String authReq,String mac,String clientIp, String operatorId,
			String channelid, String unitid, String verifycode,Map<String,String> param) {
		if (StringUtils.isNotBlank(operationRequest)
				&& StringUtils.isNotBlank(menuId)
				&& StringUtils.isNotBlank(operatorId)
				&& StringUtils.isNotBlank(channelid)
				&& StringUtils.isNotBlank(unitid)
				&& StringUtils.isNotBlank(processCode)
				&& param!=null
				) {
			
			
			  Document document = DocumentHelper.createDocument();
			  document.setXMLEncoding("UTF-8");
			  Element operation_Request = document.addElement(operationRequest);//添加根节点   
			  Element header=  operation_Request.addElement("msgheader");//添加<msgheader>结点
			  header.addElement("menu_id").setText(menuId);//添加menu_id
			  header.addElement("process_code").setText(processCode);//添加process_code
			  header.addElement("req_time").setText(DateUtil.date2String(new Date(), "yyyyMMddHHmmss"));//添加req_time
			  header.addElement("req_seq").setText(UUID.randomUUID().toString().replace("-", ""));//添加req_seq请求序列
			  if(StringUtils.isNotBlank(authReq))
				  header.addElement("auth_req").setText(authReq); //短信认证标识
			  if(StringUtils.isNotBlank(mac))
				  header.addElement("mac").setText(mac); //短信认证标识
			  
			  
			  header.addElement("timestamp").setText(DateUtil.date2String(new Date(), "yyyyMMddHHmmss"));//有效时间戳
			  if(StringUtils.isNotBlank(clientIp))
			  header.addElement("client_ip").setText(clientIp);//添加client_ip,客户端ip
			  
			  Element channelinfo = header.addElement("channelinfo");//渠道信息
			  
			  channelinfo.addElement("operatorid").setText(operatorId);//操作员
			  channelinfo.addElement("channelid").setText(channelid);//渠道id
			  channelinfo.addElement("unitid").setText(unitid);//子渠道id
			  if(StringUtils.isNotBlank(verifycode))
			      channelinfo.addElement("verifycode").setText(verifycode);//效验码
			  Element msgbody =operation_Request.addElement("msgbody");
			   for(String key :param.keySet()){
				   createNode(msgbody , key , param.get(key));
//				   key.split(".");
//				   msgbody.addElement(key).setText(param.get(key));
			   }
			return  reqModel.replace("ToRepleaceString",document.asXML().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ""));
			  

		}

		return "";
	}
	
	private void createNode(Element root , String path ,String value){
		String[] paths = path.split("[.]", 2);
		if(paths.length == 2){
			if(root.elements(paths[0]).size() == 0){
				root.addElement(paths[0]);
			}
			createNode((Element)root.elements(paths[0]).get(0), paths[1], value);
		}
		else{
			root.addElement(path).setText(value);
		}
	}
	
	public String getkeyPath(String name){
		
		return getServletContext().getRealPath("/WEB-INF/key/"+name);
	}
	protected static boolean isNumber(String str){
		return str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
	}

	static{
		url = Config.getValue("ecopServiceUrl");
		log.info("request url :"+url);
		errorCode =new HashMap<Object, String>();
		//初始化https
		String capath= getPath(ClientBaseServlet.class, "").replace("classes/", "key/ca.p12");
		HTTPUtils.initSSL(capath);
		
		Properties properties = new Properties();
//		String filepath = getServletContext().getRealPath("/WEB-INF/classes/dhtErrorCode.properties");
		String filepath= getPath(ClientBaseServlet.class, "dhtErrorCode.properties");
		
		
		File file = new File(filepath);
		if (!file.exists()) {
			log.info("Properties Service File Not Exist.");
		}
		InputStreamReader input =null;
		try {
			 input = new InputStreamReader(new FileInputStream(file), "UTF-8");
			 properties.load(input);
			 for(Object key : properties.keySet()){
                   errorCode.put(key.toString().toLowerCase(), properties.get(key).toString());
			 }
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static String getPath(Class cls,String path) {
		path = path == null ? "" : path;
		return cls.getClassLoader().getResource(path).getPath();
	}
	/**
	 * 家庭网返回json数据解析
	 * @param msg
	 * @return
	 */
 protected String getHomeResponseJSON(String msg){
		  try {
			SAXParser saxParser = factory.newSAXParser();
		    XMLHandler handl = new XMLHandler();
			InputStream is = new  ByteArrayInputStream(msg.getBytes());
			
				saxParser.parse(is, handl);
				is.close();
				JSONObject json = JSONObject.fromObject(handl.toJson());
				Object ob = json.getJSONObject("msgheader").get("retinfo");
				json.remove("msgheader");
				json.put("retinfo", ob);
				 return json.toString();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch(ParserConfigurationException e){
				e.printStackTrace();
			}
		return null;
			
	  }

}
