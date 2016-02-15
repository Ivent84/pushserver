package util;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

/**
 * <p>Title: iSoftStone</p>
 * <p>Description: </p>
 * HTTP访问工具
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: iSoftStone</p>
 *
 * @author Lilibo
 * @version 1.0
 */
public class HTTPUtils {

	public static String httpRequestEncoding = "UTF-8"; // HTTP请求编码方式
	public static String httpResponseEncoding = "UTF-8"; // HTTP响应编码方式
	public static int httpConnectTimeout = 60; // 连接超时(秒)
	public static int httpReadTimeout = 60; // 获取数据超时(秒)
	public static int httpRetryCount = 1; // 错误时重试次数

	private static int tryHttpDoGet = 0; // HTTP Get 请求重试记录
	private static int tryHttpsDoGet = 0; // HTTPS Get 请求重试记录
	private static int tryHttpDoPost = 0; // HTTP Post 请求重试记录
	private static int tryHttpsDoPost = 0; // HTTPS Post 请求重试记录
	private static int tryHttpSend = 0; // HTTP Send 请求重试记录
	private static int tryHttpsSend = 0; // HTTPS Send 请求重试记录

	private static boolean isInitSSL = false; // HTTPS连接初始化SSL环境标识
	private static SSLSocketFactory httpsSSLSocketFactory = null; // HTTPS SSL环境工厂
	private static HostnameVerifier httpsHostnameVerifier = null; // HTTPS 主机验证处理
	private static final Log log = LogFactory.getLog(HTTPUtils.class);

	/**
	 * 初始化HTTPS环境
	 */
	static {
		initSSL();
	}

	/**
	 * HTTP GET请求
	 * 
	 * @param urlstr 请求URL
	 * @return 返回内容
	 */
	public static String httpDoGet(String urlstr) {
		return httpDoGet(urlstr, null, httpConnectTimeout, httpReadTimeout, httpResponseEncoding, httpRetryCount);
	}

	/**
	 * HTTP GET请求
	 * 
	 * @param urlstr 请求URL
	 * @param resencoding 响应的编码方式
	 * @return 返回内容
	 */
	public static String httpDoGet(String urlstr, String resencoding) {
		return httpDoGet(urlstr, null, httpConnectTimeout, httpReadTimeout, resencoding, httpRetryCount);
	}

	/**
	 * HTTP GET请求
	 * 
	 * @param urlstr 请求URL
	 * @param connecttimeout 连接超时时间(秒)
	 * @param readtimeout 读取数据超时时间(秒)
	 * @param resencoding 响应的编码方式
	 * @param retrycount 失败时重试次数
	 * @return 返回内容
	 */
	public static String httpDoGet(String urlstr, Map<String, String> header, int connecttimeout, int readtimeout, String resencoding, int retrycount) {
		System.out.println("------------ [Start] HTTP DoGet URL : " + urlstr);
		long t1 = System.currentTimeMillis();
		String response = null;
		InputStreamReader resinput = null;
		HttpURLConnection httpurlconn = null;
		try {
			URL url = new URL(urlstr);
			httpurlconn = (HttpURLConnection) url.openConnection();
			httpurlconn.setConnectTimeout(1000 * (connecttimeout < 0 ? httpConnectTimeout : connecttimeout));
			httpurlconn.setReadTimeout(1000 * (readtimeout < 0 ? httpReadTimeout : readtimeout));
			httpurlconn.setRequestMethod("GET");
			if (null != header) {
				for (String header_key : header.keySet()) {
					httpurlconn.setRequestProperty(header_key, header.get(header_key));
				}
			}

			int icode = httpurlconn.getResponseCode();
			System.out.println("HTTP Response Code : " + icode + " (" + httpurlconn.getResponseMessage() + ") ");
			if (icode == HttpURLConnection.HTTP_OK) {
				int length = httpurlconn.getContentLength();
				if (length <= 0) {
					return null;
				}
				resinput = new InputStreamReader(httpurlconn.getInputStream(), (resencoding == null || "".equals(resencoding.trim()) ? httpResponseEncoding : resencoding.trim()));
				StringBuffer strbuf = new StringBuffer(length);
				char[] charbuff = new char[length];
		        int i;
		        while ((i = resinput.read(charbuff, 0, length - 1)) != -1) {
		        	strbuf.append(charbuff, 0, i);
		        }
		        response = strbuf.toString();
		        tryHttpDoGet = 0;
				System.out.println("HTTP Response Content : \n------------ (" + length + ") \n" + response + "\n------------");
			} else {
				tryHttpDoGet++;
				if (tryHttpDoGet <= (retrycount < 0 ? httpRetryCount : retrycount)) {
					System.out.println("------------ [Retry] HTTP DoGet TryCount : " + tryHttpDoGet);
					return httpDoGet(urlstr, header, connecttimeout, readtimeout, resencoding, retrycount);
				} else {
					return null;
				}
			}
		} catch (Exception e) {
			System.out.println("HTTP DoGet Exception " + e);
			tryHttpDoGet++;
			if (tryHttpDoGet <= (retrycount < 0 ? httpRetryCount : retrycount)) {
				System.out.println("------------ [Retry] HTTP DoGet TryCount : " + tryHttpDoGet);
				return httpDoGet(urlstr, header, connecttimeout, readtimeout, resencoding, retrycount);
			} else {
				return null;
			}
		} finally {
			if (null != resinput) {
				try {
					resinput.close();
				} catch (Exception e) {
					System.out.println("HTTP DoGet InputStreamReader Close Exception " + e);
				}
			}
			if (null != httpurlconn) {
				try {
					httpurlconn.disconnect();
				} catch (Exception e) {
					System.out.println("HTTP DoGet HttpURLConnection Close Exception " + e);
				}
			}
			long t2 = System.currentTimeMillis();
			System.out.println("------------ [End] HTTP DoGet ( " + (t2 - t1) + " ms ) ");
		}
		return response;
	}

	/**
	 * HTTP POST请求
	 * 
	 * @param urlstr 请求URL
	 * @param params 请求参数
	 * @return 返回内容
	 */
	public static String httpDoPost(String urlstr, Map<String, String> params) {
		return httpDoPost(urlstr, null, params, httpConnectTimeout, httpReadTimeout, httpRequestEncoding, httpResponseEncoding, httpRetryCount);
	}

