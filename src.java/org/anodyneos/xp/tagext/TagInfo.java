package org.anodyneos.xp.tagext;


public class TagInfo {
    protected String name;
    protected String description;
    protected String className;
    protected TagVariableInfo[] tagVariableInfos;
    protected TagAttributeInfo[] tagAttributeInfos;

    public TagInfo(
            String name, String description, String className,
            TagVariableInfo[] tagVariableInfos, TagAttributeInfo[] tagAttributeInfos) {
        this.name = name;
        this.description = description;
        this.className = className;
        this.tagVariableInfos = tagVariableInfos;
        this.tagAttributeInfos = tagAttributeInfos;
    }

    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public String getClassName() {
        return className;
    }

    public TagVariableInfo[] getTagVariableInfos() {
        return tagVariableInfos;
    }
    public TagAttributeInfo[] getTagAttributeInfos() {
        return tagAttributeInfos;
    }
}
