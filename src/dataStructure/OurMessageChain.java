package dataStructure;

import com.github.javaparser.ast.expr.MethodCallExpr;

public class OurMessageChain {
    private MethodCallExpr mce;
    private String statement;
    private OurMethod containerMethod;

    private String textModification;

    public OurMessageChain(MethodCallExpr mce, String statement, OurMethod containerMethod) {
        this.mce = mce;
        this.statement = statement;
        this.containerMethod = containerMethod;
        textModification = "";
    }

    @Override
    public String toString() {
        return "OurMessageChain{" +
                ", statement='" + statement + '\'' +
                ", containerMethod=" + containerMethod +
                ", end='" + mce.getName() + '\'' +
                '}';
    }

    // getter | setters

    public MethodCallExpr getMce() {
        return mce;
    }

    public void setMce(MethodCallExpr mce) {
        this.mce = mce;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public OurMethod getContainerMethod() {
        return containerMethod;
    }

    public void setContainerMethod(OurMethod containerMethod) {
        this.containerMethod = containerMethod;
    }

    public void addToModifiedText(String text){
        textModification += text;
    }

    public String getTextModification() {
        return textModification;
    }

    public void setTextModification(String textModification) {
        this.textModification = textModification;
    }

    public String getChainEnder() {
        return mce.getName().asString();
    }
}
