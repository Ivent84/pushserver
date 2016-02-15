package config;



/**
 * 全局数据
 * 
 * @author Lilibo
 *
 */
public class Global {

	/**
	 * 字符编码
	 */
	public static final String charset = "UTF-8";

	/**
	 * 时间格式
	 */
	public static final String dateformat = "yyyyMMddHHmmss";

	/**
	 * 消息内容分隔符
	 */
	public static final String splitregex = "@";

	/**
	 * 请求标识：企信注册标识
	 */
	public static final String TAG_KTQX = "KTQX";

	/**
	 * 请求标识：获取手机号码标识
	 */
	public static final String TAG_HQSJH = "SJHM";

	/** 头部签名参数：sign */
	public static final String HEAD_sign = "sign";

	/**
	 * 响应信息：报文解析错误
	 */
	public static final String RESPONSE_ERROR_10001 = "{\"error\":{\"error_code\":10001,\"error_message\":\"报文解析错误！\"}}";

	/**
	 * 响应信息：参数格式错误
	 */
	public static final String RESPONSE_ERROR_10002 = "{\"error\":{\"error_code\":10002,\"error_message\":\"参数格式错误！\"}}";

	/**
	 * 响应信息：验证失败
	 */
	public static final String RESPONSE_ERROR_10003 = "{\"error\":{\"error_code\":10003,\"error_message\":\"验证失败！\"}}";


	/**
	 * 响应信息：服务器内部错误
	 */
	public static final String RESPONSE_ERROR_10005 = "{\"error\":{\"error_code\":10005,\"error_message\":\"服务器内部错误！\"}}";
	/**
	 * 响应信息：ECOP连接异常
	 */
	public static final String RESPONSE_ERROR_10006 = "{\"error\":{\"error_code\":10006,\"error_message\":\"ECOP服务异常！\"}}";

	/**
	 * 响应信息：ECOP返回的数据格式错误
	 */
	public static final String RESPONSE_ERROR_10007 = "{\"error\":{\"error_code\":10007,\"error_message\":\"ECOP返回的数据格式错误！\"}}";

}
