package org.anodyneos.xp.tagext;

import java.util.HashMap;
import java.util.Set;

public class TagLibraryRegistry {
    protected HashMap tagLibraryInfos = new HashMap();

    public TagLibraryRegistry() {
        // super();
    }

    public TagLibraryInfo getTagLibraryInfo(String uri) {
        return (TagLibraryInfo) tagLibraryInfos.get(uri);
    }

    public String[] getURIs() {
        Set keys =  tagLibraryInfos.keySet();
        return (String[]) keys.toArray(new String[keys.size()]);
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
