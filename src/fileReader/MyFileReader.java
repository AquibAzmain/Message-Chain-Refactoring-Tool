package fileReader;

import main.MyUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyFileReader {
	private List<String> listFilesForFolder(File folder) {
		List<String> fileList = new ArrayList<>();

		try {
			for (File fileEntry : folder.listFiles()) {
				if (fileEntry.isDirectory()) {
					fileList.addAll(listFilesForFolder(fileEntry));
				} else {
					if(fileEntry.getAbsolutePath().contains(".java")){
						fileList.add(fileEntry.getAbsolutePath());
					}
				}
			}
		} catch (NullPointerException e) {
			MyUtils.exitWithInvalidFilePath();
		}

		return fileList;
	}
	
	public List<String> manageFileReader(String projectPath) {
		File folder = new File(projectPath);
		return listFilesForFolder(folder);
	}

}

/*
D:\\Programming\\Eclipse\\workspace\\Message Chain Refactoring Tool
D:\\Studies\\Semester 8\\Metrics\\Object-Oriented-Metrics-Tool\\Tool
D:\\Studies\\Semester 8\\Metrics\\Object-Oriented-Metrics-Tool\\jaimlib
 */
