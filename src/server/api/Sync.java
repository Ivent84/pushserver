package server.api;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.redis.RedisDB;

import config.Config;
import net.sf.json.JSONObject;
import redis.clients.jedis.JedisCommands;

/**
 * Servlet implementation class Sync
 */
@WebServlet("/sync")
public class Sync extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Sync() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		BufferedReader reader = request.getReader();
		String req = reader.readLine();
		JSONObject jo = JSONObject.fromObject(req);
		if(jo.has("mobile")){
			System.out.println("ios sync "+jo.get("mobile"));
			String mobile = (String) jo.get("mobile");
			JedisCommands jedis = RedisDB.getConn();
			String val = jedis.get(mobile);
			if(null == val){
				jedis.hset("iosmobilelist", mobile, "1");
				jedis.set(mobile, "1");
			}
			int time = Integer.parseInt(Config.getValue("heartbeatTimeOutSec"));
			jedis.expire(mobile, time);
			RedisDB.releaseConn(jedis);
		}
		
	}

}
