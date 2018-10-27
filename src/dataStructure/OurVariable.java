package dataStructure;

public class OurVariable {
    private String name;
    private OurClass type;

    // constructor

    public OurVariable(String name, OurClass type) {
        this.name = name;
        this.type = type;
    }

    // methods

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof OurVariable){
            OurVariable var2 = (OurVariable) obj;

            if(name.equals(var2.getName()) && type.equals(var2.getType()))
                return true;
            return false;
        }

        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "dataStructure.OurVariable{" +
                "name='" + name + '\'' +
                ", container=" + type +
                '}';
    }

    // getters / setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OurClass getType() {
        return type;
    }

    public void setType(OurClass type) {
        this.type = type;
    }
}
