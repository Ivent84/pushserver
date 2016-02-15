package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.Device;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.notification.PushNotificationManager;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;
import util.DB;

public class IosPublisher {
	PushNotificationPayload payLoad;
	PushNotificationManager pushManager;

	public IosPublisher(String certificatePath, String certificatePassword) {
		init(certificatePath, certificatePassword);
	}

	public IosPublisher(String appid) {
		String[] result = getCert(appid);
		if (null == result)
			return;
		init(result[0], result[1]);
	}

	public IosPublisher() {
		String[] result = getCert(null);
		if (null == result)
			return;
		init(result[0], result[1]);
	}

	private String[] getCert(String appid) {
		if (null == appid)
			appid = "default";
		String[] params = { appid };

		Map<String, String> data = DB.queryRow("select * from ioscert where appid=?", params);
		if (null == data)
			return null;
		String[] result = { data.get("certpath"), data.get("certpassword") };
		return result;
	}

	private void init(String certificatePath, String certificatePassword) {
		pushManager = new PushNotificationManager();
		payLoad = new PushNotificationPayload();
		try {
			payLoad.addBadge(1); // iphone应用图标上小红圈上的数值
			payLoad.addSound("default");// 铃音
			// true：表示的是产品发布推送服务 false：表示的是产品测试推送服务
			pushManager.initializeConnection(
					new AppleNotificationServerBasicImpl(certificatePath, certificatePassword, false));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void publish(String[] content) {
		List<String[]> list = new ArrayList<String[]>();
		list.add(content);
		publish(list);
	}

	public void publish(List<String[]> content) {
		System.out.println("in ios publish");
		System.out.println("publish content is: " + content);
		List<PushedNotification> notifications = new ArrayList<PushedNotification>();
		for (int i = 0; i < content.size(); i++) {
			try {
				payLoad.addAlert(content.get(i)[1]); // 消息内容
				// 发送push消息
				Device device = new BasicDevice();
				device.setToken(content.get(i)[0]);
				PushedNotification notification = pushManager.sendNotification(device, payLoad, true);
				notifications.add(notification);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			pushManager.stopConnection();
		} catch (CommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeystoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void publish(List<String> target, String content) {

		@SuppressWarnings("unused")
		List<PushedNotification> notifications;

		try {
			payLoad.addAlert(content); // 消息内容
			List<Device> device = new ArrayList<Device>();
			for (String t : target) {
				device.add(new BasicDevice(t));
			}
			notifications = pushManager.sendNotifications(payLoad, device);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String args[]) {
		String deviceToken = "ecf0234b30ee5e5bdb7168c0a2cff80820fd543c4aeed6ed614d05c358bf5683";
		String alert = "push测试";// push的内容
		int badge = 100;// 图标小红圈的数值
		String sound = "default";// 铃音

		List<String> tokens = new ArrayList<String>();
		tokens.add(deviceToken);
		String certificatePath = "/Users/Ivent/Downloads/LocationPlanPush.p12";
		String certificatePassword = "";// 此处注意导出的证书密码不能为空因为空密码会报错
		boolean sendCount = true;

		try {
			PushNotificationPayload payLoad = new PushNotificationPayload();
			payLoad.addAlert(alert); // 消息内容
			payLoad.addBadge(badge); // iphone应用图标上小红圈上的数值
			if (null == sound) {
				payLoad.addSound(sound);// 铃音
			}
			PushNotificationManager pushManager = new PushNotificationManager();
			// true：表示的是产品发布推送服务 false：表示的是产品测试推送服务
			pushManager.initializeConnection(
					new AppleNotificationServerBasicImpl(certificatePath, certificatePassword, false));
			List<PushedNotification> notifications = new ArrayList<PushedNotification>(); // 发送push消息
			if (sendCount) {
				Device device = new BasicDevice();
				device.setToken(tokens.get(0));
				PushedNotification notification = pushManager.sendNotification(device, payLoad, true);
				notifications.add(notification);
			} else {
				List<Device> device = new ArrayList<Device>();
				for (String token : tokens) {
					device.add(new BasicDevice(token));
				}
				notifications = pushManager.sendNotifications(payLoad, device);
			}
			List<PushedNotification> failedNotifications = PushedNotification.findFailedNotifications(notifications);
			List<PushedNotification> successfulNotifications = PushedNotification
					.findSuccessfulNotifications(notifications);
			int failed = failedNotifications.size();
			int successful = successfulNotifications.size();
			pushManager.stopConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
