package org.anodyneos.xp.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.anodyneos.servlet.email.ADataSource;
import org.anodyneos.xp.XpCompilationException;
import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpFileNotFoundException;
import org.anodyneos.xp.XpPage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XpDataSource extends ADataSource {

    private static Log log = LogFactory.getLog(XpDataSource.class);

    private XpContext xpCtx;
    private XpPage xpPage;

    public XpDataSource(XpContext xpCtx, XpPage xpPage) throws XpCompilationException, XpFileNotFoundException,
            XpException {
        this.xpCtx = xpCtx;
        this.xpPage = xpPage;
    }

    public InputStream getInputStream() throws IOException {
        ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try {
            xpPage.service(xpCtx, baos);
        } catch (XpException e) {
            // TODO better exception handling, IOException doesn't take a parent
            log.error(e);
            throw new IOException(e.getMessage());
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }

    public OutputStream getOutputStream() throws java.io.IOException {
        throw new java.lang.UnsupportedOperationException();
    }

    public final String getCharset() {
        return xpPage.getEncoding();
    }

    public final void setCharset(String charset) {
        xpPage.setEncoding(charset);
    }

    public final String getMimeType() {
        return xpPage.getMediaType();
    }

    public final void setMimeType(String mimeType) {
        xpPage.setMediaType(mimeType);
    }

    public XpContext getXpCtx() {
        return xpCtx;
    }

    public void setXpCtx(XpContext xpCtx) {
        this.xpCtx = xpCtx;
    }

}
