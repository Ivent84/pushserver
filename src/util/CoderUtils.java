package util;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import config.Config;

/**
 * <p>Title: iSoftStone</p>
 * <p>Description: </p>
 * 加密解密工具类
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: iSoftStone</p>
 *
 * @author Lilibo
 * @version 1.0
 */
public class CoderUtils {

	/**
	 * 信息摘要算法 MD5 (非对称)
	 * </br></br>
	 * <p>让大容量信息在用数字签名软件签署私人密匙前被"压缩"成一种保密的格式。</p>
	 */
	public static final String KEY_MD5 = "MD5";

	/**
	 * 散列算法 SHA (非对称)
	 * </br></br>
	 * <p>散列算法，散列是信息的提炼，通常其长度要比信息小得多，且为一个固定长度。</p>
	 */
	public static final String KEY_SHA = "SHA";

	/**
	 * 数据编码BASE64 (对称)
	 * </br></br>
	 * <p>采用Base64编码不仅比较简短，同时也具有不可读性，即所编码的数据不会被人用肉眼所直接看到。</p>
	 */
	public static final String KEY_BASE64 = "BASE64";

	/**
	 * 数据加密算法 DES (对称)
	 * </br></br>
	 * <p>DES是21世纪的加密标准，现新一代加密标准是AES。</p>
	 */
	public static final String KEY_DES = "DES";

	private static final char C_0 = '0'; // 字符0

	/**
	 * BASE64加密
	 * 
	 * @param data 源加密数据
	 * @return BASE64加密数据
	 */
	public static byte[] encryptBASE64(byte[] data) {
		if (data == null) {
			return null;
		}
		return Base64Utils.encode(data).getBytes();
	}

	/**
	 * BASE64加密
	 * 
	 * @param data 源加密数据
	 * @return BASE64加密串
	 */
	public static String enBASE64(byte[] data) {
		if (data == null) {
			return null;
		}
		return Base64Utils.encode(data);
	}

	/**
	 * BASE64加密
	 * 
	 * @param source 源字符串
	 * @return BASE64加密串
	 */
	public static String enBASE64(String source) {
		if (source == null) {
			return null;
		}
		return Base64Utils.encode(source.getBytes());
	}

	/**
	 * BASE64加密
	 * 
	 * @param source 源字符串
	 * @param charset 编码格式
	 * @return BASE64加密串
	 */
	public static String enBASE64(String source, String charset) {
		if (source == null || charset == null) {
			return null;
		}
		return Base64Utils.encode(source.getBytes(Charset.forName(charset)));
	}

	/**
	 * BASE64解密
	 * 
	 * @param source 需要解密的字符串
	 * @return BASE64解密后的数据
	 */
	public static byte[] decryptBASE64(String source) {
		if (source == null) {
			return null;
		}
		return Base64Utils.decode(source);
	}

	/**
	 * BASE64解密
	 * 
	 * @param source 需要解密的字符串
	 * @return BASE64解密后的字符串
	 */
	public static String deBASE64(String source) {
		if (source == null) {
			return null;
		}
		return new String(Base64Utils.decode(source));
	}

