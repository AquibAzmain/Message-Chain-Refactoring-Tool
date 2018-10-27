package fileReader;

import java.io.File;
import java.util.ArrayList;

public class MyFileReader {
	private static ArrayList<String> fileList;
	
	private void listFilesForFolder(File folder) {
	    for (File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry);
	        } else {
	        	if(fileEntry.getAbsolutePath().contains(".java")){
	        		fileList.add(fileEntry.getAbsolutePath());
	        	}
	        }
	    }
	    
//	    for(String filename: fileList) {
//	    	System.out.println(filename);
//	    }
	}
	
	public ArrayList<String> manageFileReader() {
		if(fileList==null) {
			String projectName = "";
//			System.out.println("Enter folder name: ");
//			Scanner input = new Scanner(System.in);
			//projectName = input.nextLine();
			projectName = "Tool";
			fileList = new ArrayList<>();
			String workingDir = System.getProperty("user.dir");
			File file = new File(workingDir);
			String parentDir = file.getParent();
			File folder = new File(parentDir+ "/" + projectName);
			folder = new File(("D:\\Programming\\Eclipse\\workspace\\Message Chain Refactoring Tool\\example"));
			listFilesForFolder(folder);
//			input.close();
		}
		
		return fileList;
	}

}
