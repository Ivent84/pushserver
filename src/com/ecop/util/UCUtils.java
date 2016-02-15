package com.ecop.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import config.Config;

public class UCUtils {
	private static final Log log = LogFactory.getLog(UCUtils.class);
	private static JSONObject hasDefaultValueEcopParams = JSONObject.fromObject(Config.getValue("hasDefaultValueEcopParams"));
	public static Map<String, String> getRequestParam(JSONObject json ,String process_code) {
		if(StringUtils.isBlank(process_code)){
			return null;
		}
		//请求跟节点参数
		String request_code="";
		//公共头部参数
		StringBuffer headmust = new StringBuffer();
		StringBuffer bodymust = new StringBuffer();
		
		headmust.append("process_code,channelid,unitid,menuid,operatorid");
         //ECOP_SERVICE_0008产品查询
		if("productquery".equals(process_code)){
				bodymust.append("userinfo.servernum,")
				         .append("userinfo.area,")
				         .append("userinfo.brand,")
				         .append("productinfo.productid,")
				         .append("productinfo.productgroup,")
				         .append("productinfo.producttype,")
				         .append("productinfo.productname");
				request_code="productqueryreq";
		//ECOP_SERVICE_0009产品受理
		}else if("productorder".equals(process_code)){
				bodymust.append("userinfo.servernum,")
				         .append("userinfo.area,")
//				         .append("userinfo.brand,")
				         .append("productinfo.productid,")
				         .append("productinfo.productgroup,")
				         .append("productinfo.producttype,")
				         .append("productinfo.productname,")
						 .append("productinfo.ordertype");
				request_code="productorderreq";
		   //ECOP_SERVICE_0012我的套餐查询
		} else if("PACKAGEMANAGEMENT".equals(process_code)){
						bodymust.append("userinfo.servernum,")
				         .append("serviceinfo.id");
						request_code="queryreq";
						 process_code ="query";
		  //ECOP_SERVICE_0013GPRS流量查询
		}else if("GPRS_FLOW".equals(process_code)){
			bodymust.append("userinfo.servernum,")
			        .append("serviceinfo.id");
			request_code="queryreq";
			 process_code ="query";
			//ECOP_SERVICE_0014余额查询接口
		}else if("ACCOUNTS_BALANCE_SEARCH".equals(process_code)){
			bodymust.append("userinfo.servernum,")
	        		.append("serviceinfo.id");
			request_code="queryreq";
			 process_code ="query";
			//ECOP_SERVICE_0015手机信息查询接口
		}else if("querybaseinfo".equals(process_code)){
					bodymust.append("userinfo.servernum");
					request_code="querybaseinforeq";
					 process_code ="query";
			//ECOP_SERVICE_0016查询用户账户信息接口
		}else if("queryuseraccount".equals(process_code)){
					bodymust.append("userinfo.servernum");
					request_code="queryuseraccountreq";
					 process_code ="query";
		//ECOP_SERVICE_0019积分/M值查询-总积分/总M值查询接口
		}else if("INTE_GRAL_CURR_YEAR".equals(process_code)||"MZONE_MVALUE_BALANCE".equals(process_code)){
					bodymust.append("userinfo.servernum,")
							.append("serviceinfo.id");
					request_code="queryreq";
					 process_code ="query";
		//ECOP_SERVICE_0033套餐订购状态查询
		}else if("QueryMyFuction".equals(process_code)){
					bodymust.append("servnumber,")
					        .append("querytype,")
					        .append("type");
					request_code="queryMyFuctionreq";
					 process_code ="query";
		  //ECOP_SERVICE_0035 剩余查询接口（360）
		}else if("LeftResourceQuery".equals(process_code)){
					bodymust.append("userinfo.imsiNumber,")
					.append("serviceinfo.type");
					request_code="LeftResourceQueryreq";
		//	ECOP_SERVICE_0036 账户余额查询（360）
		}else if("AccountsBalanceQuery".equals(process_code)){
					bodymust.append("userinfo.imsiNumber");
					request_code="AccountsBalanceQueryreq";
      //	5.1	ECOP_SERVICE_0001鉴权码获取
		}else if("smsauthsend".equals(process_code)){
					bodymust.append("servnumber");
					request_code="smsauthsendreq";
					process_code ="smsauthsend";
	//			5.2	ECOP_SERVICE_0002鉴权码校验
		}else if("smsauthcheck".equals(process_code)){
					bodymust.append("servnumber,");
					bodymust.append("smsno");
					request_code="smsauthcheckreq";
					process_code ="smsauthcheck";
		}
		
		boolean  checked =checkJson(json, headmust.toString()+","+bodymust.toString());
		if(checked){
			Map<String,String> map = getJSONParam(json, headmust.toString());
			json.put("request_code", request_code);
			json.put("process_code", process_code);
			return map;
		}
		return null;
	}

	/**
	 * 校验JSON参数是否正确
	 * 
	 * @param json
	 * @param mustParams
	 *            不可忽略的参数 ，各参数用英文逗号隔开,如“a,b,c”
	 * @return
	 */
	private static boolean checkJson(JSONObject json, String mustParams) {
		boolean flag = true;
		if (StringUtils.isBlank(mustParams))
			return flag;
		String[] params = mustParams.split(",");
//		try {
			for (String param : params) {
				if (!json.containsKey(param)){
					if (hasDefaultValueEcopParams.containsKey(param)){
						json.put(param, hasDefaultValueEcopParams.getString(param));
					}else{
						flag = false;
						break;
					}
				}
//				Object ob = json.get(param);
//				if (ob == null || StringUtils.isBlank(ob.toString())) {
//					flag = false;
//					log.info("["+param+"] is null");
//					break;
//				}
			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			log.error(e);
//			flag = false;
//		}
		return flag;
	}

	/**
	 * 
	 * @param json
	 * @param headParams
	 *            除去头部请求参数
	 * @return
	 */
	private static Map<String, String> getJSONParam(JSONObject json, String headParams) {
		Map<String, String> map = new HashMap<String, String>();
		if (StringUtils.isNotBlank(headParams)) {
			// 保留body参数
			Set<String> set = new HashSet<String>();
			set.addAll(json.keySet());
			// 移除头部参数
			set.removeAll(Arrays.asList(headParams.split(",")));
			for (String ob : set) {
				map.put(ob, json.get(ob).toString());
			}
		}
		return map;
	}

}
