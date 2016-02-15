package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lilystudio.smarty.Context;
import org.lilystudio.smarty.Engine;
import org.lilystudio.smarty.Template;
import org.lilystudio.smarty.TemplateException;

import config.Config;
import util.DB;

public class Sysconfig {
	protected Context context = new Context();
	
	public void v(HttpServletRequest request, HttpServletResponse response, Map params){
		String[] where = {"1"};
		List<Map<String, String>> data = DB.queryRows("select * from config where status = ?", where);
		context.set("data", data);
		context.set("formUrl", UrlUtil.encodeUrl("web", "Sysconfig", "s"));
		render(request.getRealPath("view/Sysconfig/edit.html"), response);
	}
	
	public void s(HttpServletRequest request, HttpServletResponse response, Map params) throws IOException{
		System.out.println(request.getParameter("syncseconds"));
		PrintWriter out = response.getWriter();
		System.out.println(request.getReader().readLine());
        Enumeration en = request.getParameterNames();

        while (en.hasMoreElements()) {

            String paramName = (String) en.nextElement();
            out.println(paramName + " = " + request.getParameter(paramName) + "<br/>");

        }

//		redirect(UrlUtil.encodeUrl("web", "Sysconfig", "v"), response);
	}
	
	protected void render(String templatePath, HttpServletResponse response){
		response.setCharacterEncoding(Config.getValue("charset"));
		try {
			Engine engine = new Engine();
			Template template = engine.getTemplate(templatePath);
			PrintWriter out = response.getWriter(); //设置接收模板数据的输出流
			template.merge(context, out); // 处理生成结果
		} catch (TemplateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void redirect(String url, HttpServletResponse response) {
		try {
			response.getWriter().append("<html><head><meta http-equiv=\"refresh\" content=\"0;url="+url+"\"></head></html>");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
