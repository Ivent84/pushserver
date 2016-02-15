package com.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import config.Config;
import service.DaemonThr;
import web.UrlUtil;

/**
 * Application Lifecycle Listener implementation class TaskListener
 *
 */
@WebListener
public class TaskListener implements ServletContextListener {
	
	private java.util.Timer timer = null;
	
    /**
     * Default constructor. 
     */
    public TaskListener() {
        // TODO Auto-generated constructor stub
    	System.out.println("启用后台调度");
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0)  { 
         // TODO Auto-generated method stub
    	timer = new java.util.Timer(true);
    	//启动Ios掉线检查
        timer.schedule(new OfflineCheckTask(), 0, Long.parseLong(Config.getValue("heartbeatCheckFrequencySec")) * 1000);
        arg0.getServletContext().log("已经添加Ios掉线检查调度");
        
        //启动后台监控程序
        new DaemonThr().runTopicWillListener();//仅用于监控android长链接
        arg0.getServletContext().log("已经添加android掉线监控调度");
        
        //设置路径
        UrlUtil.setBasePath(arg0.getServletContext().getContextPath());
        arg0.getServletContext().log("设置basepath" + arg0.getServletContext().getContextPath());
        
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0)  { 
         // TODO Auto-generated method stub
    	timer.cancel();    
        arg0.getServletContext().log("定时器销毁");
    }
	
}
