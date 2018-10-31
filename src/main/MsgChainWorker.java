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
    public static List<OurMessageChain> allMsgChains, finalMsgChains;

    public MsgChainWorker(){
        allClasses = new ArrayList<>();
        allMethods = new ArrayList<>();
        allMsgChains = new ArrayList<>();
        finalMsgChains = new ArrayList<>();
    }

    public void run(){
        ArrayList<String> fileList;
        MyFileReader myFileReader = new MyFileReader();
        fileList = myFileReader.manageFileReader();

        try {
            detectMessageChains(fileList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for(OurMessageChain msgChain: allMsgChains){
            OurMessageChain newMsgChain = refactorMsgChain(msgChain);
            if(newMsgChain != null)
                finalMsgChains.add(newMsgChain);
        }

        printOutput();
    }

    public void detectMessageChains(List<String> filePaths) throws FileNotFoundException {
        for(String filePath: filePaths){
            populateClasses(filePath);
        }

        for(OurClass currClass: allClasses){
            populateMethods(currClass);
            populateInstanceVariables(currClass);
        }

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

    private void printOutput() {
        try {
            PrintStream out = new PrintStream(new FileOutputStream(
                    "OutFile.txt"));

            out.println("There are " + finalMsgChains.size() + " Message Chains in this project");
            out.println("Following are the Message Chains and their respective refactoring suggestion:");

            int i=1;
            for(OurMessageChain msgChain: finalMsgChains){
                out.println("\n----------------------------------\n" + i++ + ". " + msgChain + "\n-----\n");
                out.print(msgChain.getTextModification());
            }

            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private OurMessageChain refactorMsgChain(OurMessageChain msgChain) {
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

                if(!isInnerMethod(nextClass, chainElements[i+1])){
                    break;
                }

                msgChain.addToModifiedText(modifyClassText(parentMethod, chainElement, msgChain.getChainEnder()));

                OurMethod newMethod = new OurMethod(msgChain.getChainEnder(), "public .1 " + msgChain.getChainEnder() + "()", nextClass);
                String lastClassModifier = modifyClassText(newMethod, getRestOfChain(chainElements, i), msgChain.getChainEnder());

                if(i==chainElements.length-2)
                    msgChain.addToModifiedText(lastClassModifier);

                currClass = nextClass;
                parentMethod = newMethod;
            }
            else{
                OurClass currMethodType = getTypeFromMethod(chainElement, currClass);
                String enderMethodType = "void";

                if(!(currMethodType == null))
                    enderMethodType = getTypeStringFromMethod(msgChain.getChainEnder(), currMethodType);
                msgChain.setTextModification(msgChain.getTextModification().replace(".1", enderMethodType));
            }
        }

        if(msgChain.getTextModification().isEmpty())
            return null;
        return msgChain;
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