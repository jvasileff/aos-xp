package org.anodyneos.xpImpl.runtime;

import java.io.OutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;

import org.apache.fop.apps.Driver;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class FopOutputer {

    public static void outputFop(XMLReader xmlReader, Transformer trans, OutputStream out) throws TransformerException {

        Driver driver = new Driver();
        driver.setRenderer(Driver.RENDER_PDF);
        driver.setOutputStream(out);
        Source source = new SAXSource(xmlReader, new InputSource(""));
        trans.transform(source, new SAXResult(driver.getContentHandler()));

    }

}
