package org.anodyneos.xpImpl.translater;

import org.anodyneos.xpImpl.util.CodeWriter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * @author jvas
 */
public class HelperProcessorNonResultContent extends TranslaterProcessor {

    private int phantomPrefixCount = 0;

    public HelperProcessorNonResultContent(TranslaterContext ctx) {
        super(ctx);
    }

    public final void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();
        phantomPrefixCount =  outputBufferedMappingsAsPhantoms();

        startElementNonResultContent(uri, localName, qName, attributes);
    }

    public final void endElement(String uri, String localName, String qName) throws SAXException {
        endElementNonResultContent(uri, localName, qName);

        CodeWriter out = getTranslaterContext().getCodeWriter();
        for (int i=0; i < phantomPrefixCount; i++) {
            out.printIndent().println("xpCH.popPhantomPrefixMapping();");
        }
    }

    /**
     * Subclasses should override this function.
     */
    public void startElementNonResultContent(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
    }

    /**
     * Subclasses should override this function.
     */
    public void endElementNonResultContent(String uri, String localName, String qName)
            throws SAXException {
    }
}