	/**
	 * HTTP POST请求
	 * 
	 * @param urlstr 请求URL
	 * @param params 请求参数
	 * @param reqencoding 请求的编码方式
	 * @param resencoding 响应的编码方式
	 * @return 返回内容
	 */
	public static String httpDoPost(String urlstr, Map<String, String> params, String reqencoding, String resencoding) {
		return httpDoPost(urlstr, null, params, httpConnectTimeout, httpReadTimeout, reqencoding, resencoding, httpRetryCount);
	}

	/**
	 * HTTP POST请求
	 * 
	 * @param urlstr 请求URL
	 * @param header 请求参数
	 * @param params 请求参数
	 * @param reqencoding 请求的编码方式
	 * @param resencoding 响应的编码方式
	 * @return 返回内容
	 */
	public static String httpDoPost(String urlstr, Map<String, String> header, Map<String, String> params, String reqencoding, String resencoding) {
		return httpDoPost(urlstr, header, params, httpConnectTimeout, httpReadTimeout, reqencoding, resencoding, httpRetryCount);
	}

	/**
	 * HTTP POST请求
	 * 
	 * @param urlstr 请求URL
	 * @param header 头部参数
	 * @param params 请求参数
	 * @param connecttimeout 连接超时时间(秒)
	 * @param readtimeout 读取数据超时时间(秒)
	 * @param reqencoding 请求的编码方式
	 * @param resencoding 响应的编码方式
	 * @param retrycount 失败时重试次数
	 * @return 返回内容
	 */
	public static String httpDoPost(String urlstr, Map<String, String> header, Map<String, String> params, int connecttimeout, int readtimeout, String reqencoding, String resencoding, int retrycount) {
		System.out.println("------------ [Start] HTTP DoPost URL : " + urlstr);
		long t1 = System.currentTimeMillis();
		String response = null;
		InputStreamReader resinput = null;
		HttpURLConnection httpurlconn = null;
		try {
			URL url = new URL(urlstr);
			httpurlconn = (HttpURLConnection) url.openConnection();
			httpurlconn.setConnectTimeout(1000 * (connecttimeout < 0 ? httpConnectTimeout : connecttimeout));
			httpurlconn.setReadTimeout(1000 * (readtimeout < 0 ? httpReadTimeout : readtimeout));
			httpurlconn.setRequestMethod("POST");
			httpurlconn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=" + (reqencoding == null || "".equals(reqencoding.trim()) ? httpRequestEncoding : reqencoding.trim()));
			httpurlconn.setDoInput(true);
			httpurlconn.setDoOutput(true);
			if (null != header) {
				for (String header_key : header.keySet()) {
					httpurlconn.setRequestProperty(header_key, header.get(header_key));
				}
			}
			if (null != params) {
				StringBuffer strbuf = new StringBuffer();
				for (String key : params.keySet()) {
					strbuf.append(key).append("=").append(params.get(key)).append("&");
				}
				String postparams = strbuf.toString();
				postparams = postparams.substring(0, postparams.length() -1);
				System.out.println("HTTP Ruquest Params : " + postparams);
				byte[] data = postparams.getBytes(reqencoding);
				int length = data.length;
				httpurlconn.setRequestProperty("Content-Length", String.valueOf(length));
				if (length != 0) {
					OutputStream resoutput = null;
					try {
						resoutput = httpurlconn.getOutputStream();
						resoutput.write(data);
						resoutput.flush();
					} catch (Exception e) {
						System.out.println("HTTP Ruquest Params Exception " + e);
					} finally {
						if (null != resoutput) {
							try {
								resoutput.close();
							} catch (Exception e) {
								System.out.println("HTTP Ruquest Params OutputStreamWriter Close Exception " + e);
							}
						}
					}
				}
			}

			int icode = httpurlconn.getResponseCode();
			System.out.println("HTTP Response Code : " + icode + " (" + httpurlconn.getResponseMessage() + ") ");
			if (icode == HttpURLConnection.HTTP_OK) {
				int length = httpurlconn.getContentLength();
				if (length <= 0) {
					return null;
				}
				resinput = new InputStreamReader(httpurlconn.getInputStream(), (resencoding == null || "".equals(resencoding.trim()) ? httpResponseEncoding : resencoding.trim()));
				StringBuffer strbuf = new StringBuffer(length);
				char[] charbuff = new char[length];
		        int i;
		        while ((i = resinput.read(charbuff, 0, length - 1)) != -1) {
		        	strbuf.append(charbuff, 0, i);
		        }
		        response = strbuf.toString();
		        tryHttpDoPost = 0;
				System.out.println("HTTP Response Content : \n------------ (" + length + ") \n" + response + "\n------------");
			} else {
				tryHttpDoPost++;
				if (tryHttpDoPost <= (retrycount < 0 ? httpRetryCount : retrycount)) {
					System.out.println("------------ [Retry] HTTP DoPost TryCount : " + tryHttpDoPost);
					return httpDoPost(urlstr, header, params, connecttimeout, readtimeout, reqencoding, resencoding, retrycount);
				} else {
					return null;
				}
			}
		} catch (Exception e) {
			System.out.println("HTTP DoPost Exception " + e);
			tryHttpDoPost++;
			if (tryHttpDoPost <= (retrycount < 0 ? httpRetryCount : retrycount)) {
				System.out.println("------------ [Retry] HTTP DoPost TryCount : " + tryHttpDoPost);
				return httpDoPost(urlstr, header, params, connecttimeout, readtimeout, reqencoding, resencoding, retrycount);
			} else {
				return null;
			}
		} finally {
			if (null != resinput) {
				try {
					resinput.close();
				} catch (Exception e) {
					System.out.println("HTTP DoPost InputStreamReader Close Exception " + e);
				}
			}
			if (null != httpurlconn) {
				try {
					httpurlconn.disconnect();
				} catch (Exception e) {
					System.out.println("HTTP DoPost HttpURLConnection Close Exception " + e);
				}
			}
			long t2 = System.currentTimeMillis();
			System.out.println("------------ [End] HTTP DoPost ( " + (t2 - t1) + " ms ) ");
		}
		return response;
	}

