package org.anodyneos.xp.http;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public abstract class HttpXpFactory {
    private static final String DEFAULT_FACTORY = "org.anodyneos.xpImpl.runtime.HttpXpFactoryImpl";

    protected HttpXpFactory() {
        // super();
    }

    public static HttpXpFactory getDefaultFactory() {
        ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
        ClassLoader cl2 = HttpXpFactory.class.getClassLoader();

        Class xpFactoryClass = null;
        if (null != cl1) {
            xpFactoryClass = loadClass(cl1, DEFAULT_FACTORY);
        }
        if (null == xpFactoryClass) {
            xpFactoryClass = loadClass(cl2, DEFAULT_FACTORY);
        }
        if (null == xpFactoryClass) {
            return null;
        } else {
            try {
                HttpXpFactory xpFactory = (HttpXpFactory) xpFactoryClass.newInstance();
                return xpFactory;
            } catch (InstantiationException e) {
                return null;
            } catch (IllegalAccessException e) {
                return null;
            }
        }
    }

    private static Class loadClass(ClassLoader cl, String name) {
        try {
            Class clazz = cl.loadClass(name);
            return clazz;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public abstract HttpXpContext getHttpXpContext(Servlet iServlet, ServletRequest iServletRequest,
            ServletResponse iServletResponse);

}
