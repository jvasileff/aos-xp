package org.anodyneos.xp.http;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

import org.anodyneos.xp.XpContext;

public interface HttpXpContext extends XpContext {

    public static final int REQUEST_SCOPE = 10;
    public static final int SESSION_SCOPE = 11;

    public static final String REQUEST_SCOPE_STRING = "request";
    public static final String SESSION_SCOPE_STRING = "session";

    public abstract ServletRequest getRequest();
    public abstract ServletResponse getResponse();
    public abstract ServletConfig getServletConfig();
    public abstract ServletContext getServletContext();
    public abstract HttpSession getSession();

}
