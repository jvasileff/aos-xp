package org.anodyneos.xp.tagext;

import java.util.HashMap;

public class TagLibraryInfo {
    protected String description;
    protected HashMap tagInfos = new HashMap();
    protected HashMap functionInfos = new HashMap();

    public TagLibraryInfo(String description, TagInfo[] tagInfos, FunctionInfo[] functionInfos) {
        this.description = description;
        if (tagInfos != null) {
            for (int i = 0; i < tagInfos.length; i++) {
                this.tagInfos.put(tagInfos[i].getName(), tagInfos[i]);
            }
        }

        if (functionInfos != null) {
            for (int i = 0; i < functionInfos.length; i++) {
                this.functionInfos.put(functionInfos[i].getName(), functionInfos[i]);
            }
        }
    }

    public String getDescription() {
        return description;
    }
    public TagInfo getTagInfo(String name) {
        return (TagInfo) tagInfos.get(name);
    }

    public TagInfo[] getTagInfos() {
        if (tagInfos.size() == 0) {
            return null;
        } else {
            return (TagInfo[])
                    tagInfos.values().toArray(new TagInfo[tagInfos.size()]);
        }
    }

    public FunctionInfo getFunctionInfo(String name) {
        return (FunctionInfo) functionInfos.get(name);
    }

    public FunctionInfo[] getFunctionInfos() {
        if (functionInfos.size() == 0) {
            return null;
        } else {
            return (FunctionInfo[])
                    functionInfos.values().toArray(new FunctionInfo[functionInfos.size()]);
        }
    }
}