	/**
	 * HTTP Send请求
	 * 
	 * @param urlstr 请求URL
	 * @param content 请求数据
	 * @return 返回内容
	 */
	public static String httpSend(String urlstr, String content) {
		return httpSend(urlstr, null, content, httpConnectTimeout, httpReadTimeout, httpRequestEncoding, httpResponseEncoding, httpRetryCount);
	}

	/**
	 * HTTP Send请求
	 * 
	 * @param urlstr 请求URL
	 * @param content 请求数据
	 * @param reqencoding 请求的编码方式
	 * @param resencoding 响应的编码方式
	 * @return 返回内容
	 */
	public static String httpSend(String urlstr, String content, String reqencoding, String resencoding) {
		return httpSend(urlstr, null, content, httpConnectTimeout, httpReadTimeout, reqencoding, resencoding, httpRetryCount);
	}

	/**
	 * HTTP Send请求
	 * 
	 * @param urlstr 请求URL
	 * @param header 请求参数
	 * @param content 请求数据
	 * @param reqencoding 请求的编码方式
	 * @param resencoding 响应的编码方式
	 * @return 返回内容
	 */
	public static String httpSend(String urlstr, Map<String, String> header, String content, String reqencoding, String resencoding) {
		return httpSend(urlstr, header, content, httpConnectTimeout, httpReadTimeout, reqencoding, resencoding, httpRetryCount);
	}

	/**
	 * HTTP Send请求
	 * 
	 * @param urlstr 请求URL
	 * @param header 头部参数
	 * @param content 请求数据
	 * @param connecttimeout 连接超时时间(秒)
	 * @param readtimeout 读取数据超时时间(秒)
	 * @param reqencoding 请求的编码方式
	 * @param resencoding 响应的编码方式
	 * @param retrycount 失败时重试次数
	 * @return 返回内容
	 */
	public static String httpSend(String urlstr, Map<String, String> header, String content, int connecttimeout, int readtimeout, String reqencoding, String resencoding, int retrycount) {
		System.out.println("------------ [Start] HTTP Send URL : " + urlstr);
		long t1 = System.currentTimeMillis();
		String response = null;
		InputStreamReader resinput = null;
		HttpURLConnection httpurlconn = null;
		try {
			URL url = new URL(urlstr);
			httpurlconn = (HttpURLConnection) url.openConnection();
			httpurlconn.setConnectTimeout(1000 * (connecttimeout < 0 ? httpConnectTimeout : connecttimeout));
			httpurlconn.setReadTimeout(1000 * (readtimeout < 0 ? httpReadTimeout : readtimeout));
			httpurlconn.setRequestMethod("POST");
			httpurlconn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=" + (reqencoding == null || "".equals(reqencoding.trim()) ? httpRequestEncoding : reqencoding.trim()));
			httpurlconn.setDoInput(true);
			httpurlconn.setDoOutput(true);
			if (null != header) {
				for (String header_key : header.keySet()) {
					httpurlconn.setRequestProperty(header_key, header.get(header_key));
				}
			}
			if (null != content) {
				System.out.println("HTTP Ruquest Content : " + content);
				byte[] data = content.getBytes(reqencoding);
				int length = data.length;
				httpurlconn.setRequestProperty("Content-Length", String.valueOf(length));
				if (length != 0) {
					OutputStream resoutput = null;
					try {
						resoutput = httpurlconn.getOutputStream();
						resoutput.write(data);
						resoutput.flush();
					} catch (Exception e) {
						System.out.println("HTTP Ruquest Params Exception " + e);
					} finally {
						if (null != resoutput) {
							try {
								resoutput.close();
							} catch (Exception e) {
								System.out.println("HTTP Ruquest Params OutputStreamWriter Close Exception " + e);
							}
						}
					}
				}
			}

			int icode = httpurlconn.getResponseCode();
			System.out.println("HTTP Response Code : " + icode + " (" + httpurlconn.getResponseMessage() + ") ");
			if (icode == HttpURLConnection.HTTP_OK) {
				int length = httpurlconn.getContentLength();
				if (length <= 0) {
					return null;
				}
				resinput = new InputStreamReader(httpurlconn.getInputStream(), (resencoding == null || "".equals(resencoding.trim()) ? httpResponseEncoding : resencoding.trim()));
				StringBuffer strbuf = new StringBuffer(length);
				char[] charbuff = new char[length];
		        int i;
		        while ((i = resinput.read(charbuff, 0, length - 1)) != -1) {
		        	strbuf.append(charbuff, 0, i);
		        }
		        response = strbuf.toString();
		        tryHttpSend = 0;
				System.out.println("HTTP Response Content : \n------------ (" + length + ") \n" + response + "\n------------");
			} else {
				tryHttpSend++;
				if (tryHttpSend <= (retrycount < 0 ? httpRetryCount : retrycount)) {
					System.out.println("------------ [Retry] HTTP DoPost TryCount : " + tryHttpSend);
					return httpSend(urlstr, header, content, connecttimeout, readtimeout, reqencoding, resencoding, retrycount);
				} else {
					return null;
				}
			}
		} catch (Exception e) {
			System.out.println("HTTP DoPost Exception " + e);
			tryHttpSend++;
			if (tryHttpSend <= (retrycount < 0 ? httpRetryCount : retrycount)) {
				System.out.println("------------ [Retry] HTTP DoPost TryCount : " + tryHttpSend);
				return httpSend(urlstr, header, content, connecttimeout, readtimeout, reqencoding, resencoding, retrycount);
			} else {
				return null;
			}
		} finally {
			if (null != resinput) {
				try {
					resinput.close();
				} catch (Exception e) {
					System.out.println("HTTP Send InputStreamReader Close Exception " + e);
				}
			}
			if (null != httpurlconn) {
				try {
					httpurlconn.disconnect();
				} catch (Exception e) {
					System.out.println("HTTP Send HttpURLConnection Close Exception " + e);
				}
			}
			long t2 = System.currentTimeMillis();
			System.out.println("------------ [End] HTTP Send ( " + (t2 - t1) + " ms ) ");
		}
		return response;
	}

