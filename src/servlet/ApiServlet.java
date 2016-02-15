package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qixin.api.Api;

import net.sf.json.JSONObject;
import util.BodyReaderHttpServletRequestWrapper;

/**
 * Servlet implementation class Api
 */
@WebServlet("/qixinapi")
public class ApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ApiServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject jo = ((BodyReaderHttpServletRequestWrapper) request).getRequestAsJson();
		System.out.println(jo);
		if(jo == null || ! jo.has("op")){
			response.getWriter().append("op is need");
			return;
		}
		String operation = jo.getString("op");
		jo.remove("op");
		System.out.println(Api.getContentFromServ(operation, jo));
	}

}
