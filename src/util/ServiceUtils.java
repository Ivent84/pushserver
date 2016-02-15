package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import config.Global;

/**
 * 业务工具类
 * 
 * @author Lilibo
 *
 */
public class ServiceUtils {

	/**
	 * 验证请求合法性
	 * 
	 * @param token
	 * @return
	 */
	public static boolean checkOperate(String token) {
		if (null == token) {
			return false;
		}
		Calendar calendar = Calendar.getInstance();
		String secret = (calendar.get(Calendar.MONTH) + calendar.get(Calendar.DATE) + calendar.get(Calendar.HOUR) + calendar.get(Calendar.MINUTE)) + "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		System.out.println(sdf.format(calendar.getTime()) + " " + secret);
		return secret.equals(token);
	}

	/**
	 * 生成多次MD5加密值
	 * <br/>
	 * 生成规则：MD5(source, ((SUM(source.toCharArray()) % x) + y))
	 * 
	 * @param source
	 * @param x
	 * @param y
	 * @return
	 */
	public static String generateMD5(String source, int x, int y) {
		char[] chars = source.toCharArray();
		int n = 0;
		for (char c : chars) {
			n += c;
		}
		n =	(n % x) + y;
		return CoderUtils.encryptMD5(source, n);
	}

	/**
	 * 生成Token值
	 * <br/>
	 * 生成规则：MD5(source, ((SUM(source.toCharArray()) % 3) + 3))
	 * 
	 * @param source
	 * @return
	 */
	public static String generateToken(String source) {
		return generateMD5(source, 3, 3);
	}

	/**
	 * 验证Token值
	 * 
	 * @param source
	 * @return
	 */
	public static boolean checkToken(String source, String token) {
		if (source == null || token == null) {
			return false;
		}
		return token.equals(generateToken(source));
	}

	/**
	 * 格式化时间
	 * 
	 * @param source
	 * @return
	 */
	public static long formatTime(String source) {
		return formatTime(source, Global.dateformat);
	}

	/**
	 * 格式化时间
	 * 
	 * @param source
	 * @param format
	 * @return
	 */
	public static long formatTime(String source, String format) {
		SimpleDateFormat fdf = new SimpleDateFormat(format);
		try {
			Date date = fdf.parse(source);
			return date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
			return 0L;
		}
	}

	/**
	 * 从消息中提取IMSI加密串
	 * 
	 * @param source
	 * @return
	 */
	public static String extractMagic(String source) {
		String[] splits = msgExtract(source, Global.splitregex);
		if (splits == null || splits.length != 2) {
			return null;
		}
		return splits[0];
	}

	/**
	 * 从消息中提取开户标识
	 * 
	 * @param source
	 * @return
	 */
	public static String extractIdent(String source) {
		String[] splits = msgExtract(source, Global.splitregex);
		if (splits == null || splits.length != 2) {
			return null;
		}
		return splits[1];
	}

	/**
	 * 消息提取
	 * 
	 * @param source
	 * @param regex
	 * @return
	 */
	public static String[] msgExtract(String source, String regex) {
		return source.split(regex);
	}

	/**
	 * 验证开户标识
	 * 
	 * @param source
	 * @param regex
	 * @return
	 */
	public static boolean checkEnrollTag(String source) {
		return Global.TAG_KTQX.equals(extractIdent(source));
	}

	/**
	 * 验证手机号码存储标识
	 * 
	 * @param source
	 * @param regex
	 * @return
	 */
	public static boolean checkMobileTag(String source) {
		return Global.TAG_HQSJH.equals(extractIdent(source));
	}

	/**
	 * 根据IMSI生成密码
	 * 
	 * @param source
	 * @return
	 */
	public static String generatePassword(String source) {
		return source.substring(source.length() - 6);
	}

	/**
	 * 生成魔幻数据
	 * 
	 * @param key
	 * @param source
	 * @param indexs
	 * @return {key, magic}
	 */
	public static String[] generateMagic(String key, String source, int[] indexs) {
		if (key == null || key.length() != indexs.length) {
			key = RandomUtils.generateRandom(indexs.length, RandomUtils.CHAR_HEX);
		}
		char[] encrypts = CoderUtils.enDES(key, source).toCharArray();
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
		String magic = String.valueOf(results);
		return new String[] {key, magic};
	}

