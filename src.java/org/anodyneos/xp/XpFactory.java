package org.anodyneos.xp;

import java.io.File;
import java.net.URI;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;

import org.anodyneos.commons.xml.UnifiedResolver;
import org.anodyneos.commons.xml.xsl.TemplatesCache;

public abstract class XpFactory {

    private static final String DEFAULT_FACTORY = "org.anodyneos.xpImpl.XpFactoryImpl";

    public static XpFactory newInstance() throws XpException {
        Class xpFactoryClass = null;

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        if (null != cl) {
            xpFactoryClass = loadClass(cl, DEFAULT_FACTORY);
        }

        if (null == xpFactoryClass) {
            cl = XpFactory.class.getClassLoader();
            xpFactoryClass = loadClass(cl, DEFAULT_FACTORY);
        }

        if (null == xpFactoryClass) {
            throw new XpException("Cannot find factory class " + DEFAULT_FACTORY);
        } else {
            try {
                XpFactory xpFactory = (XpFactory) xpFactoryClass.newInstance();
                return xpFactory;
            } catch (InstantiationException e) {
                throw new XpException("Cannot create factory class " + DEFAULT_FACTORY, e);
            } catch (IllegalAccessException e) {
                throw new XpException("Cannot create factory class " + DEFAULT_FACTORY, e);
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

    // factory methods

    public abstract XpPage newXpPage(URI xpURI)
    throws XpFileNotFoundException, XpTranslationException, XpCompilationException, XpException;

    // getters and setters

    public abstract ClassLoader getParentLoader();
    public abstract void setParentLoader(ClassLoader parentLoader);

    public abstract File getClassRootDirectory();
    public abstract void setClassRootDirectory(File classRoot);

    public abstract File getJavaRootDirectory();
    public abstract void setJavaRootDirectory(File javaRoot);

    public abstract URI getXpRegistryURI();
    public abstract void setXpRegistryURI(URI xpRegistry);

    public abstract UnifiedResolver getResolver();
    public abstract void setResolver(UnifiedResolver resolver);

    public abstract TemplatesCache getTemplatesCache();
    public abstract void setTemplatesCache(TemplatesCache templatesCache);

    public abstract boolean isAutoLoad();
    public abstract void setAutoLoad(boolean autoLoad);

    public abstract String getCompileClassPath();

    private void test() throws Exception {
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        tf.setErrorListener(null);
        Templates templates = tf.newTemplates(null);
        Transformer trans = templates.newTransformer();
        trans.setParameter("name", "value");

        tf.newXMLFilter(templates);
        //templates.tf.new
    }

}