	/**
	 * HTTPS GET请求
	 * 
	 * @param urlstr 请求URL
	 * @return 返回内容
	 * 
	 * @see  #initSSL(String keyStorePath, String password, String trustStorePath)
	 */
	public static String httpsDoGet(String urlstr) {
		return httpsDoGet(urlstr, httpConnectTimeout, httpReadTimeout, httpResponseEncoding, httpRetryCount);
	}

	/**
	 * HTTPS GET请求
	 * 
	 * @param urlstr 请求URL
	 * @param resencoding 响应的编码方式
	 * @return 返回内容
	 * 
	 * @see  #initSSL(String keyStorePath, String password, String trustStorePath)
	 */
	public static String httpsDoGet(String urlstr, String resencoding) {
		return httpsDoGet(urlstr, httpConnectTimeout, httpReadTimeout, resencoding, httpRetryCount);
	}

	/**
	 * HTTPS GET请求
	 * 
	 * @param urlstr 请求URL
	 * @param connecttimeout 连接超时时间(秒)
	 * @param readtimeout 读取数据超时时间(秒)
	 * @param resencoding 响应的编码方式
	 * @param retrycount 失败时重试次数
	 * @return 返回内容
	 * 
	 * @see  #initSSL(String keyStorePath, String password, String trustStorePath)
	 */
	public static String httpsDoGet(String urlstr, int connecttimeout, int readtimeout, String resencoding, int retrycount) {
		// System.out.println("------------ [Start] HTTPS DoGet URL : " + urlstr);
		// long t1 = System.currentTimeMillis();
		String response = null;
		InputStreamReader resinput = null;
		HttpsURLConnection httpsurlconn = null;
		try {
			URL url = new URL(urlstr);
			httpsurlconn = (HttpsURLConnection) url.openConnection();
			httpsurlconn.setConnectTimeout(1000 * (connecttimeout < 0 ? httpConnectTimeout : connecttimeout));
			httpsurlconn.setReadTimeout(1000 * (readtimeout < 0 ? httpReadTimeout : readtimeout));
			httpsurlconn.setRequestMethod("GET");

			int icode = httpsurlconn.getResponseCode();
			// System.out.println("HTTPS Response Code : " + icode + " (" + httpsurlconn.getResponseMessage() + ") ");
			if (icode == HttpURLConnection.HTTP_OK) {
				int length = httpsurlconn.getContentLength();
				if (length <= 0) {
					return null;
				}
				resinput = new InputStreamReader(httpsurlconn.getInputStream(), (resencoding == null || "".equals(resencoding.trim()) ? httpResponseEncoding : resencoding.trim()));
				StringBuffer strbuf = new StringBuffer(length);
				char[] charbuff = new char[length];
		        int i;
		        while ((i = resinput.read(charbuff, 0, length - 1)) != -1) {
		        	strbuf.append(charbuff, 0, i);
		        }
		        response = strbuf.toString();
		        tryHttpsDoGet = 0;
				// System.out.println("HTTPS Response Content : \n------------ (" + length + ") \n" + response + "\n------------");
			} else {
				tryHttpsDoGet++;
				if (tryHttpsDoGet <= (retrycount < 0 ? httpRetryCount : retrycount)) {
					// System.out.println("------------ [Retry] HTTPS DoGet TryCount : " + tryHttpsDoGet);
					return httpsDoGet(urlstr, connecttimeout, readtimeout, resencoding, retrycount);
				} else {
					return null;
				}
			}
		} catch (Exception e) {
			// System.out.println("HTTPS DoGet Exception " + e);
			tryHttpsDoGet++;
			if (tryHttpsDoGet <= (retrycount < 0 ? httpRetryCount : retrycount)) {
				// System.out.println("------------ [Retry] HTTPS DoGet TryCount : " + tryHttpsDoGet);
				return httpsDoGet(urlstr, connecttimeout, readtimeout, resencoding, retrycount);
			} else {
				return null;
			}
		} finally {
			if (null != resinput) {
				try {
					resinput.close();
				} catch (Exception e) {
					// System.out.println("HTTPS DoGet InputStreamReader Close Exception " + e);
				}
			}
			if (null != httpsurlconn) {
				try {
					httpsurlconn.disconnect();
				} catch (Exception e) {
					// System.out.println("HTTPS DoGet HttpURLConnection Close Exception " + e);
				}
			}
			// long t2 = System.currentTimeMillis();
			// System.out.println("------------ [End] HTTPS DoGet ( " + (t2 - t1) + " ms ) ");
		}
		return response;
	}

	/**
	 * HTTPS POST请求
	 * 
	 * @param urlstr 请求URL
	 * @param params 请求参数
	 * @return 返回内容
	 * 
	 * @see  #initSSL(String keyStorePath, String password, String trustStorePath)
	 */
	public static String httpsDoPost(String urlstr, Map<String, String> params) {
		return httpsDoPost(urlstr, null, params, httpConnectTimeout, httpReadTimeout, httpRequestEncoding, httpResponseEncoding, httpRetryCount, httpsSSLSocketFactory, httpsHostnameVerifier);
	}

	/**
	 * HTTPS POST请求
	 * 
	 * @param urlstr 请求URL
	 * @param params 请求参数
	 * @param httpsSSLSocketFactory SSLSocketFactory
	 * @param httpsHostnameVerifier HostnameVerifier
	 * @return 返回内容
	 * 
	 * @see  #initSSL(String keyStorePath, String password, String trustStorePath)
	 */
	public static String httpsDoPost(String urlstr, Map<String, String> params, SSLSocketFactory httpsSSLSocketFactory, HostnameVerifier httpsHostnameVerifier) {
		return httpsDoPost(urlstr, null, params, httpConnectTimeout, httpReadTimeout, httpRequestEncoding, httpResponseEncoding, httpRetryCount, httpsSSLSocketFactory, httpsHostnameVerifier);
	}

	/**
	 * HTTPS POST请求
	 * 
	 * @param urlstr 请求URL
	 * @param params 请求参数
	 * @param reqencoding 请求的编码方式
	 * @param resencoding 响应的编码方式
	 * @return 返回内容
	 * 
	 * @see  #initSSL(String keyStorePath, String password, String trustStorePath)
	 */
	public static String httpsDoPost(String urlstr, Map<String, String> params, String reqencoding, String resencoding) {
		return httpsDoPost(urlstr, null, params, httpConnectTimeout, httpReadTimeout, reqencoding, resencoding, httpRetryCount, httpsSSLSocketFactory, httpsHostnameVerifier);
	}

