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
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import owlAPI.OWLmap.owlRuleSet;

public class OntologyWriter {
	/*
	 * 
	 * Writes properties and classes to ontology
	 */

	public void writeAll(LinkedHashSet<OntologyClass> classes, LinkedHashSet<Individual> individuals, OWLmap rules, 
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
		System.out.println("# of individuals: " + individuals.size());
		
        for (Individual ind : individuals) {
            Integer index = 0;

            OWLNamedIndividual obj = factory.getOWLNamedIndividual(
                    "#" + ind.getName(), pm);

            for (Entry<String, Number> entry : ind.getValues().entrySet()){
                OWLObjectProperty dataProp = factory.getOWLObjectProperty("#"
                        + entry.getKey(), pm);

                OWLDatatype doubleDatatype = factory
                        .getOWLDatatype(OWL2Datatype.XSD_DOUBLE.getIRI());

                OWLNamedIndividual name = factory.getOWLNamedIndividual(entry.getValue().toString(), pm);
                OWLLiteral literal = factory.getOWLLiteral(entry.getValue().toString(),
                        doubleDatatype);

                OWLObjectPropertyAssertionAxiom dataPropertyAssertion = factory
                        .getOWLObjectPropertyAssertionAxiom(dataProp, obj,
                                name);
                manager.applyChange(new AddAxiom(ontology, dataPropertyAssertion));
                index = index + 1;
            }
            index = 0;
            for (Entry<String, String> entry : ind.getStringValues().entrySet()){
                OWLObjectProperty dataProp = factory.getOWLObjectProperty("#"
                        + entry.getKey(), pm);
                 
                OWLClass myClass = factory.getOWLClass(entry.getValue().toString(), pm);
                OWLDatatype stringDatatype = factory
                        .getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI());
                //System.out.println("about to write: " + value);
                String value = entry.getValue();
                if (value == null){
                    value = "";
                }
                OWLNamedIndividual name = factory.getOWLNamedIndividual(entry.getValue().toString(), pm);

                OWLLiteral literal = factory.getOWLLiteral(value,
                        stringDatatype);

                OWLObjectPropertyAssertionAxiom dataPropertyAssertion = factory
                        .getOWLObjectPropertyAssertionAxiom(dataProp, obj,
                                name);
                manager.applyChange(new AddAxiom(ontology, dataPropertyAssertion));
                index = index + 1;
            }
        }
        OWLDatatype rdfsLiteral = factory.getTopDatatype();
        OWLLiteral literal = factory.getOWLLiteral("tree",
                rdfsLiteral);
        
        OWLObjectProperty hasVeg = factory.getOWLObjectProperty("#has_EAGLE_vegetationType_1", pm);
        OWLClass eunis = factory.getOWLClass(IRI.create("#E1"));
        OWLClass vegType = factory.getOWLClass(IRI.create("#tree"));
        factory.getOWLDataOneOf(literal);
        OWLClassExpression hasVegRest = factory.getOWLObjectSomeValuesFrom(hasVeg, vegType);
        OWLEquivalentClassesAxiom ax1 = factory.getOWLEquivalentClassesAxiom(eunis, hasVegRest);
        AddAxiom addAx = new AddAxiom(ontology, ax1);
        manager.applyChange(addAx);
        //factory.getOWLObj
        
        
		/* write rules */
        /*
		OWLClassExpression firstRuleSet= null;
		OWLClass owlCls = null;
		OWLObjectUnionOf totalunion = null;
		Iterator<Entry<String, ArrayList<owlRuleSet>>> it = rules.map.entrySet().iterator();
		Set<OWLClassExpression> unionSet = new HashSet<OWLClassExpression>();
		while (it.hasNext()){
			Map.Entry<String, ArrayList<owlRuleSet>> pair = it.next();
            String currCls = (String) pair.getKey();
			owlCls = factory.getOWLClass(IRI.create("#" + currCls ));
			OWLDataProperty dataProp = factory.getOWLDataProperty("#"
                    + currCls, pm);
			OWLIndividual obj = factory.getOWLNamedIndividual(
                    "#" + currCls, pm);
			ArrayList<owlRuleSet> currRuleset = (ArrayList<owlRuleSet>) pair.getValue();
			for (int i=0; i< currRuleset.size(); i++){
				firstRuleSet = factory.getOWLObjectIntersectionOf(currRuleset.get(i).getRuleList(currCls));
				unionSet.add(firstRuleSet);
			}
			totalunion = factory.getOWLObjectUnionOf(unionSet);
			unionSet.clear();
			manager.addAxiom(ontology, factory.getOWLEquivalentClassesAxiom(owlCls, totalunion));
		}
            //System.out.println("about to write: " + value);
		*/
		manager.saveOntology(ontology);
		
	}

}