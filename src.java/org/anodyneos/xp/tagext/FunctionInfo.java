/*
 * Created on Jan 26, 2005
 */
package org.anodyneos.xp.tagext;

/**
 * @author jvas
 */
public class FunctionInfo {

    private String functionClass;
    private String functionSignature;
    private String name;

    public FunctionInfo(String name, String functionClass, String functionSignature) {
        this.name = name;
        this.functionClass = functionClass;
        this.functionSignature = functionSignature;
    }

    /**
     * @return Returns the functionClass.
     */
    public String getFunctionClass() {
        return functionClass;
    }
    /**
     * @return Returns the functionSignature.
     */
    public String getFunctionSignature() {
        return functionSignature;
    }
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
}