	/**
	 * HTTPS POST请求
	 * 
	 * @param urlstr 请求URL
	 * @param header 头部参数
	 * @param params 请求参数
	 * @param reqencoding 请求的编码方式
	 * @param resencoding 响应的编码方式
	 * @return 返回内容
	 * 
	 * @see  #initSSL(String keyStorePath, String password, String trustStorePath)
	 */
	public static String httpsDoPost(String urlstr, Map<String, String> header, Map<String, String> params, String reqencoding, String resencoding) {
		return httpsDoPost(urlstr, header, params, httpConnectTimeout, httpReadTimeout, reqencoding, resencoding, httpRetryCount, httpsSSLSocketFactory, httpsHostnameVerifier);
	}

	/**
	 * HTTPS POST请求
	 * 
	 * @param urlstr 请求URL
	 * @param header 头部参数
	 * @param params 请求参数
	 * @param connecttimeout 连接超时时间(秒)
	 * @param readtimeout 读取数据超时时间(秒)
	 * @param reqencoding 请求的编码方式
	 * @param resencoding 响应的编码方式
	 * @param retrycount 失败时重试次数
	 * @param httpsSSLSocketFactory SSLSocketFactory
	 * @param httpsHostnameVerifier HostnameVerifier
	 * @return 返回内容
	 * 
	 * @see  #initSSL(String keyStorePath, String password, String trustStorePath)
	 */
	public static String httpsDoPost(String urlstr, Map<String, String> header, Map<String, String> params, int connecttimeout, int readtimeout, String reqencoding, String resencoding, int retrycount, SSLSocketFactory httpsSSLSocketFactory, HostnameVerifier httpsHostnameVerifier) {
		System.out.println("------------ [Start] HTTPS DoPost URL : " + urlstr);
		long t1 = System.currentTimeMillis();
		String response = null;
		InputStreamReader resinput = null;
		HttpsURLConnection httpsurlconn = null;
		try {
			URL url = new URL(urlstr);
			httpsurlconn = (HttpsURLConnection) url.openConnection();
			if (null != httpsSSLSocketFactory) {
				httpsurlconn.setSSLSocketFactory(httpsSSLSocketFactory);
			}
			if (null != httpsHostnameVerifier) {
				httpsurlconn.setHostnameVerifier(httpsHostnameVerifier);
			}
			httpsurlconn.setConnectTimeout(1000 * (connecttimeout < 0 ? httpConnectTimeout : connecttimeout));
			httpsurlconn.setReadTimeout(1000 * (readtimeout < 0 ? httpReadTimeout : readtimeout));
			httpsurlconn.setRequestMethod("POST");
			httpsurlconn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=" + (reqencoding = (reqencoding == null || "".equals(reqencoding.trim()) ? httpRequestEncoding : reqencoding.trim())));
			httpsurlconn.setDoInput(true);
			httpsurlconn.setDoOutput(true);
			if (null != header) {
				for (String header_key : header.keySet()) {
					httpsurlconn.setRequestProperty(header_key, header.get(header_key));
				}
			}
			if (null != params) {
				StringBuffer strbuf = new StringBuffer();
				for (String key : params.keySet()) {
					strbuf.append(key).append("=").append(params.get(key)).append("&");
				}
				String postparams = strbuf.toString();
				postparams = postparams.substring(0, postparams.length() -1);
				System.out.println("HTTPS Ruquest Params : " + postparams);
				byte[] data = postparams.getBytes(reqencoding);
				int length = data.length;
				httpsurlconn.setRequestProperty("Content-Length", String.valueOf(length));
				if (length != 0) {
					OutputStream resoutput = null;
					try {
						resoutput = httpsurlconn.getOutputStream();
						resoutput.write(data);
						resoutput.flush();
					} catch (Exception e) {
						System.out.println("HTTPS Ruquest Params Exception " + e);
					} finally {
						if (null != resoutput) {
							try {
								resoutput.close();
							} catch (Exception e) {
								System.out.println("HTTPS Ruquest Params OutputStreamWriter Close Exception " + e);
							}
						}
					}
				}
			}

			int icode = httpsurlconn.getResponseCode();
			System.out.println("HTTPS Response Code : " + icode + " (" + httpsurlconn.getResponseMessage() + ") ");
			if (icode == HttpURLConnection.HTTP_OK) {
				int length = httpsurlconn.getContentLength();
				if (length <= 0) {
					return null;
				}
				resinput = new InputStreamReader(httpsurlconn.getInputStream(), (resencoding == null || "".equals(resencoding.trim()) ? httpResponseEncoding : resencoding.trim()));
				StringBuffer strbuf = new StringBuffer(length);
				char[] charbuff = new char[length];
		        int i;
		        while ((i = resinput.read(charbuff, 0, length - 1)) != -1) {
		        	strbuf.append(charbuff, 0, i);
		        }
		        response = strbuf.toString();
		        tryHttpsDoPost = 0;
				System.out.println("HTTPS Response Content : \n------------ (" + length + ") \n" + response + "\n------------");
			} else {
				tryHttpsDoPost++;
				if (tryHttpsDoPost <= (retrycount < 0 ? httpRetryCount : retrycount)) {
					System.out.println("------------ [Retry] HTTPS DoPost TryCount : " + tryHttpsDoPost);
					return httpsDoPost(urlstr, header, params, connecttimeout, readtimeout, reqencoding, resencoding, retrycount, httpsSSLSocketFactory, httpsHostnameVerifier);
				} else {
					return null;
				}
			}
		} catch (Exception e) {
			System.out.println("HTTPS DoPost Exception " + e);
			tryHttpsDoPost++;
			if (tryHttpsDoPost <= (retrycount < 0 ? httpRetryCount : retrycount)) {
				System.out.println("------------ [Retry] HTTPS DoPost TryCount : " + tryHttpsDoPost);
				return httpsDoPost(urlstr, header, params, connecttimeout, readtimeout, reqencoding, resencoding, retrycount, httpsSSLSocketFactory, httpsHostnameVerifier);
			} else {
				return null;
			}
		} finally {
			if (null != resinput) {
				try {
					resinput.close();
				} catch (Exception e) {
					System.out.println("HTTPS DoPost InputStreamReader Close Exception " + e);
				}
			}
			if (null != httpsurlconn) {
				try {
					httpsurlconn.disconnect();
				} catch (Exception e) {
					System.out.println("HTTPS DoPost HttpsURLConnection Close Exception " + e);
				}
			}
			long t2 = System.currentTimeMillis();
			System.out.println("------------ [End] HTTPS DoPost ( " + (t2 - t1) + " ms ) ");
		}
		return response;
	}

