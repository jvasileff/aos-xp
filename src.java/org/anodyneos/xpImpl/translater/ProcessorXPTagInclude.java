package org.anodyneos.xpImpl.translater;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
/**
 * @author yao
 */
public class ProcessorXPTagInclude extends TranslaterProcessor {

    ProcessorResultContent processorResultContent;
    public static final String E_PARAM = "param";
    public static final String A_FILE = "file";

    public ProcessorXPTagInclude(TranslaterContext ctx) {
        super(ctx);
        processorResultContent = new ProcessorResultContent(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {

        if (URI_XP.equals(uri) && E_PARAM.equals(localName)) {
            // TODO not yet implemented
            //ElementProcessor proc = new ProcessorXPTagIncludeParam(getTranslaterContext(), varName);
            //return proc;
            throw new SAXParseException(localName + " has not been implemented yet.", getContext().getLocator());
        } else {
            throw new SAXParseException(localName + " is not valid inside <include>.", getContext().getLocator());
        }
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();

        String file = attributes.getValue(A_FILE);

        if(null == file) {
            throw new SAXParseException("@file is a required attribute for <include>", getContext().getLocator());
        }

        getTranslaterContext().addDependent(file);
        file = stripExtension(file);
        if (file.startsWith("/") || file.startsWith("\\")){
            file = file.substring(1,file.length());
        }
        out.printIndent().println("new " +
                TranslaterContext.DEFAULT_PACKAGE + "." + file.replace('/','.') + "().service(xpContext,xpCH);");
        //out.printIndent().println("getIncludedPage(\"" + TranslaterContext.DEFAULT_PACKAGE + "." + file.replace('/','.')+
        //                                            "\").service(xpContext,xpCH);");
    }
    private static String stripExtension(String fileName){
        int dotXp = fileName.indexOf(".xp");
        if (dotXp != -1){
            return fileName.substring(0,dotXp);
        }else{
            return fileName;
        }
    }
    public static String getContextRelativePath(ServletRequest request,
            String relativePath) {

        if (relativePath.startsWith("/")){
            return (relativePath);
        }

        if (!(request instanceof HttpServletRequest)){
            return (relativePath);
        }
        HttpServletRequest hrequest = (HttpServletRequest) request;
        String uri = (String)
        request.getAttribute("javax.servlet.include.servlet_path");
        if (uri != null) {
            String pathInfo = (String)
            request.getAttribute("javax.servlet.include.path_info");
            if (pathInfo == null) {
                if (uri.lastIndexOf('/') >= 0){
                    uri = uri.substring(0, uri.lastIndexOf('/'));
                }
            }
        }
        else {
            uri = hrequest.getServletPath();
            if (uri.lastIndexOf('/') >= 0)
            {
                uri = uri.substring(0, uri.lastIndexOf('/'));
            }
        }
        return uri + '/' + relativePath;
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        processorResultContent.characters(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        processorResultContent.flushCharacters();
    }

}
