package org.anodyneos.xpImpl.runtime;

import java.util.List;
import java.util.Map;

import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.standalone.StandaloneXpContext;

/**
 * @author jvas
 */
public final class StandaloneXpContextWrapper extends XpContextWrapperA implements StandaloneXpContext {

    public StandaloneXpContextWrapper(StandaloneXpContext wrappedContext,
            List<String> nestedVars, List<String> atBeginVars, List<String> atEndVar,
            Map<String, String> aliases) {
        super(wrappedContext, nestedVars, atBeginVars, atEndVar, aliases);
    }

    @Override
    public XpContext wrap(List<String> nestedVars, List<String> atBeginVars, List<String> atEndVar, Map<String, String> aliases) {
        return new StandaloneXpContextWrapper(this, nestedVars, atBeginVars, atEndVar, aliases);
    }

}
