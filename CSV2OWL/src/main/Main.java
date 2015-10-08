package main;

import java.io.File;
import java.io.IOException;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import csvToOWLRules.CreateClassesFromCSV;

public class Main {

	public static void main(String[] args) throws IOException {
		File file = new File(".");
		String gDocLocation = file.getCanonicalPath();
		String workingDirectory = null;
		String OS = (System.getProperty("os.name")).toUpperCase();
		//to determine what the workingDirectory is.
		//if it is some version of Windows
		String pythonLoc = null;
		if (OS.contains("WIN"))
		{
			workingDirectory = System.getenv("AppData");
			pythonLoc = 
					"C:/Python27_64/WinPython-64bit-2.7.9.3/python-2.7.9.amd64/python.exe";
		
		}
		//Otherwise, we assume Linux or Mac
		else
		{
			workingDirectory = System.getProperty("user.home");
			pythonLoc = "python2";
		}
		System.out.println("Executing: " + gDocLocation);
		
        Process process = new ProcessBuilder(pythonLoc,
                    gDocLocation).start();
		String myFileName = args[0];
		String myOutFile = args[1];
		File owlFile = new File(myOutFile);
		try {
			CreateClassesFromCSV.createClassesfromCSV(myFileName, 2, owlFile);
		} catch (OWLOntologyStorageException e2) {
			throw new RuntimeException(e2.getMessage(), e2);
		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}