	/**
	 * 生成魔幻数据
	 * 
	 * @param source
	 * @param indexs
	 * @return {key, magic}
	 */
	public static String[] generateMagic(String source, int[] indexs) {
		String key = RandomUtils.generateRandom(indexs.length, RandomUtils.CHAR_HEX);
		char[] encrypts = CoderUtils.enDES(key, source).toCharArray();
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
		String magic = String.valueOf(results);
		return new String[] {key, magic};
	}

	/**
	 * 解密魔幻数据
	 * 
	 * @param source
	 * @param indexs
	 * @return {key, imsi}
	 */
	public static String[] decryptMagic(String source, int[] indexs) {
		char[] encrypts = source.toCharArray();
		char[] keys = new char[indexs.length];
		char[] imsis = new char[source.length() - indexs.length];
		int t = 0, a = 0, b = 0;
		for (int i = 0; i < indexs.length; i++) {
			b = indexs[i];
			System.arraycopy(encrypts, a + i, imsis, a, b - a);
			keys[i] = encrypts[b + i];
			a = b;
			t = a + i + 1;
		}
		System.arraycopy(encrypts, t, imsis, a, encrypts.length - t);
		String key = String.valueOf(keys);
		String imsi = CoderUtils.deDES(key, String.valueOf(imsis));
		return new String[] {key, imsi};
	}

	/**
	 * RCS开户生成魔幻数据
	 * 
	 * @param source
	 * @return {key, magic}
	 */
	public static String[] generateMagicEnroll(String source) {
		int[] indexs = {4, 8, 12, 14, 18, 20, 24, 28};
		return generateMagic(source, indexs);
	}

	/**
	 * RCS开户解密魔幻数据
	 * 
	 * @param source
	 * @return {key, imsi}
	 */
	public static String[] decryptMagicEnroll(String source) {
		int[] indexs = {4, 8, 12, 14, 18, 20, 24, 28};
		return decryptMagic(source, indexs);
	}

	/**
	 * 获取手机号码生成魔幻数据
	 * 
	 * @param source
	 * @return {key, magic}
	 */
	public static String[] generateMagicMobile(String source) {
		int[] indexs = {4, 8, 12, 15, 17, 20, 24, 28};
		return generateMagic(source, indexs);
	}

	/**
	 * 获取手机号码解密魔幻数据
	 * 
	 * @param source
	 * @return {key, imsi}
	 */
	public static String[] decryptMagicMobile(String source) {
		int[] indexs = {4, 8, 12, 15, 17, 20, 24, 28};
		return decryptMagic(source, indexs);
	}

	/**
	 * 根据IMSI生成魔幻凭证
	 * 
	 * @param key
	 * @param source
	 * @param iorder
	 * @return {key, token}
	 */
	public static String[] generateMagicToken(String key, String source, int iorder) {
		if (key == null || key.length() != 16) {
			key = RandomUtils.generateRandom(16, RandomUtils.CHAR_HEX);
		}
		source = iorder % 2 == 0 ? source + key : key + source;
		String enmd5 = CoderUtils.enMD5(source);
		String token = enmd5.substring(8, 24);
		token = iorder % 2 == 1 ? token + key : key + token;
		return new String[] {key, token};
	}

	/**
	 * 根据IMSI生成魔幻凭证
	 * 
	 * @param source
	 * @param iorder
	 * @return {key, token}
	 */
	public static String[] generateMagicToken(String source, int iorder) {
		String key = RandomUtils.generateRandom(16, RandomUtils.CHAR_HEX);
		source = iorder % 2 == 0 ? source + key : key + source;
		String token = CoderUtils.enMD5(source).substring(8, 24);
		token = iorder % 2 == 1 ? token + key : key + token;
		return new String[] {key, token};
	}

	/**
	 * 校验查询魔幻凭证
	 * 
	 * @param source
	 * @param token
	 * @param iorder
	 * @return
	 */
	public static boolean checkMagicToken(String source, String token, int iorder) {
		if (source == null || token == null || token.length() != 32) {
			return false;
		}
		int ct = iorder % 2 == 1 ? 16 : 0;
		String key = token.substring(0 + ct, 16 + ct);
		source = iorder % 2 == 0 ? source + key : key + source;
		String tokens = CoderUtils.enMD5(source).substring(8, 24);
		tokens = iorder % 2 == 1 ? tokens + key : key + tokens;
		return token.equals(tokens);
	}

	/**
	 * RCS开户生成魔幻凭证
	 * 
	 * @param source
	 * @return {key, token}
	 */
	public static String[] generateTokenEnroll(String source) {
		int iorder = 1;
		return generateMagicToken(source, iorder);
	}

