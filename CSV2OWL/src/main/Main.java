package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import csvToOWLRules.CreateClassesFromCSV;

public class Main {

	public static void main(String[] args) throws IOException {
	    File file = new File(".");  
        //File[] files = file.listFiles();  
        String gDocLocation = file.getCanonicalPath() + "\\src\\get_google_doc\\get_google_doc.py";
        System.out.println("Executing: " + gDocLocation);
        Process process =   new ProcessBuilder("cmd", "python", gDocLocation).start();
        //processBuilder.redirectErrorStream(true);
        //processBuilder.start();
        //processBuilder.command("python");
        //Process pythonProcess = processBuilder.start();
        /*
		String myFileName = args[0];
		String myOutFile = args[1];
		File owlFile = new File(myOutFile); 
		try {
			CreateClassesFromCSV.createClassesfromCSV(myFileName, 2, owlFile); 
		}catch (OWLOntologyStorageException e2) {
			throw new RuntimeException(e2.getMessage(), e2);
		}
		catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
			System.out.println("created owlFile");
		}*/
	}
}