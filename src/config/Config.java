package config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Config {
	private static Config config = null;
	private Map<String, String> data = null;
	protected Config(){
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("config.properties");
		Properties p = new Properties();
		try {
			p.load(inputStream);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Map<String, String> tConfig = new HashMap<String, String>();
		Set<Object> keys = p.keySet();
		for(Object key : keys){
			String value = "";
			try {
				value = new String(p.getProperty(key.toString()).getBytes("ISO8859-1"), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tConfig.put(key.toString(), value);
		}
		data = tConfig;
	}
	public static String getValue(String name){
		if(config == null)
			config = new Config();
		return config.data.get(name);
	}
}
