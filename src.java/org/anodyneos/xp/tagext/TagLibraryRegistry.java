package org.anodyneos.xp.tagext;

import java.util.HashMap;

public class TagLibraryRegistry {
    protected HashMap tagLibraryInfos = new HashMap();

    public TagLibraryRegistry() {
        // super();
    }

    public TagLibraryInfo getTagLibraryInfo(String uri) {
        return (TagLibraryInfo) tagLibraryInfos.get(uri);
    }

    public TagLibraryInfo[] getTagLibraryInfos() {
        if (tagLibraryInfos.size() == 0) {
            return null;
        } else {
            return (TagLibraryInfo[])
                    tagLibraryInfos.values().toArray(new TagLibraryInfo[tagLibraryInfos.size()]);
        }
    }
}
