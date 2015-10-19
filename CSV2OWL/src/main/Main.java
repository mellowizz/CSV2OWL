package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.opencsv.CSVReader;

import owlAPI.OntologyCreator;

public class Main {
    private static LinkedHashMap<String, Integer> getColIndexes(
            String fileName) {
        CSVReader reader = null;
        List<String> headerCols = null;
        LinkedHashMap<String, Integer> myHash = new LinkedHashMap<String, Integer>();
        try {
            reader = new CSVReader(new FileReader(fileName));
            headerCols = Arrays.asList(reader.readNext());
            for (int i = 0; i < headerCols.size(); i++) {
                String column = headerCols.get(i);
                if (column.startsWith("EUNIS_") && !column.startsWith("EUNIS_N")
                        || column.startsWith("NATFLO")
                        || column.startsWith("EAGLE")) { 
                    myHash.put(column, i);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myHash;
    }

    public static void main(String[] args) throws IOException {
        File file = new File(".");
        String gDocLocation = file.getCanonicalPath()
                + "/src/get_google_doc/get_google_doc.py";
        String workingDirectory = null;
        String OS = (System.getProperty("os.name")).toUpperCase();
        String pythonLoc = null;
        if (OS.contains("WIN")) {
            workingDirectory = System.getenv("AppData");
            pythonLoc = "C:/Python27_64/WinPython-64bit-2.7.9.3/python-2.7.9.amd64/python.exe";

        }
        // Otherwise, we assume Linux or Mac
        else {
            workingDirectory = System.getProperty("user.home");
            pythonLoc = "python2";
        }
        System.out.println("Executing: " + gDocLocation);

        Process process = new ProcessBuilder(pythonLoc, gDocLocation).start();
        String myFileName = args[0];
        String myOutFile = args[1];
        File owlFile = new File(myOutFile);
        CSVReader reader = null;
        try {
            /* Read from CSV */
            LinkedHashMap<String, Integer> nameIndex = null;
            String iriString = "http://www.user.tu-berlin.de/niklasmoran/EUNIS/"
                    + owlFile.getName().trim();
            /* open file */
            reader = new CSVReader(new FileReader(myFileName));
            nameIndex = getColIndexes(myFileName);
            /* create ontology */
            OntologyCreator ontCreate = new OntologyCreator();
            ontCreate.createOntology(iriString, "version_1_0", owlFile);
            ontCreate.createOntologyObject(nameIndex, myFileName);
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (OWLOntologyStorageException e2) {
            throw new RuntimeException(e2.getMessage(), e2);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}