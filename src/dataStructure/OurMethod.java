package dataStructure;

import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class OurMethod {
    private String name;
    private String signature;
    private OurClass parentClass;
    private OurClass type;
    private String typeString;

    private MethodDeclaration md;

    private List<OurVariable> variables;

    //constructors

    public OurMethod() {
        variables = new ArrayList<>();
    }

    public OurMethod(String name, String signature, OurClass parentClass) {
        this();
        this.name = name;
        this.signature = signature;
        this.parentClass = parentClass;
    }

    // methods

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof OurMethod){
            OurMethod method2 = (OurMethod) obj;

            if(signature.equals(method2.getSignature()) && parentClass.equals(method2.getParentClass()))
                return true;
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return name + " [" + type + "] ";
    }

    // getters / setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public OurClass getParentClass() {
        return parentClass;
    }

    public void setParentClass(OurClass parentClass) {
        this.parentClass = parentClass;
    }

    public MethodDeclaration getMd() {
        return md;
    }

    public void setMd(MethodDeclaration md) {
        this.md = md;
    }

    public OurClass getType() {
        return type;
    }

    public void setType(OurClass type) {
        this.type = type;
    }

    public void setVariables(List<OurVariable> variables) {
        this.variables = variables;
    }

    public List<OurVariable> getVariables() {
        return variables;
    }

    public String getTypeString() {
        return typeString;
    }

    public void setTypeString(String typeString) {
        this.typeString = typeString;
    }
}