	/**
	 * MD5加密
	 * 
	 * @param data 源加密数据
	 * @return MD5加密后的字符串 (32位)
	 */
	public static byte[] encryptMD5(byte[] data) {
		if (data == null) {
			return null;
		}
		try {
			MessageDigest md5 = MessageDigest.getInstance(KEY_MD5);
			md5.update(data);
			return md5.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * MD5加密
	 * 
	 * @param data 源加密数据
	 * @return MD5加密后的字符串 (32位)
	 */
	public static String enMD5(byte[] data) {
		if (data == null) {
			 return null;
		}
		return byte2hex(encryptMD5(data));
	}

	/**
	 * MD5加密
	 * 
	 * @param source 源加密字符串（默认UTF-8编码）
	 * @return MD5加密后的字符串 (32位)
	 */
	public static String enMD5(String source) {
		if (source == null) {
			 return null;
		}
		return byte2hex(encryptMD5(source.getBytes(Charset.forName(Config.getValue("charset")))));
	}

	/**
	 * MD5加密
	 * 
	 * @param source 源加密字符串
	 * @param charset 编码格式
	 * @return MD5加密后的字符串 (32位)
	 */
	public static String enMD5(String source, String charset) {
		if (source == null || charset == null) {
			 return null;
		}
		return byte2hex(encryptMD5(source.getBytes(Charset.forName(charset))));
	}

	/**
	 * 多次MD5加密
	 * 
	 * @param source 源加密字符串
	 * @param n 加密次数
	 * @return MD5加密n次后的字符串 (32位)
	 */
	public static String encryptMD5(String source, int n) {
		try {
			for (int i = 0; i < n; i++) {
				source = enMD5(source);
			}
			return source;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * SHA加密
	 * 
	 * @param data 源加密数据
	 * @return SHA加密后的数据
	 */
	public static byte[] encryptSHA(byte[] data) {
		if (data == null) {
			return null;
		}
		try {
			MessageDigest sha = MessageDigest.getInstance(KEY_SHA);
			sha.update(data);
			return sha.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * SHA加密
	 * 
	 * @param data 源加密数据
	 * @return SHA加密后的字符串 (40位)
	 */
	public static String enSHA(byte[] data) {
		if (data == null) {
			 return null;
		}
		return byte2hex(encryptSHA(data));
	}

	/**
	 * SHA加密
	 * 
	 * @param source 源加密字符串
	 * @return SHA加密后的字符串 (40位)
	 */
	public static String enSHA(String source) {
		if (source == null) {
			 return null;
		}
		return byte2hex(encryptSHA(source.getBytes()));
	}

	/**
	 * SHA加密
	 * 
	 * @param source 源加密字符串
	 * @param charset 编码格式
	 * @return SHA加密后的字符串 (40位)
	 */
	public static String enSHA(String source, String charset) {
		if (source == null || charset == null) {
			 return null;
		}
		return byte2hex(encryptSHA(source.getBytes(Charset.forName(charset))));
	}

	/**
	 * DES加密
	 * 
	 * @param key DES密钥
	 * @param data 源加密数据
	 * @return DES加密后的数据
	 */
	public static byte[] encryptDES(byte[] key, byte[] data) {
		if (key == null || data == null) {
			return null;
		}
		try {
			DESKeySpec deskey = new DESKeySpec(key);
			SecretKeyFactory keyfactory = SecretKeyFactory.getInstance(KEY_DES);
			SecretKey securekey = keyfactory.generateSecret(deskey);
			Cipher cipher = Cipher.getInstance(KEY_DES);
			SecureRandom random = new SecureRandom();
			cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
			return cipher.doFinal(data);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * DES加密
	 * 
	 * @param key DES密钥
	 * @param data 源加密数据
	 * @return DES加密后的数据
	 */
	public static byte[] encryptDES(String key, byte[] data) {
		if (key == null || data == null) {
			return null;
		}
		return encryptDES(encryptMD5(key.getBytes()), data);
	}

	/**
	 * DES加密
	 * 
	 * @param key DES密钥
	 * @param source 源加密字符串
	 * @return DES加密后的数据
	 */
	public static byte[] encryptDES(String key, String source) {
		if (key == null || source == null) {
			return null;
		}
		return encryptDES(encryptMD5(key.getBytes()), source.getBytes());
	}

	/**
	 * DES加密
	 * 
	 * @param key DES密钥
	 * @param source 源加密字符串
	 * @param charset 源加密字符串编码格式
	 * @return DES加密后的数据
	 */
	public static byte[] encryptDES(String key, String source, String charset) {
		if (key == null || source == null) {
			return null;
		}
		return encryptDES(encryptMD5(key.getBytes()), source.getBytes(Charset.forName(charset)));
	}

	/**
	 * DES加密
	 * 
	 * @param key DES密钥
	 * @param data 源加密数据
	 * @return DES加密后的字符串
	 */
	public static String enDES(byte[] key, byte[] data) {
		if (key == null || data == null) {
			return null;
		}
		return byte2hex(encryptDES(key, data));
	}

	/**
	 * DES加密
	 * 
	 * @param key DES密钥
	 * @param data 源加密数据
	 * @return DES加密后的字符串
	 */
	public static String enDES(String key, byte[] data) {
		if (key == null || data == null) {
			return null;
		}
		return byte2hex(encryptDES(encryptMD5(key.getBytes()), data));
	}

	/**
	 * DES加密
	 * 
	 * @param key DES密钥
	 * @param source 源加密字符串
	 * @return DES加密后的字符串
	 */
	public static String enDES(String key, String source) {
		if (key == null || source == null) {
			return null;
		}
		return byte2hex(encryptDES(encryptMD5(key.getBytes()), source.getBytes()));
	}

	/**
	 * DES加密
	 * 
	 * @param key DES密钥
	 * @param source 源加密字符串
	 * @param charset 源加密字符串编码格式
	 * @return DES加密后的字符串
	 */
	public static String enDES(String key, String source, String charset) {
		if (key == null || source == null) {
			return null;
		}
		return byte2hex(encryptDES(encryptMD5(key.getBytes()), source.getBytes(Charset.forName(charset))));
	}

	/**
	 * DES解密
	 * 
	 * @param key DES密钥
	 * @param data 源解密数据
	 * @return DES解密后的数据
	 */
	public static byte[] decryptDES(byte[] key, byte[] data) {
		if (key == null || data == null) {
			return null;
		}
		try {
			DESKeySpec deskey = new DESKeySpec(key);
			SecretKeyFactory keyfactory = SecretKeyFactory.getInstance(KEY_DES);
			SecretKey securekey = keyfactory.generateSecret(deskey);
			Cipher cipher = Cipher.getInstance(KEY_DES);
			SecureRandom random = new SecureRandom();
			cipher.init(Cipher.DECRYPT_MODE, securekey, random);
			return cipher.doFinal(data);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * DES解密
	 * 
	 * @param key DES密钥
	 * @param data 源解密数据
	 * @return DES解密后的数据
	 */
	public static byte[] decryptDES(String key, byte[] data) {
		if (key == null || data == null) {
			return null;
		}
		return decryptDES(encryptMD5(key.getBytes()), data);
	}

	/**
	 * DES解密
	 * 
	 * @param key DES密钥
	 * @param source 源解密字符串
	 * @return DES解密后的数据
	 */
	public static byte[] decryptDES(String key, String source) {
		if (key == null || source == null) {
			return null;
		}
		return decryptDES(encryptMD5(key.getBytes()), hex2byte(source));
	}

	/**
	 * DES解密
	 * 
	 * @param key DES密钥
	 * @param data 源解密数据
	 * @return DES解密后的字符串
	 */
	public static String deDES(byte[] key, byte[] data) {
		if (key == null || data == null) {
			return null;
		}
		return new String(decryptDES(key, data));
	}

	/**
	 * DES解密
	 * 
	 * @param key DES密钥
	 * @param data 源解密数据
	 * @return DES解密后的字符串
	 */
	public static String deDES(String key, byte[] data) {
		if (key == null || data == null) {
			return null;
		}
		return new String(decryptDES(encryptMD5(key.getBytes()), data));
	}

	/**
	 * DES解密
	 * 
	 * @param key DES密钥
	 * @param source 源解密字符串
	 * @return DES解密后的字符串
	 */
	public static String deDES(String key, String source) {
		if (key == null || source == null) {
			return null;
		}
		return new String(decryptDES(encryptMD5(key.getBytes()), hex2byte(source)));
	}

	/**
	 * 二进制转哈希码
	 * 
	 * @param data 源二进制数据
	 * @return 哈希码
	 */
	private static String byte2hex(byte[] data) {
		if (data == null) {
			return null;
		}
		StringBuilder hs = new StringBuilder();
		String stmp;
		for (int n = 0; n < data.length; n++) {
			stmp = Integer.toHexString(data[n] & 0XFF);
			if (stmp.length() == 1) {
				hs.append(C_0);
			}
			hs.append(stmp);
		}
		return hs.toString().toUpperCase();
	}

	/**
	 * 哈希码转二进制
	 * 
	 * @param hex 源哈希码字符串
	 * @return 二进制数据
	 */
	private static byte[] hex2byte(String hex) {
		if (hex == null) {
			return null;
		}
		byte[] bs = hex.getBytes();
		if ((bs.length % 2) != 0) {
			return null;
		}
		byte[] bh = new byte[bs.length / 2];
		for (int n = 0; n < bs.length; n += 2) {
			bh[n / 2] = (byte) Integer.parseInt(new String(bs, n, 2), 16);
		}
		return bh;
	}


	public static void main(String[] args) throws Exception {
		String key = "753aaa1380288163313802881633aa138053313802881633";
		String source = "460002508161264";
		System.out.println(CoderUtils.deDES(key, source));
	}
}