	/**
	 * RCS开户校验查询魔幻凭证
	 * 
	 * @param source
	 * @param token
	 * @return
	 */
	public static boolean checkMagicTokenEnroll(String source, String token) {
		int iorder = 1;
		return checkMagicToken(source, token, iorder);
	}

	/**
	 * 获取手机号码生成魔幻凭证
	 * 
	 * @param source
	 * @return {key, token}
	 */
	public static String[] generateTokenMobile(String source) {
		int iorder = 2;
		return generateMagicToken(source, iorder);
	}

	/**
	 * 获取手机号码校验查询魔幻凭证
	 * 
	 * @param source
	 * @param token
	 * @return
	 */
	public static boolean checkMagicTokenMobile(String source, String token) {
		int iorder = 2;
		return checkMagicToken(source, token, iorder);
	}

	/**
	 * 对明文密码加密
	 * 
	 * @param key
	 * @param source
	 * @return
	 */
	public static String encryptPassword(String key, String source) {
		return CoderUtils.enDES(key, source);
	}

	/**
	 * 对加密密码解密
	 * 
	 * @param key
	 * @param source
	 * @return
	 */
	public static String decryptPassword(String key, String source) {
		return CoderUtils.deDES(key, source);
	}

	/**
	 * 生成魔幻签名
	 * 
	 * @param key
	 * @param source
	 * @param indexs
	 * @return {key, sign}
	 */
	public static String[] generateSign(String key, String source, int[] indexs) {
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
		return new String[] {key, sign};
	}

	/**
	 * 解密魔幻签名
	 * 
	 * @param source
	 * @param indexs
	 * @return {key, mdxy}
	 */
	public static String[] decryptSign(String source, int[] indexs) {
		char[] encrypts = source.toCharArray();
		char[] keys = new char[indexs.length];
		char[] mdxys = new char[source.length() - indexs.length];
		int t = 0, a = 0, b = 0;
		for (int i = 0; i < indexs.length; i++) {
			b = indexs[i];
			System.arraycopy(encrypts, a + i, mdxys, a, b - a);
			keys[i] = encrypts[b + i];
			a = b;
			t = a + i + 1;
		}
		System.arraycopy(encrypts, t, mdxys, a, encrypts.length - t);
		String key = String.valueOf(keys);
		String mdxy = String.valueOf(mdxys);
		return new String[] {key, mdxy};
	}

	/**
	 * 生成用户账号
	 * 
	 * @return
	 */
	public static String generateOAuthUID() {
		long nano = System.currentTimeMillis();
		return String.valueOf(nano);
	}

	/**
	 * 生成魔幻签名
	 * 
	 * @param key
	 * @param source
	 * @return {key, sign}
	 */
	public static String[] generateSignOAuth(String key, String source) {
		int[] indexs = {4, 8, 12, 14, 18, 20, 24, 28};
		return generateSign(key, source, indexs);
	}

	/**
	 * 解密魔幻签名
	 * 
	 * @param source
	 * @return {key, mdxy}
	 */
	public static String[] decryptSignOAuth(String source) {
		int[] indexs = {4, 8, 12, 14, 18, 20, 24, 28};
		return decryptSign(source, indexs);
	}

	/**
	 * 校验认证鉴权Token
	 * 
	 * @param key
	 * @param token
	 * @param secret
	 * @return {key, sign}
	 */
	public static boolean checkTokenOAuth(String key, String token, String secret) {
		if (key == null || token == null || secret == null || token.length() != 32 || secret.length() != 36) {
			return false;
		}
		return secret.split("-")[0].equals(CoderUtils.deDES(key, token));
	}

