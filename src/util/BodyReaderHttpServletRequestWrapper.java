package util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import jodd.JoddDefault;
import jodd.io.StreamUtil;
import net.sf.json.JSONObject;
/**
 * 转换request，使得支持多次读取reader数据流
 * @author Ivent
 *
 */
public class BodyReaderHttpServletRequestWrapper extends HttpServletRequestWrapper {
	private final byte[] body;
	private Map<String, String[]> parameterMap = null;
	public BodyReaderHttpServletRequestWrapper(HttpServletRequest request) throws IOException{
		super(request);
		// TODO Auto-generated constructor stub
		body = StreamUtil.readBytes(request.getReader(), JoddDefault.encoding);
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(getInputStream()));
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		final ByteArrayInputStream bais = new ByteArrayInputStream(body);
		return new ServletInputStream() {
			@Override
			public int read() throws IOException {
				return bais.read();
			}
		};
	}
	public JSONObject getRequestAsJson(){
		try {
			JSONObject jo = JSONObject.fromObject(new String(body));
			return jo;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

//	@Override
//	public String getParameter(String name) {
//		// TODO Auto-generated method stub
//		return parameterMap.get(name)[0];
//	}
//
//	@Override
//	public Enumeration<String> getParameterNames() {
//		// TODO Auto-generated method stub
//		return new Vector<String>(getParameterMap().keySet()).elements();
//	}
//
//	@Override
//	public String[] getParameterValues(String name) {
//		// TODO Auto-generated method stub
//		return getParameterMap().get(name);
//	}
//	@Override
//	public Map<String, String[]> getParameterMap() {
//		// TODO Auto-generated method stub
//		if(parameterMap == null){
//			String[] lists = body.toString().split("&");
//			Map<String, String[]> map = new HashMap<String, String[]>();
//			for(int i = 0; i < lists.length; i++){
//				int len = 1;
//				String[] subs;
//				String[] sub = lists[i].split("=");
//				if (!map.containsKey(sub[0])){
//					len += map.get(sub[0]).length;
//					subs = new String[len];
//					System.arraycopy(map.get(sub[0]), 0, subs, 0, len-1);
//					subs[len-1] = sub[1];
//					map.put(sub[0], subs);
//				}else{
//					subs = new String[len];
//					subs[0] = sub[1];
//					map.put(sub[0], subs);
//				}
//			}
//			parameterMap = map;
//		}
//		return parameterMap;
//	}
}
