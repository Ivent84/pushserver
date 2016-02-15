package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lilystudio.smarty.Context;
import org.lilystudio.smarty.Engine;
import org.lilystudio.smarty.Template;
import org.lilystudio.smarty.TemplateException;

import config.Config;
import util.DB;

public class Common {
	protected Context context = new Context();
	protected String primaryKey = "id";
	protected Map<String, Map<String, Object>> fields = null;
	public Common(){
		context.set("basepath", UrlUtil.getBasePath());
	}
	private void init(String tableName, Map params){
		primaryKey = FieldContext.getPrimaryKey(tableName);
		context.set("primarykey", primaryKey);
		
		Map nParmas = new HashMap(params);
		if (nParmas.containsKey(primaryKey))
			nParmas.remove(primaryKey);
		if (nParmas.containsKey("page"))
			nParmas.remove("page");
		nParmas.put("methodname", "lists");
		context.set("listsurl", UrlUtil.encodeUrl(nParmas));
		nParmas.put("methodname", "edit");
		context.set("editurl", UrlUtil.encodeUrl(nParmas));
		nParmas.put("methodname", "add");
		context.set("addurl", UrlUtil.encodeUrl(nParmas));
		nParmas.put("methodname", "del");
		context.set("delurl", UrlUtil.encodeUrl(nParmas));
	}

	public void lists(HttpServletRequest request, HttpServletResponse response, Map params){
		
		String tableName = "";
		if(params.containsKey(Config.getValue("tablename"))){
			tableName = params.get(Config.getValue("tablename")).toString();
		}else{
			tableName = "config";
			params.put(Config.getValue("tablename"), tableName);
		}
		init(tableName, params);
		initFeild(tableName);
		initData(tableName, params.containsKey("page") ? Integer.parseInt(params.get("page").toString()) : 1);
		context.set("primarykey", FieldContext.getPrimaryKey(tableName));

		context.set("fromurl", request.getRequestURI());
		render(request.getRealPath("view/Common/lists.html"), response);
		
	}
	public void edit(HttpServletRequest request, HttpServletResponse response, Map params){
		String tableName = params.get(Config.getValue("tablename")).toString();
		init(tableName, params);
		initFeild(tableName);
		String primaryKeyValue = "";
		if (params.containsKey(primaryKey)){
			primaryKeyValue = params.get(primaryKey).toString();
		}else{
			primaryKeyValue = request.getParameter(context.get("primarykey").toString());
		}
		if (primaryKeyValue == null || "".equals(primaryKeyValue)){
			redirect(response, context.get("listsurl").toString(), "请勾选一条数据");
			return;
		}
		String sql;
		String[] queryParams;
		if("POST".equals(request.getMethod())){
			sql = "update " + tableName + " set";
			queryParams = new String[fields.size() + 1];
			Set<String> fset = fields.keySet();
			Iterator<String> it = fset.iterator();
			int i = 0;
			String sqlSet = "";
			for (;it.hasNext();){
				String key = it.next();
				if (!key.equals(primaryKey) || true){
					sqlSet += "," + key + "=?";
					queryParams[i] = request.getParameter(key);
					i++;
				}
			}
			sql += " " + sqlSet.substring(1);
			sql += " where " + primaryKey + "=?";
			queryParams[i] = primaryKeyValue;
			DB.execute(sql, queryParams);
			redirect(response, context.get("listsurl").toString());
			return;
 		}
		sql = "select * from " + tableName + " where " + primaryKey + "=?";
		queryParams = new String[1];
		queryParams[0] = primaryKeyValue;
		Map<String, String> data = DB.queryRow(sql, queryParams);
		
		Set<String> set = fields.keySet();
		Iterator<String> it = set.iterator();
		for (;it.hasNext();){
			String key = it.next();
			if(data.containsKey(key) && data.get(key) != null)
				fields.get(key).put("value", data.get(key).toString());
		}
		context.set("fields", fields);
		
		context.set("fromurl", request.getRequestURI());
		render(request.getRealPath("view/Common/edit.html"), response);
		
	}
	public void add(HttpServletRequest request, HttpServletResponse response, Map params){
		String tableName = params.get(Config.getValue("tablename")).toString();
		init(tableName, params);
		initFeild(tableName);
		
		if("POST".equals(request.getMethod())){
			String sql;
			String[] queryParams;
			sql = "insert into " + tableName;
			queryParams = new String[fields.size()];
			Set<String> fset = fields.keySet();
			Iterator<String> it = fset.iterator();
			int i = 0;
			String sqlSet = "";
			String sqlVal = "";
			for (;it.hasNext();){
				String key = it.next();
				if (!key.equals(primaryKey) || !"".equals(request.getParameter(key))){
					sqlSet += "," + key;
					sqlVal += ",?";
					queryParams[i] = request.getParameter(key);
					i++;
				}
			}
			sql += "(" + sqlSet.substring(1) + ")";
			sql += " values(" + sqlVal.substring(1) + ")";
			boolean res = DB.execute(sql, queryParams);
			if (res){
				redirect(response, context.get("listsurl").toString(),"success");
			}else{
				redirect(response, context.get("listsurl").toString(),"fail");
			}
			return;
		}
		context.set("fields", fields);
		
		context.set("fromurl", request.getRequestURI());
		render(request.getRealPath("view/Common/edit.html"), response);
		
	}
	public void del(HttpServletRequest request, HttpServletResponse response, Map params){
		String tableName = params.get(Config.getValue("tablename")).toString();
		init(tableName, params);
		initFeild(tableName);
		
		String[] primaryKeyValues = null;
		String sql = null;
		if (params.containsKey(primaryKey)){
			primaryKeyValues = new String[1];
			primaryKeyValues[0] = params.get(primaryKey).toString();
			sql = "delete from " + tableName + " where " + primaryKey + " in(?)";
		}else{
			primaryKeyValues = request.getParameterValues(primaryKey);
			String sqlN = "";
			for(int n = 0; n < primaryKeyValues.length; n++){
				sqlN += ",?";
			}
			sql = "delete from " + tableName + " where " + primaryKey + " in(" + sqlN.substring(1) +")";
		}
		boolean result = DB.execute(sql, primaryKeyValues);
		if("POST".equals(request.getMethod())){
			if (result){
				ajaxRender(response, "0", "success", "");
			}else{
				ajaxRender(response, "1", "fail", "");
			}
		}else{
			redirect(response, context.get("listsurl").toString());
		}
		
	}
	
