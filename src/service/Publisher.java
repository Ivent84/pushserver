package service;

import java.util.ArrayList;
import java.util.List;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

import config.Config;

/**
 * Uses a Future based API to MQTT.
 */
public class Publisher {
	
	private static Publisher publisher = null;
	
	public MQTT mqtt;
	
	protected Publisher(){
		String user = Config.getValue("apollo_user");
        String password = Config.getValue("apollo_password");
        String host = Config.getValue("apollo_host");
        int port = Integer.parseInt(Config.getValue("apollo_port"));
        mqtt = new MQTT();
        try {
			mqtt.setHost(host, port);
			mqtt.setUserName(user);
			mqtt.setPassword(password);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void init(){
		if(null == publisher)
			publisher = new Publisher();
	}
	public static void publish(String[] content){
		List<String[]> list = new ArrayList<String[]>();
		list.add(content);
		publish(list);
	}
	/**
	 * 
	 * @param content String[0]-topic/target, String[1]-body
	 */
	public static void publish(List<String[]> content){
		init();
		FutureConnection connection = publisher.mqtt.futureConnection();
		try {
			connection.connect().await();
			for(String[] i: content){
				connection.publish(new UTF8Buffer(i[0]), new UTF8Buffer(i[1]), QoS.AT_LEAST_ONCE, false);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			try {
				connection.disconnect().await();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
