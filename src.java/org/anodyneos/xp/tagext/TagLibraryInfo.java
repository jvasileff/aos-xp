package org.anodyneos.xp.tagext;

import java.util.HashMap;

public class TagLibraryInfo {
    protected String description;
    protected HashMap tagInfos = new HashMap();

    public TagLibraryInfo(String description, TagInfo[] tagInfos) {
        this.description = description;
        if (tagInfos != null) {
            for (int i = 0; i < tagInfos.length; i++) {
                this.tagInfos.put(tagInfos[i].getName(), tagInfos[i]);
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
}
