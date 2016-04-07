/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.web.util;

import java.io.IOException;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@SessionScoped
@ManagedBean
public class UserMB {
	String user;
	
	public String getUser(){
		if(user == null){
			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			String user = context.getUserPrincipal().getName();
		}
		
		return user;
	}
	
	public boolean isUserAdmin(){
		return getRequest().isUserInRole("ADMIN");
	}
	
	public String logOut_old(){
		getRequest().getSession().invalidate();
		//return "logout";
		//return "/index.xhtml";
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("/index.jsf");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void logOut() throws IOException {
	    ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
	    ec.invalidateSession();	    
	    ec.redirect(ec.getRequestContextPath()+"/index.jsf");
	}

	private HttpServletRequest getRequest() {
		return (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
	}
	
	
//	public String logout() {
//	       ((HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false)).invalidate();
//	        return "/index.xhtml";
//	}
}