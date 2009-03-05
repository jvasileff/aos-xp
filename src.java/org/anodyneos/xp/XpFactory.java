package org.anodyneos.xp;

import java.net.URI;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;

public abstract class XpFactory {

    private static final String DEFAULT_FACTORY = "org.anodyneos.xpImpl.XpFactoryImpl";

    public static XpFactory newInstance() {
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
                XpFactory xpFactory = (XpFactory) xpFactoryClass.newInstance();
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

    public abstract XpPage getXpPage(URI uri);

    //public abstract X

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
