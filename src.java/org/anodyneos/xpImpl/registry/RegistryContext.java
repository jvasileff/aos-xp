package org.anodyneos.xpImpl.registry;


import org.anodyneos.commons.xml.sax.BaseContext;
import org.anodyneos.xpImpl.tagext.TagLibraryRegistryImpl;
import org.xml.sax.InputSource;

public class RegistryContext extends BaseContext {

    private TagLibraryRegistryImpl registry;

    public RegistryContext(InputSource inputSource, TagLibraryRegistryImpl registry) {
        super(inputSource);
        this.registry = registry;
    }

    public TagLibraryRegistryImpl getRegistry() {
        return registry;
    }
}
