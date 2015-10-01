package main;

import java.io.File;
import java.io.IOException;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import csvToOWLRules.CreateClassesFromCSV;

public class Main {

	public static void main(String[] args) {
		String myFileName = args[0];
		String myOutFile = args[1];
		//LinkedHashSet<OntologyClass> classes = null;
		File owlFile = new File(myOutFile); //"C:/Users/Moran/ontologies/" +);
		try {
		    /*
		     * 
            final FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(
                "csv", "CSV");
            if (!extensionFilter.accept(csvFile)) {
                System.err.println("error: file doesn't end in .csv");
            }*/
			// create ontology
		    
			/* get classes and individuals */
            /* get classes and individuals */
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
		}
	}
}