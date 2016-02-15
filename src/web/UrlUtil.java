package web;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class UrlUtil {
	public static String basePath = null;
	
	public static void setBasePath(String bPath){
		if (basePath == null)
			basePath = bPath;
	}
	
	public static String getBasePath(){
		return basePath;
	}
	
	public static String encodeUrl(String modulename, String classname, String methodname){
		Map<String, String> params = new HashMap<String, String>();
		params.put("modulename", modulename);
		params.put("classname", classname);
		params.put("methodname", methodname);
		return encodeUrl(params);
	}
	public static String encodeUrl(Map<String, String> params){
		Map<String, String> nparams = new HashMap(params);
		String result = basePath + "/" + nparams .get("modulename") + "/" + nparams.get("classname") + "/" + nparams.get("methodname");
		nparams.remove("modulename");
		nparams.remove("classname");
		nparams.remove("methodname");
		if (nparams.size() > 0){
			Set<String> keys = nparams.keySet();
			Iterator<String> it = keys.iterator();
			String key;
			for (; it.hasNext(); ){
				key = it.next();
				result += "/" + key + "/" + nparams.get(key);
			}
		}
		return result;
	}
	
	public static Map<String, String> decodeUrl(String uri){
		
		String[] uriList = uri.replace((basePath == "/" ? "" : basePath), "").split("/");
		
		Map<String, String> params = new HashMap<>();
		
		if(uriList.length > 1)
			params.put("modulename", uriList[1]);
		if(uriList.length > 1)
			params.put("classname", uriList[2]);
		if(uriList.length > 2)
			params.put("methodname", uriList[3]);
		
		String param = "";
		for (int i = 4; i < uriList.length; i++) {
			if ("".equals(param)) {
				param = uriList[i];
			} else {
				params.put(param, uriList[i]);
				param = "";
			}
		}
		if (!"".equals(param)) {
			params.put("_last_", param);
		}
		
		return params;
	}
}
