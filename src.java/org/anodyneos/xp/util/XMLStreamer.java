package org.anodyneos.xp.util;

import java.io.PrintStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.xml.sax.XMLReader;

public class XMLStreamer {

    public static void process(XMLReader xmlReader, PrintStream os) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();

        // TODO: follow setting from <xp:output> tag.
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        transformer.transform(
                new javax.xml.transform.sax.SAXSource(xmlReader, new org.xml.sax.InputSource("")),
                new javax.xml.transform.stream.StreamResult(os));
    }

}
