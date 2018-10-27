public class A {
    public B b;

    public void runA(){
        b.getC().getD().runD();
    }

    public void runA2(){
        getB().getC().getD();
    }

    public B getB(){
        return b;
    }
}
