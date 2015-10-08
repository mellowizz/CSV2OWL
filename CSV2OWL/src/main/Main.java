package main;

import java.io.File;
import java.io.IOException;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import csvToOWLRules.CreateClassesFromCSV;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        File file = new File(".");
        // File[] files = file.listFiles();
        String gDocLocation = file.getCanonicalPath()
                + "\\src\\get_google_doc\\get_google_doc.py";
        System.out.println("Executing: " + gDocLocation);
        String pyPath = "C:\\Python27_64\\WinPython-64bit-2.7.9.3\\python-2.7.9.amd64\\";
        Process process = new ProcessBuilder(
                pyPath + "python.exe",
                gDocLocation).start();
        int exitCode = process.waitFor();
        System.out.println("Exit code:" + exitCode);
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
        } finally {
            System.out.println("created owlFile");
        }
    }
}