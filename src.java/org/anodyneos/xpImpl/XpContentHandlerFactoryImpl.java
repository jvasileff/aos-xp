package org.anodyneos.xpImpl;

import org.anodyneos.xp.XpContentHandler;
import org.anodyneos.xp.XpContentHandlerFactory;
import org.xml.sax.ContentHandler;

public class XpContentHandlerFactoryImpl extends XpContentHandlerFactory {

    public XpContentHandler getXpContentHandler(ContentHandler ch) {
        return new XpContentHandlerImpl(ch);
    }

}
