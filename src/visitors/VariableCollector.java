package visitors;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import dataStructure.OurClass;
import dataStructure.OurMethod;
import dataStructure.OurVariable;

import java.util.List;

public class VariableCollector extends VoidVisitorAdapter<List<OurVariable>> {
    OurMethod currMethod;
    List<OurClass> allClasses;

    public VariableCollector(OurMethod currMethod, List<OurClass> allClasses){
        this.currMethod = currMethod;
        this.allClasses = allClasses;
    }

    @Override
    public void visit(VariableDeclarator vd, List<OurVariable> collector){
        super.visit(vd, collector);

        for(OurClass aClass: allClasses){
            if(aClass.getName().equals(vd.getTypeAsString())){
                OurVariable var = new OurVariable(vd.getNameAsString(), aClass);
                collector.add(var);
            }
        }
    }
}