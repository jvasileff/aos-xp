package org.anodyneos.xpImpl.runtime;

import org.anodyneos.xp.standalone.StandaloneXpContext;
import org.anodyneos.xp.standalone.StandaloneXpFactory;

public class StandaloneXpContextFactoryImpl extends StandaloneXpFactory {

    // TODO: pool?

    public StandaloneXpContextFactoryImpl() {
        // super();
    }

    public StandaloneXpContext getStandaloneXpContext() {
        StandaloneXpContext xpCtx = new StandaloneXpContextImpl();
        return xpCtx;
    }

}

