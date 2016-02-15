/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package service;

import java.net.URISyntaxException;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Topic;

import config.Config;

/**
 * Uses an callback based interface to MQTT.  Callback based interfaces
 * are harder to use but are slightly more efficient.
 */
public class Listener {
	boolean flag = true;
	private static Listener listener = null;
	private int status = 0;//0-未监听，1-已监听
	CallbackConnection connection = null;
	protected Listener(){
		String user = Config.getValue("apollo_user");
        String password = Config.getValue("apollo_password");
        String host = Config.getValue("apollo_host");
        int port = Integer.parseInt(Config.getValue("apollo_port"));
        MQTT mqtt = new MQTT();
        try {
			mqtt.setHost(host, port);
			mqtt.setUserName(user);
			mqtt.setPassword(password);
			connection = mqtt.callbackConnection();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static int getStatus(){
		if(null == listener){
			listener = new Listener();
		}
		return listener.status;
	}
	public void setStatus(int status){
		this.status = status;
	}
	public static void start(final Topic[] topics, final ListenerCallback callback){
		if(getStatus() == 0){
			listener._start(topics, callback);
			listener.setStatus(1);
		}
	}
	public static void stop(){
		if(getStatus() == 1){
			listener._stop();
			listener.setStatus(0);
		}
	}
	private void _stop() {
		// TODO Auto-generated method stub
		connection.disconnect(new Callback<Void>(){

			@Override
			public void onFailure(Throwable arg0) {}

			@Override
			public void onSuccess(Void arg0) {}
			
		});
	}
	private void _start(final Topic[] topics, final ListenerCallback callback){
        //subscribe
        connection.connect(new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                connection.subscribe(topics, new Callback<byte[]>() {
                    public void onSuccess(byte[] qoses) {
                    }
                    public void onFailure(Throwable value) {
                        value.printStackTrace();
                        flag = false;
                    }
                });
            }
            @Override
            public void onFailure(Throwable value) {
                value.printStackTrace();
                flag = false;
            }
        });
        if(!flag){
        	return;
        }
        //listen
        connection.listener(new org.fusesource.mqtt.client.Listener() {
            public void onConnected() {
            }
            public void onDisconnected() {
            }
            public void onFailure(Throwable value) {
                value.printStackTrace();
                flag = false;
            }
            public void onPublish(UTF8Buffer topic, Buffer msg, Runnable ack) {
                String body = msg.utf8().toString();
                callback.onSuccess(topic.utf8().toString(), body);
            }
        });
	}
}
