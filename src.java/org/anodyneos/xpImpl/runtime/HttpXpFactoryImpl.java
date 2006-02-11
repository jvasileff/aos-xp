package org.anodyneos.xpImpl.runtime;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.anodyneos.xp.http.HttpXpContext;
import org.anodyneos.xp.http.HttpXpFactory;

public class HttpXpFactoryImpl extends HttpXpFactory {

    public HttpXpFactoryImpl() {
        // super();
    }

    @Override
    public HttpXpContext getHttpXpContext(Servlet iServlet, ServletRequest iServletRequest,
            ServletResponse iServletResponse) {
        return new HttpXpContextImpl(iServlet, iServletRequest, iServletResponse);
    }

}
