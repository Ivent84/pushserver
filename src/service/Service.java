package service;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import net.sf.json.JSONObject;
import util.DB;

/**
 * Servlet implementation class Service
 * 记录手机对应的类型
 */
@WebServlet("/service")
public class Service extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Service() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Map<String, String[]> params = request.getParameterMap();
		String flag = params.get("flag")[0];
		Topic[] topic = {new Topic("register", QoS.AT_LEAST_ONCE)};
		if("".equals(flag) || "start".equals(flag)){
			Listener.start(topic, new ListenerCallback() {
				
				@Override
				public void onSuccess(String topic, String body) {
					// TODO Auto-generated method stub
					if("register".equals(topic)){
						JSONObject jo = JSONObject.fromObject(body);
						String phone = jo.getString("phone");
						String type = jo.getString("type");
						//存库
						String[] params = {phone, type};
						DB.execute("insert into phoneostype(phone,type) values(?,?)", params);
					}
				}
			});
		}else if("stop".equals(flag)){
			Listener.stop();
		}
	}

}
