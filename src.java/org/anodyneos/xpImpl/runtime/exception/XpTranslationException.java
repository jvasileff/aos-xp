package org.anodyneos.xpImpl.runtime.exception;

public class XpTranslationException extends XpRuntimeException{
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3834032437427713081L;

    long lineNumber;

    public XpTranslationException(Throwable rootCause) {
        super(rootCause);
        // TODO Auto-generated constructor stub
    }

    public XpTranslationException(String message) {
        super(message);
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
}
