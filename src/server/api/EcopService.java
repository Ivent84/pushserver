package server.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ecop.util.ClientBaseServlet;
import com.ecop.util.DataTidy;
//import com.isoftstone.config.Global;
//import com.isoftstone.servlet.UCUtils;
//import com.isoftstone.utils.HTTPUtils;
//import com.isoftstone.utils.ServiceUtils;
//import com.isoftstone.utils.XMLHandler;
//import com.isoftstone.servlet.UnifiedCommunication;
import com.ecop.util.UCUtils;

import config.Global;
import net.sf.json.JSONObject;
import util.HTTPUtils;
import util.ServiceUtils;

/**
 * Servlet implementation class EcopService
 */
@WebServlet("/ecopservice")
public class EcopService extends ClientBaseServlet {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(EcopService.class);
	// private static final String url = "";

	private String operationRequest = "Picksbrthxnumreq";
//	private static final String menuId = "0";
	private static final String authReq = "";
	private static final String clientIp = "";
//	private static final String operatorId = "0";
	private static final String verifycode = "";
    /**
     * Default constructor. 
     */
    public EcopService() {
        // TODO Auto-generated constructor stub
    }
    public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		JSONObject rejson = new JSONObject();
		String results = "";
		try {
			request.setCharacterEncoding("UTF-8");
			StringBuffer strbuf = new StringBuffer();
			char[] charbuff = new char[512];
			int i;
			BufferedReader br = request.getReader();
			while ((i = br.read(charbuff)) != -1) {
				strbuf.append(charbuff, 0, i);
			}

			if (StringUtils.isBlank(strbuf.toString())) {
				results = "202";// json为空
				results = Global.RESPONSE_ERROR_10002;//参数错误
				return;
			}
			String requestJson = strbuf.toString();
			log.info("request json:"+requestJson);
			// 获取签名
			 String sign = request.getHeader(Global.HEAD_sign);
			if (sign == null) {
				log.debug("[Authentication Servlet Error] : (sign == null)");
				results = Global.RESPONSE_ERROR_10001; // 报文解析错误
				return;
	        }
		
//			String tokenVal = TokenEncryptUtils.sxyEncryMD5(requestJson);
//			if(!tokenVal.equals(token)){
//				rejson.put("recode", "301");
//				return ;
//			}
			JSONObject json = JSONObject.fromObject(requestJson);
			String process_codein = json.getString("process_code");
			
			Map<String,String> map=UCUtils.getRequestParam(json, process_codein);
			if(map ==null){
				results = Global.RESPONSE_ERROR_10002;//参数错误
				return ;
			}
			String token = json.getString("token");
			String option =json.getString("option");
			if(StringUtils.isBlank(token)){
				results = Global.RESPONSE_ERROR_10002;//参数错误
				return;
			}
			if(StringUtils.isBlank(option)){
				results = Global.RESPONSE_ERROR_10002;//参数错误
				return;
			}
			
			// 验证签名
			String[] signs = ServiceUtils.decryptSignOAuth(sign);
			String keys = signs[0];
			if (!sign.equals(ServiceUtils.generateSignOAuth(keys, requestJson)[1])) {
				log.debug("[Authentication Servlet Error] : (!sign.equals(ServiceUtils.generateSignOAuth(keys, requests)[1]))");
				results = Global.RESPONSE_ERROR_10003; // 签名验证失败
				return;
			}
			operationRequest = json.getString("request_code");
			String channelid = json.getString("channelid");
			String unitid = json.getString("unitid");
			String  menuId = json.getString("menuid").isEmpty()?"0":json.getString("menuid");
			String  operatorId = json.getString("operatorid").isEmpty()?"0":json.getString("operatorid");
			String process_code = json.getString("process_code");
			
			String xml = createStringXML(operationRequest, menuId,
					process_code, authReq,null, clientIp, operatorId, channelid,
					unitid, verifycode, map);
			if("querybaseinfo".equals(process_codein)){//5.15	ECOP_SERVICE_0015手机信息查询接口
				xml=xml.replaceFirst("<querybaseinforeq>", "<querybaseinforeq xmlns=\"http://www.gmcc.net/ngcrm/\">");
			}
			log.info("request xml:"+xml);
			results = HTTPUtils.httpsDoPostXML(url, xml, "UTF-8", "UTF-8");
			results = StringEscapeUtils.unescapeXml(results);
			log.info("response xml: " + results);
			if(StringUtils.isBlank(results)){
				log.info("reuqest error");
				results = Global.RESPONSE_ERROR_10005;//内部错误
			}else{
//					results =new XMLSerializer().read(results).toString();
//					rejson.put("recode", "0");
//					rejson.put("msg", results);
//					results=rejson.toString();
					results = DataTidy.parseData(results, process_codein, json);
					if(StringUtils.isBlank(results)){
						results = Global.RESPONSE_ERROR_10007;//ECOP返回的数据格式错误
					}
			}
		} catch (Exception e) {
			results = Global.RESPONSE_ERROR_10005;//内部错误
			log.error("Exception:" + e.fillInStackTrace());
		} finally {
			log.info("to client :"+results);
			servletWrite(response, results);
		}
	}
}
