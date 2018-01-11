/****************************************************************************** 
 * @Title: ThreadPool.java 
 * @author wuyj 
 * @Create Date : 2015-8-6 
 * @JDK version used:      JDK1.6  
 * @Version : V1.0 
 * @Description: TODO 
 *  
 * History : TODO
 *
 * @Copyright (C), 2001-2012, Xi'an TCL Software Development Co.,Ltd 
 * All rights reserved 
 ******************************************************************************/
package com.reb.dsd_ble.ble.profile.utility;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/** 
 * @description :Class description goes here. 
 *
 * @author: 409041 
 * @Create Date :  2015-8-6 
 * @version : V1.0 
 * @description :   
 */

public class ThreadPool {
	 
	private final String TAG = "ThreadPool";
	
	private static ThreadPool mThreadPool = null;
	private ExecutorService cachedThreadPool = null;
	private ExecutorService singleThreadPool = null;
	
	public static ThreadPool getInstance(){
		
		if(mThreadPool == null){
			synchronized (ThreadPool.class) {
				if(mThreadPool == null){
					mThreadPool = new ThreadPool();
				}
			}
		}
		
		return mThreadPool;
	}
	
	/**
	 * 
	 * <p>Title: TODO</p> 
	 * <p>Description: TODO</p>
	 */
	private ThreadPool(){
		newCacheThreadPool();
		newSingleThreadPool();
	}
	
	/**
	 * 
	 * @Title: newThreadPool 
	 * @Description: TODO 
	 * @param 
	 * @return void 
	 * @exception/throws  description
	 */
	private void newCacheThreadPool(){
		cachedThreadPool = Executors.newCachedThreadPool();
	}
	
	
	/**
	 * 
	 * @Title: execute 
	 * @Description: TODO 
	 * @param @param run
	 * @return void 
	 * @exception/throws  description
	 */
	public void cacheThreadsexecute(Runnable run){
		cachedThreadPool.execute(run);
	}
	
	/**
	 * 
	 * @Title: newSingleThreadPool 
	 * @Description: TODO 
	 * @param 
	 * @return void 
	 * @exception/throws  description
	 */
	private void newSingleThreadPool(){
		singleThreadPool = Executors.newSingleThreadExecutor();
	}
	
	/**
	 * 
	 * @Title: singleThreadsexecute 
	 * @Description: TODO 
	 * @param @param run
	 * @return void 
	 * @exception/throws  description
	 */
	public void singleThreadsexecute(Runnable run){
		singleThreadPool.execute(run);
	}
}
