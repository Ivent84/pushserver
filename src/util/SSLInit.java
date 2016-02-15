package util;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import config.Config;


/**
 * <描述>
 * 
 * @author galleon
 * @date 2012-10-22
 */
public class SSLInit {
	private static final Log log = LogFactory.getLog(SSLInit.class);
	static String host = "221.179.11.204";
	static{
		
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				// ip address of the service URL(like.23.28.244.244)
				if (host.equals(hostname)) return true;
				return false;
			}
		});
	}
	
	public static SSLContext init(String pa) throws Exception {
		// keystore path and password
		String pass = Config.getValue("pass");
//		String keyf = prop.getProperty("KeyStore");
//		String trustf = prop.getProperty("TrustStore");
		String keyf = pa;
		String trustf = pa;
		String keyType = Config.getValue("keyType");
		String trustType = Config.getValue("trustType");
		if(pass==null)pass="";
		// set up a connection
		SSLContext ctx = null;
		try {
			System.setProperty("javax.net.ssl.keyStore", keyf);
			System.setProperty("javax.net.ssl.keyStorePassword", pass);
			System.setProperty("javax.net.ssl.trustStore", trustf);
			System.setProperty("javax.net.ssl.trustStorePassword", pass);
			
			// init context
			ctx = SSLContext.getInstance("TLS");
//			SSLContext.setDefault(ctx);
			if("1.6".compareTo(System.getProperty("java.specification.version"))<=0){
				//jdk1.6才有该方法
				Class<?> clz = SSLContext.class;
				clz.getMethod("setDefault", clz).invoke(clz, ctx);
			}
			
			KeyStore ks = KeyStore.getInstance(keyType);
			KeyStore tks = KeyStore.getInstance(trustType);
			
			// load keystore
			ks.load(new FileInputStream(keyf), pass.toCharArray());
			tks.load(new FileInputStream(trustf), pass.toCharArray());

			String alg = KeyManagerFactory.getDefaultAlgorithm();
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(alg);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(alg);
			kmf.init(ks, pass.toCharArray());
			tmf.init(tks);
			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
			System.out.println("load keystore success.");
			log.info("load keystore success.");
			
		} catch (Exception e) {
			ctx = null;
			System.out.println("establish connection error.");
			log.error("establish connection error.");
			e.printStackTrace();
		} 
		return ctx;
	}
	
	
	public static SSLContext init(String keyStorePath, String trustStorePath) throws Exception {
		SSLContext ctx = SSLContext.getInstance("SSL");  
		String pass = Config.getValue("pass");
		//用于证实自己身份
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()); ;
		KeyStore ks = KeyStore.getInstance("JKS");

	      log.info("keystorepath:======================"+keyStorePath);
		ks.load(new FileInputStream(keyStorePath), pass.toCharArray());  
		kmf.init(ks, pass.toCharArray());  
		//ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		//初始化工厂：证实自己身份的keystore和信任的keystore 
		
		TrustAnyTrustManager  xmt = new TrustAnyTrustManager ();
      	ctx.init(kmf.getKeyManagers(), new TrustManager[]{xmt}, null);  
		return ctx;
	}
	
	private static class TrustAnyTrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException 
        {  
        }  
    
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException 
        {  
        }  
    
        public X509Certificate[] getAcceptedIssuers() 
        {  
        	return null;
        }  
    }
    
}
