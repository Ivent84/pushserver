package web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import config.Config;
import util.DB;

public class FieldContext {
	
	private static Map<String, Object> fields = new HashMap<String, Object>();
	private static Map<String, Long> fieldsPeroid = new HashMap<String, Long>();
	private static Map<String, String> fieldsPrimaryKey = new HashMap<String, String>();
	
	private static long peroid = Long.parseLong(Config.getValue("feildPeroidMin"));
	
	public static String getPrimaryKey(String tableName){
		if (!fieldsPrimaryKey.containsKey(tableName))
			initField(tableName);
		if (fieldsPrimaryKey.containsKey(tableName))
			return fieldsPrimaryKey.get(tableName);
		return "id";
	}
	
	public static Object getfield(String tableName){
		if (fieldsPeroid.containsKey(tableName)){
			if (fieldsPeroid.get(tableName) > java.lang.System.currentTimeMillis() + peroid * 60 * 1000)
				return fields.get(tableName);
		}
		return initField(tableName);
	}
	
	private static Object initField(String tableName){
		Map<String, Map<String, Object>> field = new LinkedHashMap<String, Map<String, Object>>();
		String sql = "SHOW FULL COLUMNS FROM " + tableName;
		
		List<Map<String, String>> _field = DB.queryRows(sql);
		
		if (!_field.isEmpty()){
			Map<String, Object> oneMap = null;
			String primaryKey = "id";
			for (Map oneField: _field){
				oneMap = new HashMap<String, Object>();
				oneMap.put("field", oneField.get("Field").toString());
//				oneMap.put("collation", oneField.get("Collation").toString());
				
				oneMap.put("default", oneField.get("Default") == null ? null: oneField.get("Default").toString());
				oneMap.put("value", oneField.get("Default") == null ? null: oneField.get("Default").toString());
				oneMap.put("extra", oneField.get("Extra").toString());
				if ("auto_increment".equals(oneField.get("Extra").toString()) || null != oneField.get("Default")){
					oneMap.put("null", "YES");
				}else{
					oneMap.put("null", oneField.get("Null").toString());
				}
				
				oneMap.put("key", oneField.get("Key").toString());
				if ("PRI".equals(oneField.get("Key").toString())){
					primaryKey = oneField.get("Field").toString();
				}
//				oneMap.put("privileges", oneField.get("Privileges").toString());
				Map<String, Object> comments = parseComment(oneField.get("Comment").toString());
				oneMap.put("comment", "".equals(comments.get("comment").toString()) ? oneField.get("Field").toString() : comments.get("comment").toString());
				
				String type = parseType(oneField.get("Type").toString());
				//处理编辑、展示类型
				if (comments.containsKey("showtype")){
					oneMap.put("showtype", comments.get("showtype"));
					oneMap.put("edittype", comments.get("edittype"));
				}else{
					oneMap.put("showtype", type);
					oneMap.put("edittype", type);
				}
				//取值范围
				if (comments.containsKey("values")){
					oneMap.put("values", comments.get("values"));
				}else{
					oneMap.put("values", "");
				}
				
				oneMap.put("dbtype", type);
				field.put(oneMap.get("field").toString(), oneMap);
			}
			
			fieldsPrimaryKey.put(tableName, primaryKey);
		}
		
		fieldsPeroid.put(tableName, java.lang.System.currentTimeMillis());
		fields.put(tableName, field);
		return field;
	}
	
	private static String parseType(String dbType){
		String type = "string";
		if (dbType.indexOf("int") != -1){
			type = "int";
		}else if (false){
			
		}
		
		return type;
	}
	private static Map<String, Object> parseComment(String comment){
		Map<String, Object> result = new HashMap<String, Object>();
		String[] comments = comment.split("\\|", -1);
		switch (comments.length){
		case 4:
			if(comments[3].length()>0)
				result.put("edittype", comments[3]);
		case 3:
			if(comments[2].length()>0){
				result.put("showtype", comments[2]);
				if (!result.containsKey("edittype"))
					result.put("edittype", result.get("showtype"));
			}
		case 2:
			result.put("values", parseOptions(comments[1]));
		case 1:
			result.put("comment", comments[0]);
			break;
		default:
			break;
		}
		return result;
	}
	private static Map<String, String> parseOptions(String expression){
		if ("".equals(expression))
			return null;
		
		Map<String, String> result = new HashMap<String, String>();
		String[] options = expression.split(";");
		for(int i = 0; i<options.length; i++){
			String[] value = options[i].split(":");
			if (value.length == 1){
				result.put(value[0], value[0]);
			}else{
				result.put(value[0], value[1]);
			}
		}
		return result;
	}
	public static void main(String args[]){
		initField("config");
	}
}