	private void initData(String tableName, int page) {
		initData(tableName, page, "");
	}
	private void initData(String tableName, int page, String params) {
		// TODO Auto-generated method stub
		String sql = "select * from " + tableName + " where 1 = 1 " + params;
		int rowsOfOnePage = Integer.parseInt(Config.getValue("rowsOfOnePage"));
		sql += " limit " + ((page-1)*rowsOfOnePage) + "," + rowsOfOnePage;
		context.set("data", DB.queryRows(sql));
	}

	protected void render(String templatePath, HttpServletResponse response){
		try {
			Engine engine = new Engine();
			Template template = engine.getTemplate(templatePath);
			PrintWriter out = response.getWriter(); //设置接收模板数据的输出流
			template.merge(context, out); // 处理生成结果
		} catch (TemplateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void ajaxRender(HttpServletResponse response, String errcode, String errmsg, String url){
		try {
			response.getWriter().append("{\"errcode\":\""+errcode+"\",\"errmsg\":\""+errmsg+"\",\"url\":\""+url+"\"}");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void redirect(HttpServletResponse response, String url, String msg) {
		try {
//			response.sendRedirect(url);
			response.setHeader("refresh", "1;url="+url);
			response.getWriter().append("<body>"+msg+"</body>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	protected void redirect(HttpServletResponse response, String url) {
		redirect(response, url, "");
	}
	
	@SuppressWarnings("unchecked")
	protected void initFeild(String tableName){
		fields = (Map<String, Map<String, Object>>) FieldContext.getfield(tableName);
		context.set("fields", fields);
	}
}