	public static void main(String[] args) {
		String userid = generateOAuthUID();
		System.out.println(userid);
		// testOAuthSign();
		
		/*System.out.println("\n------------ [generateSign]\n");
		int[] indexs = {4, 8, 12, 14, 18, 20, 24, 28};
		String secret = "00000000";
		String key = "12345678";
		
		String token = CoderUtils.enDES(key, secret);
		System.out.println(token);
		
		String source = "{\"option\":0,\"mobile\":\"15989062608\",\"token\":\"" + token + "\"}"; // "{\"mobile\":\"13800000000\",\"enterpriseid\":1,\"deviceid\":\"460029890330189\",\"token\":\"" + token + "\"}";
		String[] signs = generateSign(key, source, indexs);
		System.out.println(signs[1]);
		System.out.println("\n------------\n");*/

		/*System.out.println("\n------------ [generateAuthSign]\n");
		String client_id = "7a7811ea-17cd-4a70-8c48-e08c881039ec";
		String client_key = "72ff7f4e-9b2e-4b33-8232-07cb0d08bbf3";
		String timestamp = "2011-08-23 11:40:39";
		String sign = generateAuthSign(client_id, client_key, timestamp);
		System.out.println("client_id: " +client_id+ " ; client_key: " +client_key+ " ; timestamp: " +timestamp+ "\n\nsign: " + sign);
		System.out.println("\n------------\n");*/

		/*System.out.println("\n------------ [DecryptPassword]\n");
		testDecryptPassword();
		System.out.println("\n------------\n");*/
		
		/*System.out.println("\n------------ [Password]\n");
		testPassword();
		System.out.println("\n------------\n");*/
		
		/*System.out.println("\n------------ [Decrypt]\n");
		testDecrypt();
		System.out.println("\n------------\n");*/
		
		/*System.out.println("\n------------ [Enroll]\n");
		testMyEnroll();
		System.out.println("\n------------\n");
		System.out.println("\n------------ [Mobile]\n");
		testMyMobile();
		System.out.println("\n------------\n");*/
		
		/*System.out.println("\n------------\n");
		testMyMagic();
		System.out.println("\n------------\n");
		testMyToken();
		System.out.println("\n------------\n");*/
		
		/*System.out.println("\n------------\n");
		testMagicEnroll();
		System.out.println("\n------------\n");
		testMagicMoblie();
		System.out.println("\n------------\n");
		testMagicTokenEnroll();
		System.out.println("\n------------\n");
		testMagicTokenMobile();
		System.out.println("\n------------\n");*/
	}

	public static void testOAuthSign() {
		String secret = "00000000";
		String key = "12345678";
		
		// 加密
		String token = CoderUtils.enDES(key, secret);
		String source = "{\"option\":0,\"mobile\":\"15989062608\",\"token\":\"" + token + "\"}"; // "{\"mobile\":\"13800000000\",\"enterpriseid\":1,\"deviceid\":\"460029890330189\",\"token\":\"" + token + "\"}";
		String[] ensigns = generateSignOAuth(key, source);
		
		// 解密
		String sign = ensigns[1];
		String[] designs = decryptSignOAuth(sign);
		String dekey = designs[0];
		String signs = generateSignOAuth(dekey, source)[1];
		if (sign.equals(signs)) {
			System.out.println("sign check ok");
		}
		String desecret = CoderUtils.deDES(dekey, token);
		
		System.out.println("secret: " + secret + "\nkey: " + key + "\n\n[-->]\ntoken: " + token + "\nsign: " + sign + "\n\nsource: " + source + "\ndesecret: " + desecret + "\n");
	}

	public static void testDecryptPassword() {
		String key = "15927583074";
		String password = "05E0C2BE24A810A0";
		
		String depwd = decryptPassword(key, password);
		System.out.println("key: " + key + "\npassword: " + password + "\n\n[-->]\ndepwd: " + depwd + "\n");
	}

	public static void testPassword() {
		String key = "15989062608";
		String source = "123456";
		String password = "F0D3560D23ABFD03";
		
		String enpwd = encryptPassword(key, source);
		String depwd = decryptPassword(key, password);
		System.out.println("key: " + key + "\nsource: " + source + "\n\n[-->]\nenpwd: " + enpwd + "\ndepwd: " + depwd + "\n");
	}

	public static void testDecrypt() {
		String source = "9FA1D60AD7AF0E1003562FE83DC36CB11BD0AADB";
		String[] demagics = decryptMagicEnroll(source);
		System.out.println("source: " + source + "\n\n[-->]\nkey: " + demagics[0] + "\nimsi: " + demagics[1] + "\n");
	}

	public static String testMyEnroll() {
		int[] indexs = {4, 8, 12, 14, 18, 20, 24, 28}; // 开户平台开户
		int iorder = 1; // 开户结果获取
		String keyset = "12345678";
		String keyget = "0123456789ABCDEF";
		String imsi = "460002508161264";

		String[] enmagics = generateMagic(keyset, imsi, indexs);
		String[] magics = generateMagicToken(keyget, imsi, iorder);
		
		String[] demagics = decryptMagicEnroll(enmagics[1]);
		boolean checkmagic = keyset.equals(demagics[0]) && imsi.equals(demagics[1]);
		boolean checktoken = checkMagicTokenEnroll(imsi, magics[1]);
		
		System.out.println("imsi: " + imsi + "\n\n[SET]\nkey: " + enmagics[0] + "\nmagic: " + enmagics[1] + "\n\n[GET]\nkey: " + magics[0] + "\ntoken: " + magics[1] + "\n\n[CHK]\ncheckmagic: " + checkmagic + "\nchecktoken: " + checktoken + "\n");
		
		String connectset = "%1$s@KTRCS";
		System.out.println("开户短信内容: " + String.format(connectset, enmagics[1]));
		String connectget = "{\"enroll\":{\"option\":0,\"imsi\":\"%1$s\",\"token\":\"%2$s\"}}";
		String connects = String.format(connectget, imsi, magics[1]);
		System.out.println("获取开户结果: " + connects);
		return connects;
	}

