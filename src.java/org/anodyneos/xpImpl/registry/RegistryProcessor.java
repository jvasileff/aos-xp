package org.anodyneos.xpImpl.registry;

class RegistryProcessor extends org.anodyneos.commons.xml.sax.ElementProcessor {

    private RegistryContext ctx;

    public RegistryProcessor(RegistryContext ctx) {
        super(ctx);
        this.ctx = ctx;
    }

    protected RegistryContext getRegistryContext() {
        return ctx;
    }

}
