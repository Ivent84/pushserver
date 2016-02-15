package util;
import java.util.Random;
import java.util.UUID;

/**
 * <p>Title: iSoftStone</p>
 * <p>Description: </p>
 * 随机生成工具类
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: iSoftStone</p>
 *
 * @author Lilibo
 * @version 1.0
 */
public class RandomUtils {

	/**
	 * 所有, 包含所有数字和大小写字母
	 */
	public static final int CHAR_ALL = 1;
	/**
	 * 完整, 包含所有数字和字母
	 */
	public static final int CHAR_INTACT = 2;
	/**
	 * 字母, 包含所有字母
	 */
	public static final int CHAR_LETTER = 3;
	/**
	 * 数字, 包含所有数字
	 */
	public static final int CHAR_NUMBER = 4;
	/**
	 * HEX, 十六进制数字和字母
	 */
	public static final int CHAR_HEX = 5;


	private static final String allChar = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String intactChar = "0123456789abcdefghijklmnopqrstuvwxyz";
	private static final String letterChar = "abcdefghijklmnopqrstuvwxyz";
	private static final String numberChar = "0123456789";
	private static final String hexChar = "0123456789ABCDEF";

	/**
	 * 生成UUID字符串，去除"-"
	 * 
	 * @return UUID
	 */
	public static String generateUUID() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	/**
	 * 随机生成字符串, 大小写混合
	 * 
	 * @param length 生成的长度
	 * @return 随机字符串
	 */
	public static String generateRandom(int length) {
		StringBuffer strbuf = new StringBuffer();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			strbuf.append(allChar.charAt(random.nextInt(allChar.length())));
		}
		return strbuf.toString();
	}

	/**
	 * 随机生成字符串, 根据类型输出
	 * 
	 * @param length 生成的长度
	 * @param type 生成的类型
	 * @return 随机字符串
	 */
	public static String generateRandom(int length, int type) {
		String chars = allChar;
		switch (type) {
		case CHAR_ALL:
			chars = allChar;
			break;
		case CHAR_INTACT:
			chars = intactChar;
			break;
		case CHAR_LETTER:
			chars = letterChar;
			break;
		case CHAR_NUMBER:
			chars = numberChar;
			break;
		case CHAR_HEX:
			chars = hexChar;
			break;
		default:
			chars = allChar;
			break;
		}
		StringBuffer strbuf = new StringBuffer();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			strbuf.append(chars.charAt(random.nextInt(chars.length())));
		}
		return strbuf.toString();
	}

}
