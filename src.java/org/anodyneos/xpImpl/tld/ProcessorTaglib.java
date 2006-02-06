package org.anodyneos.xpImpl.tld;

import java.util.ArrayList;

import org.anodyneos.commons.xml.sax.CDATAProcessor;
import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xp.tagext.FunctionInfo;
import org.anodyneos.xp.tagext.TagInfo;
import org.anodyneos.xp.tagext.TagLibraryInfo;
import org.xml.sax.SAXException;

class ProcessorTaglib extends TLDProcessor {

    private TagLibraryInfo tagLibraryInfo;

    private CDATAProcessor descriptionProcessor;

    private ArrayList<ProcessorTag> tags = new ArrayList<ProcessorTag>();
    private ArrayList<ProcessorFunction> functions = new ArrayList<ProcessorFunction>();

    public static final String E_DESCRIPTION = "description";

    public static final String E_TAG = "tag";
    public static final String E_FUNCTION = "function";

    public ProcessorTaglib(TLDContext ctx) {
        super(ctx);
        descriptionProcessor = new CDATAProcessor(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName)
            throws SAXException {
        if (E_DESCRIPTION.equals(localName)) {
            return descriptionProcessor;
        } else if (E_TAG.equals(localName)) {
            ProcessorTag p = new ProcessorTag(getTLDContext());
            tags.add(p);
            return p;
        } else if (E_FUNCTION.equals(localName)) {
            ProcessorFunction p = new ProcessorFunction(getTLDContext());
            functions.add(p);
            return p;
        } else {
            return super.getProcessorFor(uri, localName, qName);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        TagInfo[] tarray = new TagInfo[tags.size()];
        for (int i = 0; i < tags.size(); i++) {
            tarray[i] = tags.get(i).getTagInfo();
        }

        FunctionInfo[] farray = new FunctionInfo[functions.size()];
        for (int i = 0; i < functions.size(); i++) {
            farray[i] = functions.get(i).getFunctionInfo();
        }

        tagLibraryInfo = new TagLibraryInfo(descriptionProcessor.getCDATA(), tarray, farray);

        descriptionProcessor = null;
        tags = null;
    }

    public TagLibraryInfo getTagLibraryInfo() {
        return tagLibraryInfo;
    }

}
