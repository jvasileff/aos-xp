package org.anodyneos.xpImpl.runtime.exception;

public class XpRuntimeException extends Exception{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    private Throwable rootCause;

    public XpRuntimeException(){
        super();
    }
    public XpRuntimeException(String message){
        super(message);
    }

    public XpRuntimeException(Throwable rootCause){
        this.rootCause = rootCause;
    }

    /**
     * @return Returns the rootCause.
     */
    public Throwable getRootCause() {
        return rootCause;
    }
    /**
     * @param rootCause The rootCause to set.
     */
    public void setRootCause(Throwable rootCause) {
        this.rootCause = rootCause;
    }
}