	/**
	 * HTTPS Send请求
	 * 
	 * @param urlstr 请求URL
	 * @param content 请求数据
	 * @return 返回内容
	 */
	public static String httpsSend(String urlstr, String content) {
		return httpsSend(urlstr, null, content, httpConnectTimeout, httpReadTimeout, httpRequestEncoding, httpResponseEncoding, httpRetryCount, httpsSSLSocketFactory, httpsHostnameVerifier);
	}

	/**
	 * HTTPS Send请求
	 * 
	 * @param urlstr 请求URL
	 * @param content 请求数据
	 * @param httpsSSLSocketFactory SSLSocketFactory
	 * @param httpsHostnameVerifier HostnameVerifier
	 * @return 返回内容
	 */
	public static String httpsSend(String urlstr, String content, SSLSocketFactory httpsSSLSocketFactory, HostnameVerifier httpsHostnameVerifier) {
		return httpsSend(urlstr, null, content, httpConnectTimeout, httpReadTimeout, httpRequestEncoding, httpResponseEncoding, httpRetryCount, httpsSSLSocketFactory, httpsHostnameVerifier);
	}

	/**
	 * 
	 * @param urlstr
	 * @param xml      发送的xml字符串
	 * @param reqencoding
	 * @param resencoding
	 * @return
	 */
	public static String httpsDoPostXML(String urlstr,String xml, String reqencoding, String resencoding){
		return httpsDoPostXML(urlstr, null, xml,httpConnectTimeout,httpReadTimeout,reqencoding,resencoding,httpRetryCount);
	}
	public static String httpsDoPostXML(String urlstr, Map<String, String> header, String xml, int connecttimeout, int readtimeout, String reqencoding, String resencoding, int retrycount) {
		System.out.println("------------ [Start] HTTPS DoPost URL : " + urlstr);
		long t1 = System.currentTimeMillis();
		String response = null;
		InputStreamReader resinput = null;
		HttpsURLConnection httpsurlconn = null;
	
		try {
			URL url = new URL(urlstr);
			httpsurlconn = (HttpsURLConnection) url.openConnection();
			httpsurlconn.setConnectTimeout(1000 * (connecttimeout < 0 ? httpConnectTimeout : connecttimeout));
			httpsurlconn.setReadTimeout(1000 * (readtimeout < 0 ? httpReadTimeout : readtimeout));
			httpsurlconn.setRequestMethod("POST");
			httpsurlconn.setRequestProperty("Content-Type", "text/xml; charset=" +reqencoding);// 设置文件类型
			
			httpsurlconn.setDoInput(true);
			httpsurlconn.setDoOutput(true);
			if (null != header) {
				for (String header_key : header.keySet()) {
					httpsurlconn.setRequestProperty(header_key, header.get(header_key));
				}
			}
		
				System.out.println("HTTPS Ruquest xml : " + xml);
				//DES加密并转换成16进制
			//	xml=DesUtil.encode(DesUtil.encrypt(xml, DES_KEY));
				
				byte[] data = xml.getBytes(reqencoding);
				int dlength = data.length;
				httpsurlconn.setRequestProperty("Content-Length", String.valueOf(dlength));
				if (dlength != 0) {
					OutputStream resoutput = null;
					try {
						resoutput = httpsurlconn.getOutputStream();
						resoutput.write(data);
						resoutput.flush();
					} catch (Exception e) {
						log.error("HTTPS Ruquest Params Exception :"+e);
						System.out.println("HTTPS Ruquest Params Exception " + e);
					} finally {
						if (null != resoutput) {
							try {
								resoutput.close();
							} catch (Exception e) {
								log.error("HTTPS Ruquest Params OutputStreamWriter Close Exception " + e);
								System.out.println("HTTPS Ruquest Params OutputStreamWriter Close Exception " + e);
							}
						}
					}
				}
			

			int icode = httpsurlconn.getResponseCode();
			log.debug("HTTPS Response Code : " + icode + " (" + httpsurlconn.getResponseMessage() + ") ");
			System.out.println("HTTPS Response Code : " + icode + " (" + httpsurlconn.getResponseMessage() + ") ");
			if (icode == HttpURLConnection.HTTP_OK) {
//				int length = httpsurlconn.getContentLength();
//				if (length <= 0) {
//					return null;
//				}
				resinput = new InputStreamReader(httpsurlconn.getInputStream(), (resencoding == null || "".equals(resencoding.trim()) ? httpResponseEncoding : resencoding.trim()));
				StringBuffer strbuf = new StringBuffer();
				char[] charbuff = new char[2048];
		        int i;
		        while ((i = resinput.read(charbuff)) != -1) {
		        	strbuf.append(charbuff, 0, i);
		        }
		        response = strbuf.toString();
		        //response = DesUtil.decrypt(DesUtil.decode(response), DES_KEY);
		        log.debug("--------------------resp"+response);
		        log.debug("--------------------respend-------------------------");
		        response =  new  XMLSerializer().read(response).toString();
		        JSONObject json = JSONObject.fromObject(response);
		        response=  ((JSONObject)((JSONObject)(json.get("S:Body"))).get("ns2:handleResponse")).get("return").toString();
		        log.debug("--------------------resp2"+response);
		        log.debug("--------------------respend2-------------------------");
		        tryHttpsDoPost = 0;
				System.out.println("HTTPS Response Content : \n------------ (" + strbuf.length() + ") \n" + response + "\n------------");
			} else {
				tryHttpsDoPost++;
				if (tryHttpsDoPost <= (retrycount < 0 ? httpRetryCount : retrycount)) {
					System.out.println("------------ [Retry] HTTPS DoPost TryCount : " + tryHttpsDoPost);
					return httpsDoPostXML( urlstr, header, xml,connecttimeout,readtimeout,reqencoding,resencoding,retrycount);
				} else {
					return null;
				}
			}
		} catch (Exception e) {
			log.error("HTTPS DoPostXML ddddException " + e);
			System.out.println("HTTPS DoPost Exception " + e);
			tryHttpsDoPost++;
			if (tryHttpsDoPost <= (retrycount < 0 ? httpRetryCount : retrycount)) {
				System.out.println("------------ [Retry] HTTPS DoPost TryCount : " + tryHttpsDoPost);
				return httpsDoPostXML( urlstr, header, xml,connecttimeout,readtimeout,reqencoding,resencoding,retrycount);
			} else {
				return null;
			}
		} finally {
			if (null != resinput) {
				try {
					resinput.close();
				} catch (Exception e) {
					log.info("HTTPS DoPostxml InputStreamReader Close Exception " + e);
					System.out.println("HTTPS DoPost InputStreamReader Close Exception " + e);
				}
			}
			if (null != httpsurlconn) {
				try {
					httpsurlconn.disconnect();
				} catch (Exception e) {
					log.info("HTTPS DoPostXML1 HttpsURLConnection Close Exception " + e);
					System.out.println("HTTPS DoPost HttpsURLConnection Close Exception " + e);
				}
			}
			long t2 = System.currentTimeMillis();
			System.out.println("------------ [End] HTTPS DoPost ( " + (t2 - t1) + " ms ) ");
		}
		return response;
	}
	/**
	 * HTTPS Send请求
	 * 
	 * @param urlstr 请求URL
	 * @param content 请求数据
	 * @param reqencoding 请求的编码方式
	 * @param resencoding 响应的编码方式
	 * @return 返回内容
	 */
	public static String httpsSend(String urlstr, String content, String reqencoding, String resencoding) {
		return httpsSend(urlstr, null, content, httpConnectTimeout, httpReadTimeout, reqencoding, resencoding, httpRetryCount, httpsSSLSocketFactory, httpsHostnameVerifier);
	}

