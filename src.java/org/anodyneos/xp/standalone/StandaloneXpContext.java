package org.anodyneos.xp.standalone;

import org.anodyneos.xp.XpContext;

public abstract class StandaloneXpContext implements XpContext {

    public static final int GLOBAL_SCOPE = 2;

    public static final String GLOBAL_SCOPE_STRING = "global";

    public abstract void initialize();
}
