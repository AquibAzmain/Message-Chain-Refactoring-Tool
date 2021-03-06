package main;

import dataStructure.OurMessageChain;
import dataStructure.OurProject;

import java.io.*;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MsgChainTool {
    private BufferedReader reader;

    public MsgChainTool(){
        reader = new BufferedReader(new InputStreamReader(System.in));
    }


    public void run(){
        String choice = getChoice();

        if(choice.equals("M")){
            runMultipleVersions();
        }
        else{
            runSingleProject();
        }
    }

    private void runMultipleVersions() {
        List<String> projectPaths = getMultipleProjectPaths();
        List<OurProject> ourProjects = new ArrayList<>();

        for(String path: projectPaths){
            String[] splitPath = splitProjectPath(path);

            System.out.println("Scanning completed for project " + splitPath[0]);

            MsgChainWorker msgChainWorker = new MsgChainWorker();
            List<OurMessageChain> msgChains = msgChainWorker.run(splitPath[1]);

            OurProject newProject = new OurProject(splitPath[0], msgChains);
            ourProjects.add(newProject);

            System.out.println("Scanning completed for project " + newProject.getName() + "\n");
        }

        int num = 1;
        for(OurProject ourProject: ourProjects){
            printOutput(ourProject.getMsgChains(), ourProject.getName(), "OutFile" + num);
            num++;
        }

        try {
            outputProjectStats(ourProjects);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] splitProjectPath(String path) {
        return path.split(" -:- ");
    }

    public void outputProjectStats(List<OurProject> ourProjects) throws IOException{

        PrintWriter csvFileWriter = new PrintWriter("project_stats.csv");
        csvFileWriter.println("Project Name,Total Message Chain,Maximum Chain Degree,Average Chain Degree");

        for(OurProject ourProject: ourProjects){
            csvFileWriter.println(ourProject.getName() + "," +
                    ourProject.getChainNumber() + "," +
                    ourProject.getMaxChainDegree() + "," +
                    ourProject.getAverageDegree());
        }

        csvFileWriter.close();
    }

    private List<String> getMultipleProjectPaths() {
        List<String> paths = new ArrayList<>();

        System.out.println("Enter projects. Format: <Project name> -:- <Project path>");
        System.out.println("<enter 'q' to stop>");

        String path = "";

        while(true){
            try {
                path = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(path.equals("q"))
                break;

            paths.add(path);
        }

        return paths;
    }

    private boolean isValidPath(String path) {
        try {
            Paths.get(path);
        } catch (InvalidPathException | NullPointerException ex) {
            return false;
        }
        return true;
    }

    private void runSingleProject() {
        String projectPath = getProjectPath();

        String filePath = splitProjectPath(projectPath)[1];
        String projectName = splitProjectPath(projectPath)[0];

        if(filePath == null || filePath.trim().isEmpty() || !isValidPath(filePath))
            MyUtils.exitWithInvalidFilePath();

        MsgChainWorker msgChainWorker = new MsgChainWorker();
        List<OurMessageChain> msgChains = msgChainWorker.run(filePath);

        printOutput(msgChains, projectName, "OutFile");
    }

    private String getProjectPath() {
        System.out.println("Enter project. Format: <Project name> -:- <Project path>");

        String path = null;
        try {
            path = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return path;
    }

    private String getChoice() {
        String choice = "M";
        System.out.println("Run on single project (S) or multiple versions (M)?");

        try {
            choice = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(choice.equals("M") || choice.equals("S"))
            return choice;
        return "M";
    }


    private void printOutput(List<OurMessageChain> messageChains, String projectName, final String outputFileName) {
        try {
            PrintStream out = new PrintStream(new FileOutputStream(
                    outputFileName + ".txt"));

            out.println("There are " + messageChains.size() + " Message Chains in project " + projectName);
            out.println("Following are the Message Chains and their respective refactoring suggestion:");

            int i=1;
            Collections.sort(messageChains);
            for(OurMessageChain msgChain: messageChains){
                out.println("\n----------------------------------\n" + i++ + ". " + msgChain + "\n-----\n");
                out.print(msgChain.getTextModification());
            }

            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
