package org.anodyneos.xpImpl.tld;

import java.io.IOException;

import org.anodyneos.commons.xml.sax.BaseParser;
import org.anodyneos.xp.tagext.TagLibraryInfo;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TLDParser extends BaseParser {

    public TLDParser() {
        // super
    }

    public TagLibraryInfo process(InputSource is, EntityResolver resolver) throws SAXException,
            IOException {
        TLDContext ctx = new TLDContext(is);
        ProcessorTaglib p = new ProcessorTaglib(ctx);
        process(is, p, resolver);
        return p.getTagLibraryInfo();
    }

}
