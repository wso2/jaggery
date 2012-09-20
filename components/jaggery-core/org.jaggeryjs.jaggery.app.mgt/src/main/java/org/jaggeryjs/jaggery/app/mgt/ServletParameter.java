package org.jaggeryjs.jaggery.app.mgt;

import java.util.Map;

/**
* This class will hold the servlet web.xml entries
*/
public class ServletParameter {

   private String servletName;
	private String servletClass;
	private int loadOnStartup;
	private Map<String,String> initParams;

	public String getServletName() {
		return servletName;
	}
	public void setServletName(String servletName) {
		this.servletName = servletName;
	}
	public String getServletClass() {
		return servletClass;
	}
	public void setServletClass(String servletClass) {
		this.servletClass = servletClass;
	}
	public int getLoadOnStartup() {
		return loadOnStartup;
	}
	public void setLoadOnStartup(int loadOnStartup) {
		this.loadOnStartup = loadOnStartup;
	}
	public Map<String, String> getInitParams() {
		return initParams;
	}
	public void setInitParams(Map<String, String> initParams) {
		this.initParams = initParams;
	}

}
