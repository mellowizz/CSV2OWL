package owlAPI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import owlAPI.OWLmap.owlRuleSet;

public class OntologyWriter {
	/*
	 * 
	 * Writes properties and classes to ontology
	 */
    public void writeMap(OWLmap rules, IRI documentIRI, IRI ontologyIRI) throws OWLOntologyCreationException, OWLOntologyStorageException{
        
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager
				.loadOntologyFromOntologyDocument(documentIRI);
		OWLDataFactory factory = manager.getOWLDataFactory();
		SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);
		manager.addIRIMapper(mapper);
		PrefixManager pm = new DefaultPrefixManager(ontologyIRI.toString());
		
		/* go over map */
		for (OntologyClass c: OWLmap.eunisClasses){
            OWLClass parent = factory.getOWLClass(IRI.create(ontologyIRI + "#"
                    + c.getParent()));
			OWLClass cls = factory.getOWLClass(IRI.create(ontologyIRI + "#"
					+ c.getName()));
			OWLClass thing = factory.getOWLThing();
			OWLAxiom classAx = factory.getOWLSubClassOfAxiom(cls, parent);
			OWLAxiom parameterAx = factory.getOWLSubClassOfAxiom(parent, thing);
			manager.applyChange(new AddAxiom(ontology, classAx));
			manager.applyChange(new AddAxiom(ontology, parameterAx));
			if (c.getDescription() != null) {
                OWLAnnotation commentAnno = factory.getOWLAnnotation(factory.getRDFSComment(),
                         factory.getOWLLiteral(c.getDescription(), "en"));
                OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(cls.getIRI(),
                         commentAnno);
                manager.applyChange(new AddAxiom(ontology, ax));
                OWLAnnotation commentDE = factory.getOWLAnnotation(factory.getRDFSComment(),
                         factory.getOWLLiteral(c.getDescriptionDE(), "de"));
                OWLAxiom axDE = factory.getOWLAnnotationAssertionAxiom(cls.getIRI(),
                         commentDE);
                manager.applyChange(new AddAxiom(ontology, axDE));
			}
		}
		OWLClassExpression firstRuleSet= null;
        OWLClass owlCls = null;
        OWLClass owlParent = null;
        OWLObjectUnionOf totalunion = null;
		Iterator<Entry<String, ArrayList<owlRuleSet>>> it = rules.map.entrySet().iterator();
        Set<OWLClassExpression> unionSet = new HashSet<OWLClassExpression>();
        manager.saveOntology(ontology);
    }
	public void writeAll(LinkedHashSet<OntologyClass> classes, List<AddAxiom> rules, 
	        IRI documentIRI, IRI ontologyIRI)
	                throws OWLOntologyCreationException, 
	                OWLOntologyStorageException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager
				.loadOntologyFromOntologyDocument(documentIRI);
		OWLDataFactory factory = manager.getOWLDataFactory();
		SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);
		manager.addIRIMapper(mapper);
		//PrefixManager pm = new DefaultPrefixManager(ontologyIRI.toString()); 

		/* TODO: fixme */
		for (OntologyClass EUClass : classes) {
		    OWLClass parameter = factory.getOWLClass(IRI.create(ontologyIRI
				+ "#" + EUClass.getParent()));
			OWLClass cls = factory.getOWLClass(IRI.create(ontologyIRI + "#"
					+ EUClass.getName()));
			OWLClass thing = factory.getOWLThing();
			OWLAxiom classAx = factory.getOWLSubClassOfAxiom(cls, parameter);
			OWLAxiom parameterAx = factory.getOWLSubClassOfAxiom(parameter, thing);
			manager.applyChange(new AddAxiom(ontology, classAx));
			manager.applyChange(new AddAxiom(ontology, parameterAx));
			if (EUClass.getDescription() != null) {
                OWLAnnotation commentAnno = factory.getOWLAnnotation(factory.getRDFSComment(),
                         factory.getOWLLiteral(EUClass.getDescription(), "en"));
                OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(cls.getIRI(),
                         commentAnno);
                manager.applyChange(new AddAxiom(ontology, ax));
                OWLAnnotation commentDE = factory.getOWLAnnotation(factory.getRDFSComment(),
                         factory.getOWLLiteral(EUClass.getDescriptionDE(), "de"));
                OWLAxiom axDE = factory.getOWLAnnotationAssertionAxiom(cls.getIRI(),
                         commentDE);
                manager.applyChange(new AddAxiom(ontology, axDE));
			}
		}
		System.out.println("object properties: " + rules.size());
		manager.saveOntology(ontology);
		
	}
	public void writeIndividuals(LinkedHashSet<Individual> individuals,
			IRI documentIRI) throws OWLOntologyCreationException,
			OWLOntologyStorageException {
		/*
		 * Writes Individuals (and corresponding DataProperties) to an existing
		 * ontology
		 */
		System.out.println("# of individuals: " + individuals.size());
		OWLDataFactory factory = manager.getOWLDataFactory();
		IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI();
		SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);

		manager.addIRIMapper(mapper);

		PrefixManager pm = new DefaultPrefixManager(ontologyIRI.toString());

		for (Individual ind : individuals) {
			Integer index = 0;

			OWLNamedIndividual obj = factory.getOWLNamedIndividual(
					"#" + ind.getFID(), pm);
			for (Entry<String, Number> entry : ind.getValues().entrySet()){
				OWLDataProperty dataProp = factory.getOWLDataProperty("#"
						+ entry.getKey(), pm);

				OWLDatatype integerDatatype = factory
						.getOWLDatatype(OWL2Datatype.XSD_DOUBLE.getIRI());

				OWLLiteral literal = factory.getOWLLiteral(entry.getValue().toString(),
						integerDatatype);

				OWLDataPropertyAssertionAxiom dataPropertyAssertion = factory
						.getOWLDataPropertyAssertionAxiom(dataProp, obj,
								literal);

				manager.addAxiom(ontology, dataPropertyAssertion);

				index = index + 1;
			}
		}
		manager.saveOntology(ontology);
	}
}
