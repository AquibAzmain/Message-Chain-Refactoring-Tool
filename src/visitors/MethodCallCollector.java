package visitors;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import dataStructure.OurMessageChain;
import dataStructure.OurMethod;
import dataStructure.OurVariable;
import main.MyUtils;

import java.util.List;

public class MethodCallCollector extends VoidVisitorAdapter<List<OurMessageChain>> {
    private OurMethod currMethod;

    public MethodCallCollector(OurMethod currMethod) {
        this.currMethod = currMethod;
    }

    @Override
    public void visit(MethodCallExpr mce, List<OurMessageChain> msgChains){
        if(mce.getScope().isPresent()){
            String scope = mce.getScope().get().toString();
            String[] scopeElements = MyUtils.splitScope(scope);
            if(isVariable(scopeElements[0]) && scopeElements.length>=2) {
                msgChains.add(new OurMessageChain(mce, scope, currMethod, scopeElements.length));
            }
        }
    }

    private boolean isVariable(String scope) {
        if(scope.endsWith(")"))
            return false;
        for(OurVariable currVar : currMethod.getVariables()){
            if(scope.equals(currVar.getName()))
                return true;
        }

        for(OurVariable currVar: currMethod.getParentClass().getFields()){
            if(scope.equals(currVar.getName()))
                return true;
        }

        return false;
    }
}
