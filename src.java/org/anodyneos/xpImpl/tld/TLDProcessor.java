package org.anodyneos.xpImpl.tld;

import org.anodyneos.commons.xml.sax.ElementProcessor;

class TLDProcessor extends ElementProcessor {

    private TLDContext ctx;

    public TLDProcessor(TLDContext ctx) {
        super(ctx);
        this.ctx = ctx;
    }

    protected TLDContext getTLDContext() {
        return ctx;
    }

}
