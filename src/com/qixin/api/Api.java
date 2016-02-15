package com.qixin.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import config.Config;
import net.sf.json.JSONObject;

public class Api extends Base {
	private static String url = Config.getValue("qixinurl");
	public static String getContentFromServ(String operation, JSONObject jo){
		Iterator keys = jo.keys();
		String key = null;
		Map<String, String> data = new HashMap<String, String>();
		for(;keys.hasNext();){
			key = (String) keys.next();
			data.put(key, jo.getString(key));
		}
		System.out.println(data);
		return _getApiData(url + operation + ".do", jo);
	}
}
