package dataStructure;

import java.util.List;

public class OurProject {
    private String name;
    private List<OurMessageChain> msgChains;
    private int chainNumber;
    private int maxChainDegree;
    private int averageDegree;

    public OurProject(String name, List<OurMessageChain> msgChains) {
        this.name = name;
        this.msgChains = msgChains;
        chainNumber = msgChains.size();
        maxChainDegree = 0;
        averageDegree = 0;
        if(chainNumber>0)
            calculateStats();
    }

    @Override
    public String toString() {
        return "OurProject{" +
                "name='" + name + '\'' +
                ", chainNumber=" + chainNumber +
                ", maxChainDegree=" + maxChainDegree +
                ", averageDegree=" + averageDegree +
                '}';
    }

    private void calculateStats() {
        int totalDegree = 0;

        for(OurMessageChain msgChain: msgChains){
            int degree = msgChain.getDegree();

            if(degree>maxChainDegree)
                maxChainDegree=degree;

            totalDegree+=degree;
        }

        averageDegree = (int) totalDegree/chainNumber;
    }


    // getter | setter


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<OurMessageChain> getMsgChains() {
        return msgChains;
    }

    public void setMsgChains(List<OurMessageChain> msgChains) {
        this.msgChains = msgChains;
    }

    public int getChainNumber() {
        return chainNumber;
    }

    public void setChainNumber(int chainNumber) {
        this.chainNumber = chainNumber;
    }

    public int getMaxChainDegree() {
        return maxChainDegree;
    }

    public void setMaxChainDegree(int maxChainDegree) {
        this.maxChainDegree = maxChainDegree;
    }

    public int getAverageDegree() {
        return averageDegree;
    }

    public void setAverageDegree(int averageDegree) {
        this.averageDegree = averageDegree;
    }
}
