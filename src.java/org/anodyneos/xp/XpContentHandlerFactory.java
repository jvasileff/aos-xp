package org.anodyneos.xp;

import org.xml.sax.ContentHandler;

public abstract class XpContentHandlerFactory {
    private static final String DEFAULT_FACTORY = "org.anodyneos.xpImpl.XpContentHandlerFactoryImpl";

    public static XpContentHandlerFactory getDefaultFactory() {
        ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
        ClassLoader cl2 = XpContentHandlerFactory.class.getClassLoader();

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
                XpContentHandlerFactory xpFactory = (XpContentHandlerFactory) xpFactoryClass.newInstance();
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

    public abstract XpContentHandler getXpContentHandler(ContentHandler ch);
    //public abstract void releaseHttpXpContext(HttpXpContext xpContext);

}