	/**
	 * HTTPS Send请求
	 * 
	 * @param urlstr 请求URL
	 * @param header 请求参数
	 * @param content 请求数据
	 * @param reqencoding 请求的编码方式
	 * @param resencoding 响应的编码方式
	 * @return 返回内容
	 */
	public static String httpsSend(String urlstr, Map<String, String> header, String content, String reqencoding, String resencoding) {
		return httpsSend(urlstr, header, content, httpConnectTimeout, httpReadTimeout, reqencoding, resencoding, httpRetryCount, httpsSSLSocketFactory, httpsHostnameVerifier);
	}

	/**
	 * HTTPS Send请求
	 * 
	 * @param urlstr 请求URL
	 * @param header 头部参数
	 * @param content 请求数据
	 * @param connecttimeout 连接超时时间(秒)
	 * @param readtimeout 读取数据超时时间(秒)
	 * @param reqencoding 请求的编码方式
	 * @param resencoding 响应的编码方式
	 * @param retrycount 失败时重试次数
	 * @param httpsSSLSocketFactory SSLSocketFactory
	 * @param httpsHostnameVerifier HostnameVerifier
	 * @return 返回内容
	 */
	public static String httpsSend(String urlstr, Map<String, String> header, String content, int connecttimeout, int readtimeout, String reqencoding, String resencoding, int retrycount, SSLSocketFactory httpsSSLSocketFactory, HostnameVerifier httpsHostnameVerifier) {
		System.out.println("------------ [Start] HTTPS Send URL : " + urlstr);
		long t1 = System.currentTimeMillis();
		String response = null;
		InputStreamReader resinput = null;
		HttpsURLConnection httpsurlconn = null;
		try {
			URL url = new URL(urlstr);
			httpsurlconn = (HttpsURLConnection) url.openConnection();
			if (null != httpsSSLSocketFactory) {
				httpsurlconn.setSSLSocketFactory(httpsSSLSocketFactory);
			}
			if (null != httpsHostnameVerifier) {
				httpsurlconn.setHostnameVerifier(httpsHostnameVerifier);
			}
			httpsurlconn.setConnectTimeout(1000 * (connecttimeout < 0 ? httpConnectTimeout : connecttimeout));
			httpsurlconn.setReadTimeout(1000 * (readtimeout < 0 ? httpReadTimeout : readtimeout));
			httpsurlconn.setRequestMethod("POST");
			httpsurlconn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=" + (reqencoding == null || "".equals(reqencoding.trim()) ? httpRequestEncoding : reqencoding.trim()));
			httpsurlconn.setDoInput(true);
			httpsurlconn.setDoOutput(true);
			if (null != header) {
				for (String header_key : header.keySet()) {
					httpsurlconn.setRequestProperty(header_key, header.get(header_key));
				}
			}
			if (null != content) {
				log.info("HTTPS Ruquest Content : " + content);
				byte[] data = content.getBytes(reqencoding);
				int length = data.length;
				httpsurlconn.setRequestProperty("Content-Length", String.valueOf(length));
				if (length != 0) {
					OutputStream resoutput = null;
					try {
						resoutput = httpsurlconn.getOutputStream();
						resoutput.write(data);
						resoutput.flush();
					} catch (Exception e) {
						System.out.println("HTTPS Ruquest Params Exception " + e);
					} finally {
						if (null != resoutput) {
							try {
								resoutput.close();
							} catch (Exception e) {
								System.out.println("HTTPS Ruquest Params OutputStreamWriter Close Exception " + e);
							}
						}
					}
				}
			}

			int icode = httpsurlconn.getResponseCode();
			log.info("HTTPS Response Code : " + icode + " (" + httpsurlconn.getResponseMessage() + ") ");
			if (icode == HttpURLConnection.HTTP_OK) {
				int length = httpsurlconn.getContentLength();
				if (length <= 0) {
					return null;
				}
				resinput = new InputStreamReader(httpsurlconn.getInputStream(), (resencoding == null || "".equals(resencoding.trim()) ? httpResponseEncoding : resencoding.trim()));
				StringBuffer strbuf = new StringBuffer(length);
				char[] charbuff = new char[length];
		        int i;
		        while ((i = resinput.read(charbuff, 0, length - 1)) != -1) {
		        	strbuf.append(charbuff, 0, i);
		        }
		        response = strbuf.toString();
		        tryHttpsSend = 0;
		        log.info("HTTPS Response Content : \n------------ (" + length + ") \n" + response + "\n------------");
			} else {
				tryHttpsSend++;
				if (tryHttpsSend <= (retrycount < 0 ? httpRetryCount : retrycount)) {
					System.out.println("------------ [Retry] HTTPS DoPost TryCount : " + tryHttpsSend);
					return httpsSend(urlstr, header, content, connecttimeout, readtimeout, reqencoding, resencoding, retrycount, httpsSSLSocketFactory, httpsHostnameVerifier);
				} else {
					return null;
				}
			}
		} catch (Exception e) {
			log.info("HTTPS DoPost Exception " + e);
			tryHttpsSend++;
			if (tryHttpsSend <= (retrycount < 0 ? httpRetryCount : retrycount)) {
				System.out.println("------------ [Retry] HTTPS DoPost TryCount : " + tryHttpsSend);
				return httpsSend(urlstr, header, content, connecttimeout, readtimeout, reqencoding, resencoding, retrycount, httpsSSLSocketFactory, httpsHostnameVerifier);
			} else {
				return null;
			}
		} finally {
			if (null != resinput) {
				try {
					resinput.close();
				} catch (Exception e) {
					System.out.println("HTTPS Send InputStreamReader Close Exception " + e);
				}
			}
			if (null != httpsurlconn) {
				try {
					httpsurlconn.disconnect();
				} catch (Exception e) {
					System.out.println("HTTPS Send HttpURLConnection Close Exception " + e);
				}
			}
			long t2 = System.currentTimeMillis();
			log.info("------------ [End] HTTPS Send ( " + (t2 - t1) + " ms ) ");
		}
		return response;
	}

