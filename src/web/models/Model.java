package web.models;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import util.DB;

public class Model {
	
	protected String tablename = null;
	protected Map<String, Object> vars = new HashMap<String, Object>();
	
	public Model(String tablename){
		
		this.tablename = tablename;
		
	}
	public static void main(String args[]){
		Model model = new Model("config");
		model.set("title", "测试");
		model.set("code", "ceshi");
		model.set("value", "cs");
		model.save();
	}
	public void set(String key, String value){
		vars.put(key, value);
	}
	
	public void set(Map<String, Object> map){
		vars.putAll(map);
	}
	
	public void unset(String key){
		vars.put(key, null);
	}
	public boolean save(){
		
		return save(vars);
		
	}
	public boolean save(Map<String, Object> map){
		
		String sqlH = "insert into " + tablename + "";
		String sqlK = "";
		String sqlV = "";
		
		int size = map.size();
		String[] params = new String[size];
		
		if(size > 0){
			Set<String> kSet = map.keySet();
			Iterator<String> it = kSet.iterator();
			int i = 0;
			for(; it.hasNext();){
				String key = it.next();
				if (null != map.get(key)){
					sqlK += "," + key;
					sqlV += ",?";
					params[i] = (String) map.get(key);
					i++;
				}
			}
			if (sqlK.length() > 0){
				String sql = sqlH + "(" + sqlK.substring(1) + ")values(" + sqlV.substring(1) + ")";
				return DB.execute(sql, params);
			}
		}
		return false;
	}
	
}
