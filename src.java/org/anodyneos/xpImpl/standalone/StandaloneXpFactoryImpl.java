package org.anodyneos.xpImpl.standalone;

import org.anodyneos.xp.XpContentHandler;
import org.anodyneos.xp.standalone.StandaloneXpContext;
import org.anodyneos.xp.standalone.StandaloneXpFactory;

public class StandaloneXpFactoryImpl extends StandaloneXpFactory {

    // todo: pool?

    public StandaloneXpFactoryImpl() {
        // super();
    }

    public StandaloneXpContext getStandaloneXpContext(org.xml.sax.ContentHandler ch) {
        StandaloneXpContext xpCtx = new StandaloneXpContextImpl();
        xpCtx.initialize(new XpContentHandler(ch));
        return xpCtx;
    }

    public void releaseStandaloneXpContext(StandaloneXpContext xpContext) {
        xpContext.release();
    }

}

