package org.anodyneos.xp.tagext;


public class TagInfo {
    protected String name;
    protected String description;
    protected String className;
    protected String tagFile;
    protected TagVariableInfo[] tagVariableInfos;
    protected TagAttributeInfo[] tagAttributeInfos;

    public TagInfo(
            String name, String description, String className, String tagFile,
            TagVariableInfo[] tagVariableInfos, TagAttributeInfo[] tagAttributeInfos) {
        this.name = name;
        this.description = description;
        this.className = className;
        this.tagFile = tagFile;
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
    public String getTagFile() {
        return tagFile;
    }

    public TagVariableInfo[] getTagVariableInfos() {
        return tagVariableInfos;
    }
    public TagAttributeInfo[] getTagAttributeInfos() {
        return tagAttributeInfos;
    }
}
