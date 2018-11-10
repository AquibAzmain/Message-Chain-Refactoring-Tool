package main;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import dataStructure.*;
import fileReader.MyFileReader;
import visitors.InstanceFieldCollector;
import visitors.MethodCallCollector;
import visitors.MethodNameCollector;
import visitors.VariableCollector;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class MsgChainWorker {
    private List<OurClass> allClasses;
    private List<OurMethod> allMethods;
    private List<OurMessageChain> allMsgChains, finalMsgChains;
    private List<Integer> chainDegrees;
    private Set<Integer> chainDegreeSet;

    public MsgChainWorker(){
        allClasses = new ArrayList<>();
        allMethods = new ArrayList<>();
        allMsgChains = new ArrayList<>();
        finalMsgChains = new ArrayList<>();
        chainDegrees = new ArrayList<>();
        chainDegreeSet = new LinkedHashSet<>();
    }

    public List<OurMessageChain> run(String projectPath){
        MyFileReader myFileReader = new MyFileReader();
        List<String> fileList = myFileReader.manageFileReader(projectPath);

        System.out.println("<<<<----- Detection ---->>>>");
        try {
            detectMessageChains(fileList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("<<<<----- Refactor ---->>>>");
        for(OurMessageChain msgChain: allMsgChains){
            refactorMsgChain(msgChain);
        }

        if(finalMsgChains.size()>0)
            classifyMessageChains();

        return finalMsgChains;
    }

    private void classifyMessageChains() {
        chainDegrees.addAll(chainDegreeSet);
        Collections.sort(chainDegrees);

        int[] ranges = getCategoryRanges();
        int lowRange = ranges[0], highRange = ranges[1];


        for(OurMessageChain msgChain: finalMsgChains){
            addCategoryToMsgChain(lowRange, highRange, msgChain);
        }

    }

    private void addCategoryToMsgChain(int lowRange, int highRange, OurMessageChain msgChain) {
        if(msgChain.getDegree()<=lowRange)
            msgChain.setChainCategory(ChainCategory.LOW);
        else if(msgChain.getDegree()>=highRange)
            msgChain.setChainCategory(ChainCategory.HIGH);
        else
            msgChain.setChainCategory(ChainCategory.MEDIUM);
    }

    private int[] getCategoryRanges(){
        int lowLimit=chainDegrees.get(0), highLimit;
        if(chainDegrees.size()>3){
            int lowIndex = (int) Math.floor(chainDegrees.size() * 0.25);
            int highIndex = (int) Math.ceil(chainDegrees.size() * 0.75);

            lowLimit = chainDegrees.get(lowIndex);
            highLimit = chainDegrees.get(highIndex);
        } else if(chainDegrees.size()==3){
            highLimit=chainDegrees.get(2);
        } else if(chainDegrees.size()==2){
            highLimit=chainDegrees.get(1)+1;
        } else {
            lowLimit--;
            highLimit=chainDegrees.get(0)+1;
        }

        return new int[]{lowLimit, highLimit};
    }

    public void detectMessageChains(List<String> filePaths) throws FileNotFoundException {
        System.out.println("\t<<<<----- populating classes ---->>>>");
        for(String filePath: filePaths){
            populateClasses(filePath);
        }

        System.out.println("\t<<<<----- populating methods ---->>>>");
        for(OurClass currClass: allClasses){
            populateMethods(currClass);
            populateInstanceVariables(currClass);
        }

        System.out.println("\t<<<<----- populating message chains ---->>>>");
        for(OurMethod currMethod: allMethods){
            populateMethodVariables(currMethod);
            populateMessageChains(currMethod);
        }
    }

    private void populateClasses(String filePath) throws FileNotFoundException {
        String fileArr[] = filePath.split("\\\\");

        String javaSplitName[] = fileArr[fileArr.length-1].split("\\.");

        OurClass ourClass = new OurClass(javaSplitName[0], fileArr[fileArr.length-2]);
        ourClass.setFilePath(filePath);
        ourClass.setCompilationUnit(getCompilationUnitFromFile(filePath));

        allClasses.add(ourClass);
    }

    private void refactorMsgChain(OurMessageChain msgChain) {
        OurMethod parentMethod = msgChain.getContainerMethod();
        OurClass currClass = parentMethod.getParentClass();

        String[] chainElements = MyUtils.splitScope(msgChain.getStatement());

        for(int i=0; i<chainElements.length; i++){
            String chainElement = chainElements[i];
            OurClass nextClass;

            if(i<chainElements.length-1) {
                if(i==0){
                    nextClass = getTypeFromVariable(chainElement, parentMethod);
                }
                else{
                    nextClass = getTypeFromMethod(chainElement, currClass);
                }

                if(nextClass == null)
                    break;

                if(!isInnerMethod(nextClass, chainElements[i+1])){
                    break;
                }

                String newMethodName = getNewMethodName(i, chainElements, msgChain);
                msgChain.addToModifiedText(modifyClassText(parentMethod, chainElement, newMethodName));

                OurMethod newMethod = new OurMethod(newMethodName , "public .1 " + newMethodName + "()", nextClass);
                String lastClassModifier = modifyClassText(newMethod, getRestOfChain(chainElements, i), msgChain.getChainEnder());

                if(i==chainElements.length-2)
                    msgChain.addToModifiedText(lastClassModifier);

                currClass = nextClass;
                parentMethod = newMethod;
            }
            else{
                OurClass currMethodType = getTypeFromMethod(chainElement, currClass);
                String tailMethodType = "void";

                if(!(currMethodType == null))
                    tailMethodType = getTypeStringFromMethod(msgChain.getChainEnder(), currMethodType);
                msgChain.setTextModification(msgChain.getTextModification().replace(".1", tailMethodType));
            }
        }

        if(!msgChain.getTextModification().isEmpty()){
            finalMsgChains.add(msgChain);
            chainDegreeSet.add(msgChain.getDegree());
        }
    }

    private String getNewMethodName(int index, String[] chainElements, OurMessageChain msgChain) {
        String name = "";

        for(int i=index+1; i<chainElements.length; i++){
            String currMethodName = MyUtils.capitalize(getSkimmedMethodName(chainElements[i]).replace("get", ""));
            name += currMethodName;
        }

        name += MyUtils.capitalize(getSkimmedMethodName(msgChain.getChainEnder()).replace("get", ""));

        return MyUtils.decapitalize(name);
    }

    private boolean isInnerMethod(OurClass currClass, String chainElement) {
        for(OurMethod currMethod : currClass.getMethods()){
            if(currMethod.getName().equals(getSkimmedMethodName(chainElement))){
                return true;
            }
        }

        return false;
    }

    private String getRestOfChain(String[] chainElements, int index) {
        String restOfChain = "";
        for(int i=index+1; i<chainElements.length; i++){
            restOfChain += chainElements[i];
            if(i<chainElements.length-1) restOfChain +=".";
        }
        return restOfChain;
    }

    private String modifyClassText(OurMethod currMethod, String prefix, String suffix){
        String text = "";

        text += "public class " + currMethod.getParentClass().getName() + "{\n...\n";
        text += "\t" + currMethod.getSignature() + "{\n\t...\n";
        text += "\t\t" + "... " + prefix + "." + suffix + "();\n\t...\n\t}\n...\n}\n\n";

        return text;
    }

    private OurClass getTypeFromMethod(String chainElement, OurClass currClass) {
        for(OurMethod currMethod : currClass.getMethods()){
            if(currMethod.getName().equals(getSkimmedMethodName(chainElement))){
                return currMethod.getType();
            }
        }

        return null;
    }

    private String getTypeStringFromMethod(String chainElement, OurClass currClass) {
        for(OurMethod currMethod : currClass.getMethods()){
            if(currMethod.getName().equals(getSkimmedMethodName(chainElement))){
                return currMethod.getTypeString();
            }
        }

        return "void";
    }

    private String getSkimmedMethodName(String methodName) {
        int index = methodName.indexOf("(");
        if(index>0)
            return methodName.substring(0, index);
        return methodName;
    }

    private OurClass getTypeFromVariable(String chainElement, OurMethod parentMethod) {
        for(OurVariable currVar : parentMethod.getVariables()){
            if(chainElement.equals(currVar.getName()))
                return currVar.getType();
        }

        for(OurVariable currVar: parentMethod.getParentClass().getFields()){
            if(chainElement.equals(currVar.getName()))
                return currVar.getType();
        }
        return null;
    }

    private void populateMessageChains(OurMethod currMethod) {
        List<OurMessageChain> msgChains = new ArrayList<>();

        MethodCallCollector methodCallCollector = new MethodCallCollector(currMethod);
        methodCallCollector.visit(currMethod.getMd(), msgChains);

        allMsgChains.addAll(msgChains);
    }

    private void populateMethodVariables(OurMethod currMethod) {
        List<OurVariable> variables = new ArrayList<>();

        VariableCollector variableCollector = new VariableCollector(currMethod, allClasses);
        variableCollector.visit(currMethod.getMd(), variables);

        currMethod.setVariables(variables);
    }

    private void populateInstanceVariables(OurClass currClass) {
        List<OurVariable> variables = new ArrayList<>();

        InstanceFieldCollector instanceFieldCollector = new InstanceFieldCollector(allClasses);
        instanceFieldCollector.visit(currClass.getCompilationUnit(), variables);

        currClass.setFields(variables);
    }

    public void populateMethods(OurClass currClass){
        List<OurMethod> methods = new ArrayList<>();

        MethodNameCollector methodNameCollector = new MethodNameCollector(currClass, allClasses);
        methodNameCollector.visit(currClass.getCompilationUnit(), methods);

        currClass.setMethods(methods);
        allMethods.addAll(methods);
    }

    private CompilationUnit getCompilationUnitFromFile(String filePath) throws FileNotFoundException {
        return JavaParser.parse(new File(filePath));
    }
}