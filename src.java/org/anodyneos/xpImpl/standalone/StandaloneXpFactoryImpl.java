package org.anodyneos.xpImpl.standalone;

import org.anodyneos.xp.standalone.StandaloneXpContext;
import org.anodyneos.xp.standalone.StandaloneXpFactory;

public class StandaloneXpFactoryImpl extends StandaloneXpFactory {

    // TODO: pool?

    public StandaloneXpFactoryImpl() {
        // super();
    }

    public StandaloneXpContext getStandaloneXpContext() {
        StandaloneXpContext xpCtx = new StandaloneXpContextImpl();
        xpCtx.initialize();
        return xpCtx;
    }

    public void releaseStandaloneXpContext(StandaloneXpContext xpContext) {
        xpContext.release();
    }

}

