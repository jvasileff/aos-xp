package org.anodyneos.xp;

public class XpException extends Exception {

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
