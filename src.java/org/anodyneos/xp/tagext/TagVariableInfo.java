package org.anodyneos.xp.tagext;


public class TagVariableInfo {
    public static final int SCOPE_NESTED = 1;
    public static final int SCOPE_AT_BEGIN = 2;
    public static final int SCOPE_AT_END = 3;

    protected String description;
    protected String nameFromAttribute;
    protected String alias;
    protected int scope;

    public TagVariableInfo(
            String description, String nameFromAttribute,
            String alias, int scope) {
        this.description = description;
        this.nameFromAttribute = nameFromAttribute;
        this.alias = alias;
        this.scope = scope;
    }

    public String getDescription() {
        return description;
    }
    public String getNameFromAttribute() {
        return nameFromAttribute;
    }
    public String getAlias() {
        return alias;
    }
    public int getScope() {
        return scope;
    }
}
