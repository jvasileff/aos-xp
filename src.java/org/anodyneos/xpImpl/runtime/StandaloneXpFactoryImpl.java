package org.anodyneos.xpImpl.runtime;

import org.anodyneos.xp.standalone.StandaloneXpContext;
import org.anodyneos.xp.standalone.StandaloneXpFactory;

public class StandaloneXpFactoryImpl extends StandaloneXpFactory {

    public StandaloneXpFactoryImpl() {
        // super();
    }

    @Override
    public StandaloneXpContext getStandaloneXpContext() {
        StandaloneXpContext xpCtx = new StandaloneXpContextImpl();
        return xpCtx;
    }

}

