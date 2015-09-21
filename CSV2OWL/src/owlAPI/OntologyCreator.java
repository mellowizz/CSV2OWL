package owlAPI;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

public class OntologyCreator {
	/*
	 * 
	 * Creates ontology with onotology IRI and saves it to input OWL File
	 */

	public void createOntology(String ontologyIRIasString, String version,
			File owlFile) throws OWLOntologyCreationException,
			OWLOntologyStorageException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		IRI ontologyIRI = IRI.create(ontologyIRIasString);

		IRI documentIRI = IRI.create(owlFile);

		SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);
		manager.addIRIMapper(mapper);

		OWLOntology ontology = manager.createOntology(ontologyIRI);

		IRI versionIRI = IRI.create(ontologyIRI + "/version1");

		OWLOntologyID newOntologyID = new OWLOntologyID(ontologyIRI, versionIRI);

		SetOntologyID setOntologyID = new SetOntologyID(ontology, newOntologyID);
		manager.applyChange(setOntologyID);
		System.out.println("Ontology created: " + ontology);

		save(ontologyIRI, ontology, owlFile);
	}

	public void save(IRI ontologyIRI, OWLOntology ontology, File owlFile)
			throws OWLOntologyCreationException, OWLOntologyStorageException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLXMLOntologyFormat owlxmlFormat = new OWLXMLOntologyFormat();
		/* Save OWL to file*/
		manager.saveOntology(ontology, owlxmlFormat,
				IRI.create(owlFile.toURI()));
	}
}