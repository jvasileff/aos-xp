package org.anodyneos.xp;

public class XpException extends Exception {

    private static final long serialVersionUID = 3904960871644935733L;

    public XpException(String msg) {
        super(msg);
    }

    public XpException(Throwable cause) {
        super(cause);
    }

    public XpException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