	public static String testMyMobile() {
		int[] indexs = {4, 8, 12, 15, 17, 20, 24, 28}; // 手机号码保存
		int iorder = 2; // 手机号码获取
		String keyset = "12345678";
		String keyget = "0123456789ABCDEF";
		String imsi = "460002508161264";

		String[] enmagics = generateMagic(keyset, imsi, indexs);
		String[] magics = generateMagicToken(keyget, imsi, iorder);

		String[] demagics = decryptMagicMobile(enmagics[1]);
		boolean checkmagic = keyset.equals(demagics[0]) && imsi.equals(demagics[1]);
		boolean checktoken = checkMagicTokenMobile(imsi, magics[1]);
		
		System.out.println("imsi: " + imsi + "\n\n[SET]\nkey: " + enmagics[0] + "\nmagic: " + enmagics[1] + "\n\n[GET]\nkey: " + magics[0] + "\ntoken: " + magics[1] + "\n\n[CHK]\ncheckmagic: " + checkmagic + "\nchecktoken: " + checktoken + "\n");
		
		String connectset = "%1$s@SJHM";
		System.out.println("保存号码短信: " + String.format(connectset, enmagics[1]));
		String connectget = "{\"hqsjhm\":{\"option\":0,\"imsi\":\"%1$s\",\"token\":\"%2$s\"}}";
		String connects = String.format(connectget, imsi, magics[1]);
		System.out.println("获取号码结果: " + connects);
		return connects;
	}

	public static void testMyMagic() {
		// int[] indexs = {4, 8, 12, 14, 18, 20, 24, 28}; // 开户平台开户
		int[] indexs = {4, 8, 12, 15, 17, 20, 24, 28}; // 手机号码保存
		String key = "12345678";
		String source = "460029890330189";

		String[] enmagics = generateMagic(key, source, indexs);
		String[] demagics = decryptMagic(enmagics[1], indexs);
		System.out.println(source + "\n\n" + enmagics[0] + "\n" + enmagics[1] + "\n\n" + demagics[0] + "\n" + demagics[1]);
	}

	public static void testMyToken() {
		// int iorder = 1; // 开户结果获取
		int iorder = 2; // 手机号码获取
		String key = "0123456789ABCDEF";
		String source = "460029890330189";

		String[] magics = generateMagicToken(key, source, iorder);
		boolean checkok = checkMagicToken(source, magics[1], iorder);
		
		System.out.println(source + "\n\nkey: " + magics[0] + " ; token: " + magics[1] + " ;\n\n" + checkok);
	}

	public static void testMagicEnroll() {
		String source = "460002508161264";
		String[] enmagics = generateMagicEnroll(source);
		String[] demagics = decryptMagicEnroll(enmagics[1]);
		System.out.println(source + "\n\n" + enmagics[0] + "\n" + enmagics[1] + "\n\n" + demagics[0] + "\n" + demagics[1]);
	}

	public static void testMagicMoblie() {
		String source = "460002508161264";
		String[] enmagics = generateMagicMobile(source);
		String[] demagics = decryptMagicMobile(enmagics[1]);
		System.out.println(source + "\n\n" + enmagics[0] + "\n" + enmagics[1] + "\n\n" + demagics[0] + "\n" + demagics[1]);
	}

	public static void testMagicTokenEnroll() {
		String source = "460002508161264";
		String[] magics = generateTokenEnroll(source);
		boolean checkok = checkMagicTokenEnroll(source, magics[1]);
		
		System.out.println(source + "\n\nkey: " + magics[0] + " ; token: " + magics[1] + " ;\n\n" + checkok);
	}

	public static void testMagicTokenMobile() {
		String source = "460002508161264";
		String[] magics = generateTokenMobile(source);
		boolean checkok = checkMagicTokenMobile(source, magics[1]);
		
		System.out.println(source + "\n\nkey: " + magics[0] + " ; token: " + magics[1] + " ;\n\n" + checkok);
	}

}
