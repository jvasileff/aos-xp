package org.anodyneos.xpImpl.tld;

import java.util.ArrayList;

import org.anodyneos.commons.xml.sax.CDATAProcessor;
import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xp.tagext.TagAttributeInfo;
import org.anodyneos.xp.tagext.TagInfo;
import org.anodyneos.xp.tagext.TagVariableInfo;
import org.xml.sax.SAXException;

class ProcessorTag extends TLDProcessor {

    private TagInfo tagInfo;
    private CDATAProcessor nameProcessor;
    private CDATAProcessor descriptionProcessor;
    private CDATAProcessor tagClassProcessor;
    private ArrayList<ProcessorVariable> variables = new ArrayList<ProcessorVariable>();
    private ArrayList<ProcessorAttribute> attributes = new ArrayList<ProcessorAttribute>();

    public static final String E_NAME = "name";
    public static final String E_DESCRIPTION = "description";
    public static final String E_TAG_CLASS = "tag-class";
    public static final String E_VARIABLE = "variable";
    public static final String E_ATTRIBUTE = "attribute";

    public ProcessorTag(TLDContext ctx) {
        super(ctx);
        nameProcessor = new CDATAProcessor(ctx);
        descriptionProcessor = new CDATAProcessor(ctx);
        tagClassProcessor = new CDATAProcessor(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName)
            throws SAXException {
        if (E_NAME.equals(localName)) {
            return nameProcessor;
        } else if (E_DESCRIPTION.equals(localName)) {
            return descriptionProcessor;
        } else if (E_TAG_CLASS.equals(localName)) {
            return tagClassProcessor;
        } else if (E_VARIABLE.equals(localName)) {
            ProcessorVariable p = new ProcessorVariable(getTLDContext());
            variables.add(p);
            return p;
        } else if (E_ATTRIBUTE.equals(localName)) {
            ProcessorAttribute p = new ProcessorAttribute(getTLDContext());
            attributes.add(p);
            return p;
        } else {
            return super.getProcessorFor(uri, localName, qName);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        // variables
        TagVariableInfo[] vars = new TagVariableInfo[variables.size()];
        for (int i = 0; i < variables.size(); i++) {
            vars[i] = variables.get(i).getTagVariableInfo();
        }

        // attributes
        TagAttributeInfo[] attrs = new TagAttributeInfo[attributes.size()];
        for (int i = 0; i < attributes.size(); i++) {
            attrs[i] = attributes.get(i).getTagAttributeInfo();
        }

        tagInfo = new TagInfo(nameProcessor.getCDATA(), descriptionProcessor.getCDATA(),
                tagClassProcessor.getCDATA(), vars, attrs);

        nameProcessor = null;
        descriptionProcessor = null;
        tagClassProcessor = null;
        variables = null;
        attributes = null;
    }

    public TagInfo getTagInfo() {
        return tagInfo;
    }

}
