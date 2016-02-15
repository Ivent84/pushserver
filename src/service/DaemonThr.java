package service;

import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import config.Config;

public class DaemonThr {
	
	/**
	 * 仅用于Android
	 */
	public void runTopicWillListener() {
		Topic[] topic = { new Topic("willTopic", QoS.AT_MOST_ONCE) };
		Listener.start(topic, new ListenerCallback() {

			@Override
			public void onSuccess(String topic, String body) {
				//通知用户
				new Notice().notifyAndrUser(body,Config.getValue("appdienotice"));
			}
		});
	}
}
