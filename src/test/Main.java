package test;

import java.util.Map;
import java.util.Random;

import org.apache.axis.encoding.Base64;

import util.DB;
import util.ServiceUtils;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String params[] = {"messagesendlimitperday"};
		Map<String, String> dbres = DB.queryRow("select * from config where code=?", params);
		if (null == dbres){
			int a = 2;
		}else{
			int m = Integer.parseInt(dbres.get("value"));
		}
		String key = "B977C347";
		String json = "{\"productinfo.productgroup\":\"0\",\"operatorid\":\"0\",\"productinfo.productname\":\"ZJXCLLB20\",\"productinfo.ordertype\":\"1\",\"productinfo.productid\":\"ZJXCLLB20\",\"userinfo.area\":\"200\",\"option\":\"1\",\"token\":\"1\",\"menuid\":\"1\",\"userinfo.servernum\":\"13902220524\",\"process_code\":\"productorder\",\"productinfo.producttype\":\"0\"}";
		String[] result = ServiceUtils.generateSignOAuth(key, json);
		System.out.println(result[1]);
		
		
		String resultBase64 = "cBU05leUp0YjJKcGJHVWlPaUlpTENKamJHbGxiblJwY0NJNklpSXNJbTVsZEhSNWNHVWlPaUlpZlE9PQ";
		resultBase64 = new String(Base64.decode(resultBase64.substring(2)));
		resultBase64 = new String(Base64.decode(resultBase64.substring(2)));
		System.out.println(resultBase64);
		String xml = ""
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:pcc=\"http://PCC.webservice.zte.com\">"
					+ "<soapenv:Header/>"
					+ "<soapenv:Body>"
						+ "<pcc:QueryLocation>"
							+ "<pcc:queryLocationRequest>"
								+ "<pcc:Sequence_No>" + "112233" + new Random().nextInt(999) + "</pcc:Sequence_No>"
								+ "<pcc:Operation_Type>" + 2 + "</pcc:Operation_Type>"
								+ "<pcc:APP_ID>" + "1000000107" + "</pcc:APP_ID>"
								+ "<pcc:MSISDN>" + "13902220524" + "</pcc:MSISDN>"
							+ "</pcc:queryLocationRequest>"
						+ "</pcc:QueryLocation>"
					+ "</soapenv:Body>"
				+ "</soapenv:Envelope>";
		System.out.println(xml);
	}

}
