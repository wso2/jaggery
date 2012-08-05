package org.jaggeryjs.jaggery.app.mgt;

/**
* This class will hold the servlet mappings web.xml entries
*/
public class ServletMappingParameter {

	private String servletName;
	private String urlPattern;

	public String getServletName() {
		return servletName;
	}
	public void setServletName(String servletName) {
		this.servletName = servletName;
	}
	public String getUrlPattern() {
		return urlPattern;
	}
	public void setUrlPattern(String urlPattern) {
		this.urlPattern = urlPattern;
	}
}