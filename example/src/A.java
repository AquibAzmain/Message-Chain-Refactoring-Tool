public class A {
    public B b;

    protected void runA(String a){
        b.getC(a).getD().runD();
    }

    public void runA2(){
        getB().getC("").getD();
    }

    public B getB(){
        return b;
    }

    public void what(){
        b.toString().substring(1).split("d");
    }
}
