package org.anodyneos.xpImpl.runtime;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.el.FunctionMapper;

import org.anodyneos.xp.XpNamespaceMapper;

/**
 * @author jvas
 */
public class XpFunctionResolver {

    private Map<String, Map<String, Method>> uris = new HashMap<String, Map<String, Method>>();

    public void mapFunctionWithURI(String uri, String localName, Class clazz, String methodName, Class[] args) {

        Method method;
        try {
            method = clazz.getDeclaredMethod(methodName, args);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No such method: " + e.getMessage());
        }

        Map<String, Method> m = uris.get(uri);
        if (null == m) {
            m = new HashMap<String, Method>();
            uris.put(uri, m);
        }
        m.put(localName, method);
    }

    public Method resolveFunctionWithURI(String uri, String localName) {
        Map<String, Method> m = uris.get(uri);
        if (null == m) {
            return null;
        } else {
            return m.get(localName);
        }
    }

    public FunctionMapper getFunctionMapper(XpNamespaceMapper namespaceMapper) {
        // We could try to cache these objects with a WeakHashMap, but the we will have concurrency issues.
        return new XpFunctionMapper(namespaceMapper);
    }

    public class XpFunctionMapper implements FunctionMapper {

        private XpNamespaceMapper namespaceMapper;

        private XpFunctionMapper(XpNamespaceMapper namespaceMapper) {
            this.namespaceMapper = namespaceMapper;;
        }

        public Method resolveFunction(String prefix, String localName) {
            if (null == prefix) {
                prefix = "";
            }
            String uri = namespaceMapper.getURI(prefix);
            return resolveFunctionWithURI(uri, localName);
        }
    }

}
