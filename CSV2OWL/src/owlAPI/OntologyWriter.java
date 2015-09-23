package owlAPI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
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

	public void writeAll(LinkedHashSet<OntologyClass> classes, OWLmap rules, 
	        IRI documentIRI, IRI ontologyIRI)
	                throws OWLOntologyCreationException, 
	                OWLOntologyStorageException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager
				.loadOntologyFromOntologyDocument(documentIRI);
		OWLDataFactory factory = manager.getOWLDataFactory();
		SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);
		manager.addIRIMapper(mapper);
		PrefixManager pm = new DefaultPrefixManager(ontologyIRI.toString()); 

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
		
		/* write rules */
		OWLClassExpression firstRuleSet= null;
		OWLClass owlCls = null;
		OWLObjectUnionOf totalunion = null;
		Iterator<Entry<String, ArrayList<owlRuleSet>>> it = rules.map.entrySet().iterator();
		Set<OWLClassExpression> unionSet = new HashSet<OWLClassExpression>();
		while (it.hasNext()){
			Map.Entry<String, ArrayList<owlRuleSet>> pair = it.next();
			String currCls = (String) pair.getKey();
			owlCls = factory.getOWLClass(IRI.create("#" + currCls ));
			ArrayList<owlRuleSet> currRuleset = (ArrayList<owlRuleSet>) pair.getValue();
			for (int i=0; i< currRuleset.size(); i++){
				firstRuleSet = factory.getOWLObjectIntersectionOf(currRuleset.get(i).getRuleList(currCls));
				unionSet.add(firstRuleSet);
			}
			totalunion = factory.getOWLObjectUnionOf(unionSet);
			unionSet.clear();
			manager.addAxiom(ontology, factory.getOWLEquivalentClassesAxiom(owlCls, totalunion));
		}
		manager.saveOntology(ontology);
	}

}