package org.anodyneos.xp;

public class XpOutputKeys {

    private XpOutputKeys() {
        // don't allow instantiation
    }

    public static final String CDATA_SECTION_ELEMENTS = "cdataSectionElements";

    public static final String DOCTYPE_PUBLIC = "doctypePublic";

    public static final String DOCTYPE_SYSTEM = "doctypeSystem";

    /**
     * utf-8, iso-xxxx, etc
     */
    public static final String ENCODING = "encoding";

    public static final String INDENT = "indent";

    public static final String INDENT_AMOUNT = "indentAmount";

    /**
     * mime type
     */
    public static final String MEDIA_TYPE = "mediaType";

    /**
     * may be XML or HTML
     */
    public static final String METHOD = "method";

    public static final String OMIT_XML_DECLARATION = "omitXmlDeclaration";

    ///**
    // * performs browser checking and converts XHTML to HTML for non-XHTML browsers (IE 6.0, etc)
    // */
    //public static final String XHMTL_COMPAT = "xhtmlCompat";

    /**
     * url of XSLT stylesheet to use prior to output.
     */
    public static final String XSLT_URI = "xsltURI";

    public static final String EXCLUDE_RESULT_PREFIXES = "excludeResultPrefixes";

}
