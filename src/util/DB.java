package util;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.Config;

public class DB{
	
	private static ConnectionPool connPool = null;
	
	private DB() {}
	/**
	 * 使用完后，必须调用releaseConnetion释放
	 * @return
	 */
	public static Connection getConnection(){
		if(connPool == null){
			ConnectionParam param = new ConnectionParam();
			param.setDriver(Config.getValue("driver"));
			param.setUrl(Config.getValue("url"));
			param.setUser(Config.getValue("user"));
			param.setPassword(Config.getValue("password"));
			param.setMinConnection(Integer.parseInt(Config.getValue("minConnection")));
			param.setMaxConnection(Integer.parseInt(Config.getValue("maxConnection")));
			param.setTimeoutValue(Integer.parseInt(Config.getValue("timeoutValue")));
			param.setWaitTime(Integer.parseInt(Config.getValue("waitTime")));
			param.setIncrementalConnections(Integer.parseInt(Config.getValue("incrementalConnections")));
			
			connPool = new ConnectionPool(param);
			try {
				connPool.createPool();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		try {
			return connPool.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public static void releaseConnetion(Connection conn){
		connPool.returnConnection(conn);
	}
	public static boolean execute(String sql){
		return execute(sql, null);
	}
	public static boolean execute(String sql, String[] params){
		Connection conn = getConnection();
		if(null == conn)
			return false;
		boolean result = true;
		try {
			CallableStatement prep = conn.prepareCall(sql);
			if(null != params){
				int index = 0;
				for(int i = 0; i < params.length; i++){
					if (params[i] != null){
						index++;
						prep.setString(index, params[i]);
					}
				}
			}
			prep.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = false;
		} finally {
			releaseConnetion(conn);
		}
		return result;
	}
	public static Map<String, String> queryRow(String sql){
		return queryRow(sql, null);
	}
	public static Map<String, String> queryRow(String sql, String[] params){
		List<Map<String, String>> rows = queryRows(sql, params);
		if(0 == rows.size()){
			return null;
		}else{
			return rows.get(0);
		}
	}
	public static List<Map<String, String>> queryRows(String sql){
		return queryRows(sql, null);
	}
	public static List<Map<String, String>> queryRows(String sql, String[] params){
		Connection conn = getConnection();
		if(null == conn)
			return null;
		List<Map<String, String>> result = null;
		try {
			CallableStatement prep = conn.prepareCall(sql);
			if(null != params){
				int index = 0;
				for(int i = 0; i < params.length; i++){
					if (params[i] != null){
						index++;
						prep.setString(index, params[i]);
					}
				}
			}
			ResultSet rs = prep.executeQuery();
			result = new ArrayList<Map<String, String>>();
			
			ResultSetMetaData md = rs.getMetaData();
			List<String> colums = new ArrayList<String>();
			for(int i = 0; i < md.getColumnCount(); i++){
				colums.add(md.getColumnLabel(i+1));
			}
			Map<String, String> map;
			while(rs.next()){
				map = new HashMap<String, String>();
				for(int j = 0; j < colums.size(); j++){
					map.put(colums.get(j), rs.getString(colums.get(j)));
				}
				result.add(map);
			}
			prep.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			releaseConnetion(conn);
		}
		return result;
	}
	public static List<String> getColumn(String sql){
		return getColumn(sql, null);
	}
	public static List<String> getColumn(String sql, String columnName){
		Connection conn = getConnection();
		if(null == conn)
			return null;
		List<String> result = new ArrayList<String>();
		try {
			CallableStatement prep = conn.prepareCall(sql);
			ResultSet rs = prep.executeQuery();
			while(rs.next()){
				if(null == columnName){
					result.add(rs.getString(1));
				}else{
					result.add(rs.getString(columnName));
				}
			}
			prep.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			releaseConnetion(conn);
		}
		return result;
	}
}
