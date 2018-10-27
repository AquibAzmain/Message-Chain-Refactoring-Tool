public class A {
    public B b;

    protected void runA(String a){
        b.getC().getD().runD();
    }

    public void runA2(){
        getB().getC().getD();
    }

    public B getB(){
        return b;
    }
}
