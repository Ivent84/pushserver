package web;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import config.Config;

public class Member extends Common {
	public void login(HttpServletRequest request, HttpServletResponse response, Map params){
		if("POST".equals(request.getMethod())){
			String userName = request.getParameter("username");
			String password = request.getParameter("password");
			if ("admin".equals(userName) && password.equals(Config.getValue("webAdminsPassword"))){
				HttpSession session = request.getSession();
				session.setAttribute("userid", userName);
				try {
					response.sendRedirect(UrlUtil.encodeUrl("web", "Common", "lists"));
					return;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		context.set("fromurl", request.getRequestURI());
		render(request.getServletContext().getRealPath("view/Common/login.html"), response);
	}
}
