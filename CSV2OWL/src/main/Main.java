package main;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import csvToOWLRules.CSVToOWLRules;
import csvToOWLRules.CreateClassesFromCSV;
import owlAPI.OWLmap;
import owlAPI.OntologyClass;
import owlAPI.OntologyCreator;
import owlAPI.OntologyWriter;

public class Main {

	public static void main(String[] args) {
		String myFileName = args[0];
		String myOutFile = args[1];
		LinkedHashSet<OntologyClass> classes = null;
		File owlFile = new File(myOutFile); //"C:/Users/Moran/ontologies/" +);
		try {
			// create ontology
			OntologyCreator ontCreate = new OntologyCreator();
			String ontologyIRI = "http://www.user.tu-berlin.de/niklasmoran/EUNIS/"
					+ owlFile.getName().trim();
			ontCreate.createOntology(ontologyIRI, "version_1_0", owlFile);
			/* get classes and individuals */
			classes = CreateClassesFromCSV.createClassesfromCSV(myFileName, 2);
			//individuals = createIndividualsFromDB(tableName);
			OntologyWriter ontWrite = new OntologyWriter(); // IRI.create(owlFile.toURI()));
			File csvFile = new File(myFileName);
			int numRules = 0;
			/* TODO: cleanup! */
			OWLmap rulesMap = null;
			if (csvFile.isFile()) {
				CSVToOWLRules therules = new CSVToOWLRules(myFileName,
						IRI.create(owlFile.toURI()));
				rulesMap = therules.CSVRules();
			/* if another parameter? */
			ontWrite.writeAll(classes, rulesMap,
					IRI.create(owlFile.toURI()), IRI.create(ontologyIRI));
			}
		}catch (OWLOntologyStorageException e2) {
			throw new RuntimeException(e2.getMessage(), e2);
		}
		catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
			System.out.println("created owlFile");
		}
	}
}