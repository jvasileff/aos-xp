package org.anodyneos.xpImpl.translater;

import java.util.List;

public interface TranslaterResult {
    /**
     *  @return "package.class" or "class" if no package.
     */
    String getFullClassName();

    /**
     *  @return Package name or null.
     */
    String getPackageName();

    /**
     *  @return Class name or null.
     */
    String getClassName();

    /**
     *  @return dependents or empty list
     */
    List getDependents();

}
