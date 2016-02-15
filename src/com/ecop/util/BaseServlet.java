package com.ecop.util;

import java.io.InputStreamReader;
import java.util.regex.Pattern;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import config.Config;

public class BaseServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	protected String regular = "^[0-9]*[1-9][0-9]*$"; // "^\\d+$" 正整数 +0; "^[0-9]*[1-9][0-9]*$" 正整数

	/**
	 * 获得访问者IP地址
	 * 
	 * @param request HttpServletRequest
	 * @return 访问者源IP地址
	 */
	public String getRequestIP(HttpServletRequest request) {
		String requestip = request.getHeader("x-forwarded-for");
		requestip = requestip == null ? request.getRemoteAddr() : requestip;
		return requestip;
	}

	/**
	 * 检查IP地址合法性
	 * 
	 * @param regex IP验证规则列表
	 * @param ip IP地址
	 * @return 是否符合规则
	 */
	public boolean checkRequestIP(String[] regexs, String ip) {
		if (ip == null || !Pattern.matches("^((25[0-5]|2[0-4]\\d|1?\\d?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1?\\d?\\d)$", ip)) {
			return false;
		}
		if (regexs == null || regexs.length == 0) {
			return true;
		}
		for (String rule : regexs) {
			if (Pattern.matches("^" + rule.replaceAll("\\.", "\\.") + "$", ip)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获得输入文本
	 * 
	 * @param request HttpServletRequest
	 * @param charset 字符编码
	 * @return 输入文本
	 */
	public String servletRead(HttpServletRequest request, String charset) {
		if (charset == null || "".equals(charset)) {
			charset = Config.getValue("charset");
		}
		InputStreamReader reader = null;
		ServletInputStream input = null;
		try {
			StringBuffer strbuf = new StringBuffer();
			input = request.getInputStream();
			reader = new InputStreamReader(input, charset);
			int len = -1;
			char[] buf = new char[1024];
			while ((len = reader.read(buf)) != -1) {
				strbuf.append(buf, 0, len);
			}
			return strbuf.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (null != reader) {
				try {
					reader.close();
				} catch (Exception e) {
					e.fillInStackTrace();
				}
			}
			if (null != input) {
				try {
					input.close();
				} catch (Exception e) {
					e.fillInStackTrace();
				}
			}
		}
	}

	/**
	 * 获得输入文本（读取byte[]转字符串，存在风险）
	 * 
	 * @param request HttpServletRequest
	 * @param charset 字符编码
	 * @return 输入文本
	 */
	public String servletReadByte(HttpServletRequest request, String charset) {
		if (charset == null || "".equals(charset)) {
			charset = Config.getValue("charset");
		}
		ServletInputStream input = null;
		try {
			StringBuffer strbuf = new StringBuffer();
			input = request.getInputStream();
			int len = -1;
			byte[] buf = new byte[1024];
			while ((len = input.read(buf)) != -1) {
				strbuf.append(new String(buf, 0, len, charset));
			}
			return strbuf.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (Exception e) {
					e.fillInStackTrace();
				}
			}
		}
	}

	/**
	 * 获得输入内容
	 * 
	 * @param request HttpServletRequest
	 * @param length 内容长度
	 * @return 输入内容
	 */
	public byte[] servletRead(HttpServletRequest request, int length) {
		ServletInputStream input = null;
		byte[] content = new byte[length];
		try {
			input = request.getInputStream();
			int len = -1;
			int contentlen = 0;
			byte[] buf = new byte[1024];
			while ((len = input.read(buf)) != -1) {
				System.arraycopy(buf, 0, content, contentlen, len);
				contentlen += len;
			}
			return content;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (Exception e) {
					e.fillInStackTrace();
				}
			}
		}
	}

	/**
	 * 输出文本内容
	 * 
	 * @param response HttpServletResponse
	 * @param results 输出的内容
	 * @return 是否成功
	 */
	public boolean servletWrite(HttpServletResponse response, String results) {
		if (results == null) {
			return false;
		}
		ServletOutputStream output = null;
		try {
			byte[] result = results.getBytes(Config.getValue("charset"));
			response.reset();
			response.setContentType("text/plain; charset=" + Config.getValue("charset"));
			response.setHeader("Content-Disposition", "text/plain;charset=" + Config.getValue("charset"));
			response.setHeader("Content-Length", String.valueOf(result.length));
			response.setHeader("return-from", "pushserver");
			output = response.getOutputStream();
			output.write(result);
			output.flush();
			return true;
		} catch (Exception e) {
			e.fillInStackTrace();
			return false;
		} finally {
			if (null != output) {
				try {
					output.close();
				} catch (Exception e) {
					e.fillInStackTrace();
				}
			}
		}
	}

}
