package com.redis;

import java.util.ArrayList;
import java.util.List;

import org.junit.internal.matchers.IsCollectionContaining;

import config.Config;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

public class RedisDB {
	protected static JedisPool jedisPool;//非切片连接池
	
	protected static ShardedJedisPool shardedJedisPool;//切片连接池
	
//	protected 
	
	protected static boolean isCluster;
	
	protected static String[] hosts;
	protected static int port;
    
	static{
		String str = Config.getValue("redisHosts");
		hosts = str.split(",");
		port = Integer.parseInt(Config.getValue("redisPort"));
		if(hosts.length == 1){
        	isCluster = false;
        }else{
        	isCluster = true;
        }
		JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(Integer.parseInt(Config.getValue("redisMaxTotal")));
        config.setMaxIdle(Integer.parseInt(Config.getValue("redisMaxIdle")));
        config.setMaxWaitMillis(Long.parseLong(Config.getValue("redisMaxWaitMillis")));
        config.setTestOnBorrow(false);
        
        jedisPool = new JedisPool(config, hosts[0], port);
        
        if(isCluster){
        	List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
        	for(int i = 0; i < hosts.length; i++){
        		System.out.println("<-----" + hosts[i] + "------>");
        		if(0 == i){
        			shards.add(new JedisShardInfo(hosts[i], port, "master"));
        		}else{
        			shards.add(new JedisShardInfo(hosts[i], port, "slave"));
        		}
        	}
            shardedJedisPool = new ShardedJedisPool(config, shards);
        }
		System.out.println("finish init redisdb");
	}
	
	/**
	 * 非切片
	 * @return
	 */
	public static Jedis getJedis(){
		System.out.println(jedisPool.getNumActive());
		return jedisPool.getResource();
	}
	public static void releaseJedis(Jedis jedis){
		if(null != jedis)
			jedis.close();
	}
	
	/**
	 * 切片
	 * @return
	 */
	public static ShardedJedis getShJedis(){
		return shardedJedisPool.getResource();
	}
	public static void releaseShJedis(ShardedJedis jedis){
		if(null != jedis)
			jedis.close();
	}
	/**
	 * 混合切片非切片,使用完成后必须调用releaseConn；
	 * @return
	 */
	public static JedisCommands getConn(){
		if(isCluster){
			System.out.println("run in cluster");
			return getShJedis();
		}else{
			System.out.println("run in one host");
			return getJedis();
		}
	}
	public static void releaseConn(JedisCommands jedis){
		if(null == jedis)
			return;
		if(isCluster){
			releaseShJedis((ShardedJedis) jedis);
		}else{
			releaseJedis((Jedis) jedis);
		}
	}
	public static String set(String key, String value){
		String result;
		JedisCommands jedis = getConn();
		result = jedis.set(key, value);
		releaseConn(jedis);
		return result;
	}
	public static String get(String key){
		String result;
		JedisCommands jedis = getConn();
		result = jedis.get(key);
		releaseConn(jedis);
		return result;
	}
	public static String setandexpire(String key, String value, int time){
		String result;
		JedisCommands jedis = getConn();
		result = jedis.get(key);
		jedis.set(key, value);
		jedis.expire(key, time);
		releaseConn(jedis);
		return result;
	}
}
