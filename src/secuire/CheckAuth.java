package secuire;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.CharBuffer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import config.Config;
import service.Notice;
import util.ServiceUtils;
import util.XMLHandler;
import web.UrlUtil;
import util.BodyReaderHttpServletRequestWrapper;

/**
 * Servlet Filter implementation class CheckAuth
 */
@WebFilter(urlPatterns = { "/web/*" })
public class CheckAuth implements Filter {

    /**
     * Default constructor. 
     */
    public CheckAuth() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}
	private boolean disable(ServletRequest request) throws IOException{
		if(true)
			return false;
		
		String sign = ((HttpServletRequest)request).getHeader("sign");
		
		
		
		String[] signs = ServiceUtils.decryptSignOAuth(sign);
		String keys = signs[0];
		
		//鉴权，使用企信算法
		StringBuffer strbuf = new StringBuffer();
		char[] charbuff = new char[512];
		int i;
		BufferedReader br = request.getReader();
		while ((i = br.read(charbuff)) != -1) {
			strbuf.append(charbuff, 0, i);
		}

		if (!StringUtils.isBlank(strbuf.toString())) {
			String requestJson = strbuf.toString();
			if (sign.equals(ServiceUtils.generateSignOAuth(keys, requestJson)[1])) {
				return false;
			}
		}
		return true;
	}
	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		// TODO Auto-generated method stub
		request.setCharacterEncoding(Config.getValue("charset"));
		response.setCharacterEncoding(Config.getValue("charset"));
		((HttpServletResponse)response).setHeader("Pragma","No-cache"); 
		((HttpServletResponse)response).setHeader("Cache-Control","no-cache, must-revalidate"); 
		((HttpServletResponse)response).setDateHeader("Expires", 0);
		
		HttpSession session = ((HttpServletRequest)request).getSession();
		String sessionId = session.getId();
		if (! UrlUtil.encodeUrl("web", "Member", "login").equals(((HttpServletRequest)request).getRequestURI())){
			if (null == session.getAttribute("userid")){
				((HttpServletResponse)response).sendRedirect(UrlUtil.encodeUrl("web", "Member", "login"));
				return;
			}
		}
		
		// place your code here
		// pass the request along the filter chain
//		String json = getResponseJSON("<smsauthsendresp><msgheader><req_seq>cd69aa6cee1849f385be9134904b0f1a</req_seq><ope_seq>20151229094528509040</ope_seq><retinfo><rettype>0</rettype><retcode>0</retcode><retmsg>成功</retmsg></retinfo></msgheader></smsauthsendresp>");
//		new Notice().notifyMsg("13822292229","");
		
		
//		ServletRequest requestWrapper = null;
//		if (request instanceof HttpServletRequest) {
//			requestWrapper = new BodyReaderHttpServletRequestWrapper((HttpServletRequest) request);
//		}
//		System.out.println("in filter");
//		
//		if (null == requestWrapper) {
//			if(disable(request))
//				return;
//			chain.doFilter(request, response);
//		} else {
//			if(disable(requestWrapper))
//				return;
//			chain.doFilter(requestWrapper, response);
//		}
		chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}
	String getResponseJSON(String msg) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			XMLHandler handl = new XMLHandler();
			InputStream is = new ByteArrayInputStream(msg.getBytes());

			saxParser.parse(is, handl);
			is.close();
			return handl.toJson();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}

}