	/**
	 * 初始化HTTPS环境, 忽略证书 (调用HTTPS请求之前初始化, 且只需初始化一次)
	 * <br/><br/>
	 * 生成KeyStore命令:
	 * <br/>
	 * keytool -genkey -alias tomcat -keyalg RSA -keysize 1024 -validity 365 -keystore tomcat.keystore
	 * 
	 * @return 初始化成功
	 */
	public static boolean initSSL() {
		if (isInitSSL) { // 判断是否已初始化标识, 保证只初始化一次
			return true;
		}
		try {
			// 实例化SSL上下文
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new TrustManager[]{new MyX509TrustManager()}, null);

			// 实例化HTTPS连接
			httpsSSLSocketFactory = context.getSocketFactory();
			HttpsURLConnection.setDefaultSSLSocketFactory(httpsSSLSocketFactory);
			httpsHostnameVerifier = new MyHostnameVerifier();
			HttpsURLConnection.setDefaultHostnameVerifier(httpsHostnameVerifier);
		} catch (Exception e) {
			e.printStackTrace();
			isInitSSL = false; // 设置初始化标识
			return false;
		}
		isInitSSL = true; // 设置初始化标识
		return true;
	}

	/**
	 * 初始化HTTPS环境 (调用HTTPS请求之前初始化, 且只需初始化一次)
	 * <br/><br/>
	 * 生成KeyStore命令:
	 * <br/>
	 * keytool -genkey -alias tomcat -keyalg RSA -keysize 1024 -validity 365 -keystore tomcat.keystore
	 * 
	 * @param keyStorePath 密钥库路径
	 * @param password 密钥库密码
	 * @param trustStorePath 信任库路径
	 * @return 初始化成功
	 */
	public static boolean initSSL(String keyStorePath, String password, String trustStorePath) {
		if (isInitSSL) { // 判断是否已初始化标识, 保证只初始化一次
			return true;
		}
		try {
			// 实例化密钥库
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			// 获得密钥库
			KeyStore keyStore = getKeyStore(keyStorePath, password);
			if (keyStore != null) {
				keyManagerFactory.init(keyStore, password.toCharArray());
			}

			// 实例化信任库
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			KeyStore trustStore = getKeyStore(trustStorePath, password);
			if (trustStore != null) {
				trustManagerFactory.init(trustStore);
			}

			// 实例化SSL上下文
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

			// 实例化HTTPS连接
			httpsSSLSocketFactory = context.getSocketFactory();
			HttpsURLConnection.setDefaultSSLSocketFactory(httpsSSLSocketFactory);
			httpsHostnameVerifier = new MyHostnameVerifier();
			HttpsURLConnection.setDefaultHostnameVerifier(httpsHostnameVerifier);
		} catch (Exception e) {
			e.printStackTrace();
			isInitSSL = false; // 设置初始化标识
			return false;
		}
		isInitSSL = true; // 设置初始化标识
		return true;
	}
	public static boolean initSSL(String keyStorePath) {
		if (isInitSSL) { // 判断是否已初始化标识, 保证只初始化一次
			return true;
		}
		SSLContext ssl = null;
		try {
			ssl=SSLInit.init(keyStorePath);
			HttpsURLConnection.setDefaultSSLSocketFactory(ssl.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
		} catch (Exception e) {
			e.printStackTrace();
			isInitSSL = false; // 设置初始化标识
			return false;
		}
		
		if(ssl!=null)
			isInitSSL=true;
		return true;
	}

	/**
	 * 获得密钥库
	 * 
	 * @param keyStorePath 密钥库路径
	 * @param password 密钥库密码
	 * @return 密钥库
	 */
	public static KeyStore getKeyStore(String keyStorePath, String password) {
		if (keyStorePath == null) {
			return null;
		}
		KeyStore keyStore = null;
		FileInputStream input = null;
		try {
			keyStore = KeyStore.getInstance("JKS"); // 实例化密钥库
			input = new FileInputStream(keyStorePath); // 获得密钥库文件流
			keyStore.load(input, password.toCharArray()); // 加载密钥库
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return keyStore;
	}

}


class MyX509TrustManager implements X509TrustManager {

	public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
	}

	public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
	}

	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}

}


class MyHostnameVerifier implements HostnameVerifier {

	public boolean verify(String hostname, SSLSession session) {
		return true;
	}

}
