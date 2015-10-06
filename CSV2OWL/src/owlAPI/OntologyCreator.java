package owlAPI;
import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
//import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
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
        try{
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        //PriorityCollection<OWLOntologyIRIMapper> iriMappers = manager.getIRIMappers();

        IRI ontologyIRI = IRI.create(ontologyIRIasString);

        IRI documentIRI = IRI.create(owlFile);
        SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);
		manager.addIRIMapper(mapper);

		OWLOntology ontology = manager.createOntology(ontologyIRI);

        IRI versionIRI = IRI.create(ontologyIRI + "/version1");

		OWLOntologyID newOntologyID = new OWLOntologyID(ontologyIRI, versionIRI);

		SetOntologyID setOntologyID = new SetOntologyID(ontology, newOntologyID);
		manager.applyChange(setOntologyID);

		save(ontologyIRI, ontology, owlFile);
	    } catch (OWLOntologyCreationException e) {
            System.out.println("Could not load ontology: " + e.getMessage());
        }
	}

	public void save(IRI ontologyIRI, OWLOntology ontology, File owlFile)
			throws OWLOntologyCreationException, OWLOntologyStorageException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLXMLDocumentFormat owlxmlFormat = new OWLXMLDocumentFormat();
		/* Save OWL to file*/
		manager.saveOntology(ontology, owlxmlFormat,
				IRI.create(owlFile.toURI()));
	}
}