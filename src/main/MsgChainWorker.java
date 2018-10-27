package main;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import dataStructure.OurClass;
import dataStructure.OurMessageChain;
import dataStructure.OurMethod;
import dataStructure.OurVariable;
import fileReader.MyFileReader;
import visitors.InstanceFieldCollector;
import visitors.MethodCallCollector;
import visitors.MethodNameCollector;
import visitors.VariableCollector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class MsgChainWorker {
    public static List<OurClass> allClasses;
    public static List<OurMethod> allMethods;
    public static List<OurMessageChain> allMsgChains;

    public MsgChainWorker(){
        allClasses = new ArrayList<>();
        allMethods = new ArrayList<>();
        allMsgChains = new ArrayList<>();
    }

    public void init(){
        ArrayList<String> fileList;
        MyFileReader myFileReader = new MyFileReader();
        fileList = myFileReader.manageFileReader();

        try {
            populateClasses(fileList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void populateClasses(List<String> filePaths) throws FileNotFoundException {
        for(String filePath: filePaths){
            String fileArr[] = filePath.split("\\\\");

            String javaSplitName[] = fileArr[fileArr.length-1].split("\\.");

            OurClass ourClass = new OurClass(javaSplitName[0], fileArr[fileArr.length-2]);
            ourClass.setFilePath(filePath);
            ourClass.setCompilationUnit(getCompilationUnitFromFile(filePath));

            allClasses.add(ourClass);
        }

        for(OurClass currClass: allClasses){
            populateMethods(currClass);
            populateInstanceVariables(currClass);
        }

        for(OurMethod currMethod: allMethods){
            populateMethodVariables(currMethod);
            populateMessageChains(currMethod);
        }

        for(OurClass clazz: allClasses){
            System.out.println(clazz);
            System.out.println();
        }

//        for(OurMessageChain msgChain: allMsgChains){
//            System.out.println(msgChain);
//        }

        for(OurMessageChain msgChain: allMsgChains){
            refactorMsgChain(msgChain);
        }

        printOutput();
    }

    private void printOutput() {
        try {
            PrintStream out = new PrintStream(new FileOutputStream(
                    "OutFile.txt"));

            out.println("Following are the Message Chains in the project and their respective refactoring suggestion:");
            for(OurMessageChain msgChain: allMsgChains){
                out.println("\n----------------------------------\n" + msgChain + "\n-----\n");
                out.print(msgChain.getTextModification());
            }

            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void refactorMsgChain(OurMessageChain msgChain) {
        OurMethod parentMethod = msgChain.getContainerMethod();
        OurClass currClass = parentMethod.getParentClass();

        //TODO: more correct splitting
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

                msgChain.addToModifiedText(modifyClassText(parentMethod, chainElement, msgChain.getChainEnder()));

                OurMethod newMethod = new OurMethod(msgChain.getChainEnder(), "public .1 " + msgChain.getChainEnder(), nextClass);
                String lastClassModifier = modifyClassText(newMethod, getRestOfChain(chainElements, i), msgChain.getChainEnder());

                if(i==chainElements.length-2)
                    msgChain.addToModifiedText(lastClassModifier);

                currClass = nextClass;
                parentMethod = newMethod;
            }
            else{
                String enderMethodType = getTypeStringFromMethod(msgChain.getChainEnder(), getTypeFromMethod(chainElement, currClass));
                msgChain.setTextModification(msgChain.getTextModification().replace(".1", enderMethodType));
            }
        }
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

        return null;
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