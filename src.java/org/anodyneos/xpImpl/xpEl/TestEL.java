package org.anodyneos.xpImpl.xpEl;

import java.util.HashMap;

import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.FunctionMapper;
import javax.servlet.jsp.el.VariableResolver;

import org.apache.commons.el.ExpressionEvaluatorImpl;

public class TestEL {

    public static void main(String[] args) throws Exception {
        HashMap<String, Object> vars = new HashMap<String, Object>();
        VariableResolver variableResolver = new SimpleVariableResolver(vars);
        ExpressionEvaluator sEvaluator = new ExpressionEvaluatorImpl();

        vars.put("str", "${8 + 2}");
        vars.put("num", new Integer(8));
        HashMap<String, Object> pc = new HashMap<String, Object>();
        pc.put("str", "in_pc");
        vars.put("pageContext", pc);

        String pAttributeValue;
        pAttributeValue = "${2 + 2}";
        pAttributeValue = "${'${8 + 2}'} = ${num / 3}";
        pAttributeValue = "${str} = ${num + 2}";
        pAttributeValue = "${pageContext.str} = something";
        Class pExpectedType = String.class;
        FunctionMapper functionMapper = null;

        System.out.println(
                sEvaluator.evaluate
                        (pAttributeValue,
                         pExpectedType,
                         variableResolver,
                         functionMapper));


        // sEvaluator.parseExpressionString (pAttributeValue);

    }

    public static class SimpleVariableResolver implements VariableResolver {
        private HashMap vars;
        public SimpleVariableResolver(HashMap vars) {
            this.vars = vars;
        }

        public Object resolveVariable(String pName) {
            return vars.get(pName);
        }

        //public Object resolveVariable(String pName, Object cContext) {
        //    return vars.get(pName);
        //}
    }

}
