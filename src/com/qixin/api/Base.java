package com.qixin.api;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import config.Config;
import net.sf.json.JSONObject;
import util.CoderUtils;
import util.HTTPUtils;
import util.RandomUtils;

public class Base {

	public static String _getApiData(String urlstr, JSONObject data){
		String secret = Config.getValue("sysSecretEmssdk").split("-")[0];
		String key = RandomUtils.generateRandom(8, RandomUtils.CHAR_HEX);
		String token = CoderUtils.enDES(key, secret);
		data.put("token", token);
		
		Map<String, String> header = new HashMap<String, String>();
		String sign = generateSign(key, data.toString());
		System.out.println(sign);
		header.put("sign", sign);
		return HTTPUtils.httpsSend(urlstr, header, data.toString(), Config.getValue("charset"), Config.getValue("charset"));
	}
	
	public static String generateSign(Map<String, String> data){
		JSONObject jo = JSONObject.fromObject(data);
		return generateSign(null, jo.toString());
	}
	
	public static String generateSign(String key, Map<String, String> data) {
		JSONObject jo = JSONObject.fromObject(data);
		return generateSign(key, jo.toString());
	}
	public static String generateSign(String key, String source) {
		int[] indexs = { 4, 8, 12, 14, 18, 20, 24, 28 };
		if (key == null || key.length() != indexs.length) {
			key = RandomUtils.generateRandom(indexs.length, RandomUtils.CHAR_HEX);
		}
		char[] encrypts = generateMD5(key + "&" + source, 3, 3).toCharArray();
		char[] results = new char[indexs.length + encrypts.length];
		int t = 0, a = 0, b = 0;
		for (int i = 0; i < indexs.length; i++) {
			b = indexs[i];
			System.arraycopy(encrypts, a, results, a + i, b - a);
			results[b + i] = key.charAt(i);
			a = b;
			t = a + i + 1;
		}
		System.arraycopy(encrypts, a, results, t, results.length - t);
		String sign = String.valueOf(results);
		return sign;
	}
	public static String generateMD5(String source, int x, int y) {
		char[] chars = source.toCharArray();
		int n = 0;
		for (char c : chars) {
			n += c;
		}
		n =	(n % x) + y;
		return encryptMD5(source, n);
	}
	public static String encryptMD5(String source, int n) {
		try {
			for (int i = 0; i < n; i++) {
				source = MD5(source);
			}
			return source;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public final static String MD5(String s) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};       
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
	}
}
