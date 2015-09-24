package csvToOWLRules;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import com.opencsv.CSVReader;

import owlAPI.Individual;
import owlAPI.OWLmap;
import owlAPI.OWLmap.owlRuleSet;

public class CSVToOWLRules {
    String directory;
    IRI docIRI;
    int numRules;
    HashMap<String, Integer> nameIndex = new HashMap<String, Integer>();

    public CSVToOWLRules(String csvDir, IRI documentIRI, HashMap<String, Integer> nameIndex){
        this.directory = csvDir;
        this.docIRI = documentIRI;
        this.nameIndex = nameIndex;
    }

    public OWLmap CSVRules() 
            throws OWLOntologyCreationException, NumberFormatException,
            IOException, OWLOntologyStorageException {
        /* ontology manager etc */
        OWLmap owlRulesMap = new OWLmap();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager
                .loadOntologyFromOntologyDocument(this.docIRI);
        OWLDataFactory factory = manager.getOWLDataFactory();
        final File csvFile = new File(this.directory);
        LinkedHashSet<Individual> individuals = new LinkedHashSet<Individual>();
        CSVReader reader = null;
        String[] nextLine;
        OWLObjectProperty hasParameter = null;
        String paramName = null;
        String paramValue = null;
        String eunisClass = null;
        OWLClassExpression myRestriction = null;
        Set<OWLClassExpression> ruleSet = new HashSet<OWLClassExpression>();
        //OWLClass currEunis = null;
        OWLClass currEunis = null;
        OWLClass currObjValue = null; 
        int ruleCounter = 0;
        try {
            reader = new CSVReader(new FileReader(csvFile));
            while ((nextLine = reader.readNext()) != null){
                OWLmap.owlRuleSet rule = new OWLmap.owlRuleSet(eunisClass, ruleCounter);
                rule.addAll(ruleSet);
                ruleSet.clear();
                for (Entry<String, Integer> entry : nameIndex.entrySet()) {
                    // EUNIS = 2
                    eunisClass = nextLine[2];
                    paramName = entry.getKey();
                    paramValue = nextLine[entry.getValue()];
                    if (owlRulesMap.get(eunisClass) == null){
                            ArrayList<owlRuleSet> newRules = new ArrayList<owlRuleSet>();
                            newRules.add(rule);
                           owlRulesMap.put(eunisClass, newRules); 
                           continue;
                    } else {
                        // collect parameters in ruleSet
                        ruleCounter = 0;
                        owlRulesMap.get(eunisClass).add(rule);
                    }
                    hasParameter = factory.getOWLObjectProperty(IRI.create("#" + "has_" + paramName));
                    OWLDatatype stringDatatype = factory
                            .getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI());
                    //System.out.println("about to write: " + value);
                    OWLLiteral literal = factory.getOWLLiteral(paramValue,
                            stringDatatype);
                    
                    currEunis = factory.getOWLClass(IRI.create("#" + eunisClass));
                    myRestriction = factory.getOWLObjectSomeValuesFrom(hasParameter, currEunis); //(IRI.create("#" + eunisClass));
                    //currObjValue = factory.getOWLClass(IRI.create("#" + paramValue));
                    //OWLObjectPropertyAssertionAxiom ax1 = factory.getOWLObjectPropertyAssertionAxiom(hasParameter, currEunis, currObjValue);
                    OWLClassExpression newRestriction = factory.getOWLObjectSomeValuesFrom(hasParameter, myRestriction);
                    ruleSet.add(newRestriction);
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        manager.saveOntology(ontology);
        reader.close();
        return owlRulesMap;
    }
}
