package org.anodyneos.xp.standalone;

public abstract class StandaloneXpFactory {
    private static final String DEFAULT_FACTORY = "org.anodyneos.xpImpl.standalone.StandaloneXpFactoryImpl";

    public static StandaloneXpFactory getDefaultFactory() {
        ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
        ClassLoader cl2 = StandaloneXpFactory.class.getClassLoader();

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
                StandaloneXpFactory xpFactory = (StandaloneXpFactory) xpFactoryClass.newInstance();
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

    public abstract StandaloneXpContext getStandaloneXpContext(org.xml.sax.ContentHandler ch);
    public abstract void releaseStandaloneXpContext(StandaloneXpContext xpContext);

}
