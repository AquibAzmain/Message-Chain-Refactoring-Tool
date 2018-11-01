package dataStructure;

import com.github.javaparser.ast.expr.MethodCallExpr;

import java.util.Comparator;

public class OurMessageChain implements Comparable<OurMessageChain> {
    private MethodCallExpr mce;
    private String statement;
    private OurMethod containerMethod;
    private int degree;
    private ChainCategory chainCategory;

    private String textModification;

    public OurMessageChain(MethodCallExpr mce, String statement, OurMethod containerMethod, int degree) {
        this.mce = mce;
        this.statement = statement;
        this.containerMethod = containerMethod;
        textModification = "";
        this.degree = degree;
    }

    @Override
    public String toString() {
        return "OurMessageChain{ " + mce +
                ", degree= " + degree +
                ", category= " + chainCategory +
                ", containerMethod= " + containerMethod +
                ", containerClass= " + containerMethod.getParentClass() +
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

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    public ChainCategory getChainCategory() {
        return chainCategory;
    }

    public void setChainCategory(ChainCategory chainCategory) {
        this.chainCategory = chainCategory;
    }

    @Override
    public int compareTo(OurMessageChain o) {
        return o.getDegree()-degree;
    }
}
