package visitors;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import dataStructure.OurClass;
import dataStructure.OurVariable;

import java.util.List;

public class InstanceFieldCollector extends VoidVisitorAdapter<List<OurVariable>> {
    private List<OurClass> classes;

    public InstanceFieldCollector(List<OurClass> classes){
        this.classes = classes;
    }

    @Override
    public void visit(FieldDeclaration fd, List<OurVariable>collector) {
        super.visit(fd, collector);

        for(VariableDeclarator vd: fd.getVariables()){
            for(OurClass aClass: classes){
                if(aClass.getName().equals(vd.getTypeAsString())){
                    OurVariable field = new OurVariable(vd.getNameAsString(), aClass);
                    collector.add(field);
                }
            }
        }
    }
}