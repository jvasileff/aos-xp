package org.anodyneos.xpImpl.translater;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

import org.anodyneos.commons.xml.sax.BaseContext;
import org.anodyneos.xp.tagext.TagLibraryRegistry;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.xml.sax.InputSource;

public class TranslaterContext extends BaseContext implements TranslaterResult {

    public static final String DEFAULT_PACKAGE = "xp";
    private CodeWriter mainCodeWriter;
    private TagLibraryRegistry taglibRegistry;
    private String className;
    private String packageName;
    private Stack fragmentCodeWriters = new Stack();
    private ArrayList fragments = new ArrayList();
    private int tagVariableCounter = 0;
    private int savedXPCHVariableCounter = 0;

    // A list of files on which this current file depends
    private List dependents = new ArrayList();

    public TranslaterContext(InputSource is, CodeWriter codeWriter, TagLibraryRegistry taglibRegistry) {
        super(is);
        this.mainCodeWriter = codeWriter;
        this.taglibRegistry = taglibRegistry;
    }

    public void setFullClassName(String fullClassName) {
        if (null == fullClassName) {
            this.packageName = null;
            this.className = null;
        } else {
            fullClassName = fullClassName.trim();
            int lastDot = fullClassName.lastIndexOf(".");
            if (lastDot != -1) {
                this.packageName = fullClassName.substring(0, lastDot);
                this.className = fullClassName.substring(lastDot + 1);
            } else {
                this.packageName = null;
                this.className = fullClassName;
            }
        }
    }

    public String getFullClassName() {
        if (packageName != null) {
            return packageName + "." + className;
        } else {
            return className;
        }
    }

    public void setPackageName(String packageName) {
        packageName = packageName.trim();
        this.packageName = packageName;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setClassName(String className) {
        className = className.trim();
        this.className = className;
    }

    public String getClassName() {
        return this.className;
    }

    /**
     * @return the current codewriter. Each fragment will have its own
     *         codewriter.
     */
    public CodeWriter getCodeWriter() {
        if (! fragmentCodeWriters.empty()) {
            return (CodeWriter) fragmentCodeWriters.peek();
        } else {
            return mainCodeWriter;
        }
    }

    public TagLibraryRegistry getTagLibraryRegistry() {
        return taglibRegistry;
    }

    public int startFragment() {
        int num = fragments.size();
        StringWriter sw = new StringWriter();
        fragments.add(sw);
        CodeWriter cw = new CodeWriter(sw);
        cw.indentPlus().indentPlus().indentPlus();
        fragmentCodeWriters.push(cw);
        return num;
    }

    public void endFragment() {
        CodeWriter cw = (CodeWriter) fragmentCodeWriters.pop();
        cw.flush();
        cw.close();
    }

    public int getFragmentCount() {
        return fragments.size();
    }

    public String getFragment(int index) {
        return ((StringWriter) fragments.get(index)).toString();
    }

    public String getVariableForTag(String className) {
        return "tag" + tagVariableCounter++;
    }

    public String getVariableForSavedXPCH() {
        return "savedXPCH" + savedXPCHVariableCounter++;
    }

    public boolean inFragment() {
        return fragmentCodeWriters.size() > 0;
    }

    public void addDependent(String dependent){
        dependents.add(dependent);
    }
    public List getDependents(){
        return Collections.unmodifiableList(dependents);
    }

}
