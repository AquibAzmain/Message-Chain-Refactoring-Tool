package visitors;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import dataStructure.OurClass;
import dataStructure.OurMethod;

import java.util.List;

public class MethodNameCollector extends VoidVisitorAdapter<List<OurMethod>> {
    OurClass currClass;
    List<OurClass> classes;

    public MethodNameCollector(OurClass ourClass, List<OurClass> classes){
        currClass = ourClass;
        this.classes = classes;
    }

    @Override
    public void visit(MethodDeclaration md, List<OurMethod> collector) {
        super.visit(md, collector);
        OurMethod currMethod = new OurMethod(md.getName().asString(), md.getDeclarationAsString(), currClass);
        currMethod.setType(findType(md));
        currMethod.setMd(md);
        collector.add(currMethod);

        System.out.println(currMethod);
    }

    private OurClass findType(MethodDeclaration md) {
        String type = md.getType().asString();
        for(OurClass clazz : classes){
            if(clazz.getName().equals(type)) {
                return clazz;
            }
        }
        return null;
    }
}