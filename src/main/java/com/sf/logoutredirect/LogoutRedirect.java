// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 12/1/2013 8:17:28 PM
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   LogoutRedirect.java
package com.sf.logoutredirect;

import com.liferay.portal.SystemException;
import com.liferay.portal.kernel.events.Action;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.struts.LastPath;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import com.sf.wbroms.entity.main.company.Company;
import com.sf.wbroms.entity.main.person.Person;
import com.sf.webroms.common.RomsConstants;
import com.sf.webroms.common.UserMgmtUtil;
import com.sf.webroms.common.UtilBeanImpl;
import java.io.*;
import java.util.*;
import javax.servlet.http.*;

public class LogoutRedirect extends Action {

    private static Log log = LogFactoryUtil.getLog(LogoutRedirect.class);

    public LogoutRedirect() {
    }

    public void run(HttpServletRequest request, HttpServletResponse response) {
        try {
        
            HttpSession session = request.getSession();
            User user = null;

            String defaultPath ;

            defaultPath = UtilBeanImpl
                    .getGridSettingsValue(RomsConstants.DEFAULT_LOGOUT_REDIRECT_URL_SETTING,
                    "/", "default logout redirect url", true);
            
            
            String logoutURLPath = defaultPath;
            if (session != null) {
                try {
                    user = PortalUtil.getUser(request);
                    log.info("user id is " + request.getRemoteUser());
                    if (user == null && request.getRemoteUser() != null && !request.getRemoteUser().trim().isEmpty()) {
                        user = UserLocalServiceUtil.getUserById(Long.valueOf(request.getRemoteUser()));
                    }
                    if (user != null) {
                        log.info((new StringBuilder()).append("----------------------Current User: ").append(user.getUserId()).toString());
                        log.info((new StringBuilder()).append("----------------------Current User: ").append(user.getUserId()).toString());
                        
                        /**
                         * invalidate session to manually log user out
                         */
                        session.invalidate();


                        if(UtilBeanImpl.isSeamfixAdmin(user.getUserId())){
                            logoutURLPath = defaultPath;
                        } else {

                            Person person = UserMgmtUtil.getPersonByOrbitaId(user.getUserId());

                            if(person != null){
                                Company company = person.getStaff().getCompany();

                                String companyLogoutURLPath = UtilBeanImpl.getSettingsValue(RomsConstants.LOGOUT_REDIRECT_URL_SETTING, 
                                        company, "/c/portal/logout?referer= ", "Defines the page users should be redirected to on logout", true);
                                
                                if(companyLogoutURLPath != null){
                                    logoutURLPath = companyLogoutURLPath;
                                }
                                else{
                                    log.warn("Company with id = "+company.getId()+" has no logout redirect setting");
                                }
                                
                            }
                            else{
                                log.error("orbita user with id = "
                                        +user.getUserId()
                                        +" is not a seamfix admin or person");
                            }
                        }
                    } else {
                        log.info("user is null");
                    }
                } catch (SystemException e) {
                    e.printStackTrace();
                    log.error((new StringBuilder()).append("Error: ").append(e.getMessage()).toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error((new StringBuilder()).append("Error ").append(e.getMessage()).toString());
                }
            } else {
                System.out.println("session is null");

            }
            log.info((new StringBuilder()).append("-----------------------logoutURLPath: ").append(logoutURLPath).append("-----------------------").toString());
            if (user != null) {
                try {
                    response.sendRedirect((new StringBuilder()).append("/c/portal/logout?referer= ").append(logoutURLPath).toString());
                } catch (IOException ex) {
                    log.error("io exception ", ex);
                }
            } else {
                log.info("------------------Using the default landing page from config---------------------");
                try {
                    log.info("user is null. USing default landing page");
                    setDefaultLandingPagePath(session);
                } catch (Exception ex) {
                    log.error("io exception ", ex);
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    private void setDefaultLandingPagePath(HttpSession session)
            throws Exception {
        String path = PropsUtil.get("DEFAULT_LANDING_PAGE_PATH");
        if (Validator.isNotNull(path)) {
            LastPath lastPath = new LastPath("", path, new HashMap());
            log.debug((new StringBuilder()).append("----------------Use System Config LastPath URL: ").append(lastPath.toString()).toString());
            log.info((new StringBuilder()).append("----------------Use System Config LastPath URL: ").append(lastPath.toString()).toString());
        } else {
            log.error("----------------Default Landing Page is Null-----------------");
        }
    }
}