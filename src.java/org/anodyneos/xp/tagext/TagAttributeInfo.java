package org.anodyneos.xp.tagext;


public class TagAttributeInfo {
    protected String name;
    protected String description;
    protected String type;
    protected boolean required;
    protected boolean requestTimeOK;
    protected boolean isFragment;

    public TagAttributeInfo(
            String name, String description, String typeName,
            boolean required, boolean requestTimeOK, boolean isFragment) {
        this.name = name;
        this.description = description;
        this.type = typeName;
        this.required = required;
        this.requestTimeOK = requestTimeOK;
        this.isFragment = isFragment;
    }

    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public String getType() {
        return type;
    }
    public boolean isRequired() {
        return required;
    }
    public boolean isRequestTimeOK() {
        return requestTimeOK;
    }
}
