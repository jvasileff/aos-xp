package org.anodyneos.xpImpl.tagext;

import java.io.IOException;

import org.anodyneos.xp.tagext.TagLibraryInfo;
import org.anodyneos.xp.tagext.TagLibraryRegistry;
import org.anodyneos.xpImpl.tld.TLDParser;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TagLibraryRegistryImpl extends TagLibraryRegistry {

    private TLDParser tldParser = new TLDParser();

    private EntityResolver resolver;

    public TagLibraryRegistryImpl(EntityResolver resolver) {
        this.resolver = resolver;
    }

    public void addTaglib(String uri, String location) throws SAXException, IOException {
        InputSource is = resolver.resolveEntity(null, location);
        if (is == null) {
            is = new InputSource(location);
        }
        TagLibraryInfo tldInfo = tldParser.process(is, resolver);
        tagLibraryInfos.put(uri, tldInfo);
    }

}
