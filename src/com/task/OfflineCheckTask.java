package com.task;

import java.util.Iterator;
import java.util.Set;
import java.util.TimerTask;

import com.redis.RedisDB;

import config.Config;
import redis.clients.jedis.JedisCommands;
import service.Notice;

public class OfflineCheckTask extends TimerTask {

	private static boolean checkIosOfflineIsRunning = false;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		checkIosOffline();
	}
	/**
	 * 检查IOS掉线并通知
	 */
	public void checkIosOffline() {
		if (checkIosOfflineIsRunning)
			return;
		checkIosOfflineIsRunning = true;
		System.out.println("in ios offline check");
		// TODO
		JedisCommands jedis = RedisDB.getConn();
		Notice notice = new Notice();
		Set<String> keys = jedis.hkeys("iosmobilelist");
		Iterator<String> ite = keys.iterator();
		for (; ite.hasNext();) {
			String key = ite.next();
			if (jedis.get(key) == null) {
				jedis.hdel("iosmobilelist", key);
				notice.notifyIosUser(key, Config.getValue("appdienotice"));
			}
		}
		RedisDB.releaseConn(jedis);
		checkIosOfflineIsRunning = false;
	}
}
