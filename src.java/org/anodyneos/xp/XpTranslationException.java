package org.anodyneos.xp;

import org.xml.sax.SAXParseException;

public class XpTranslationException extends Exception{
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3834032437427713081L;

    long lineNumber = -1;
    long colNumber = -1;
    String fileName;
    String internalMessage;

    public XpTranslationException(Throwable rootCause) {
        super(rootCause);
        // if it's a SAXParseException, let's take whatever info we can from it
        if (rootCause instanceof SAXParseException) {

            SAXParseException saxException = (SAXParseException) rootCause;
            setLineNumber(saxException.getLineNumber());
            setColNumber(saxException.getColumnNumber());
            setFileName(saxException.getSystemId());

            // generate an informative/useful error message
            StringBuffer retVal = new StringBuffer("XpTranslationException: ");
            retVal.append(rootCause.getMessage());
            retVal.append("  [File: ");
            retVal.append(getFileName());
            retVal.append("] [Line: ");
            retVal.append(getLineNumber());
            retVal.append("] [Column: ");
            retVal.append(getColNumber());
            retVal.append("] ");
            setInternalMessage(retVal.toString());
        }
    }

    public XpTranslationException(String message) {
        super(message);
    }

    public String getMessage(){

        String myMessage = getInternalMessage();

        if (myMessage == null) {
            myMessage = super.getMessage();
        }

        return myMessage;
    }

    /**
     * @return Returns the lineNumber.
     */
    public long getLineNumber() {
        return lineNumber;
    }
    /**
     * @param lineNumber The lineNumber to set.
     */
    public void setLineNumber(long lineNumber) {
        this.lineNumber = lineNumber;
    }
    /**
     * @return Returns the colNumber.
     */
    public long getColNumber() {
        return colNumber;
    }
    /**
     * @param colNumber The colNumber to set.
     */
    public void setColNumber(long colNumber) {
        this.colNumber = colNumber;
    }
    /**
     * @return Returns the fileName.
     */
    public String getFileName() {
        return fileName;
    }
    /**
     * @param fileName The fileName to set.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    /**
     * @return Returns the internalMessage.
     */
    public String getInternalMessage() {
        return internalMessage;
    }
    /**
     * @param internalMessage The internalMessage to set.
     */
    private void setInternalMessage(String internalMessage) {
        this.internalMessage = internalMessage;
    }
}
