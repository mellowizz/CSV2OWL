package csvToOWLRules;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

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
        Set<OWLClassExpression> ruleSet = new HashSet<OWLClassExpression>();
        OWLClass currEunis = null;
        int ruleCounter = 0;
        try {
            reader = new CSVReader(new FileReader(csvFile));
            while ((nextLine = reader.readNext()) != null){
                for (Entry<String, Integer> entry : nameIndex.entrySet()) {
                    // EUNIS = 2
                    eunisClass = nextLine[2];
                    paramName = entry.getKey();
                    paramValue = nextLine[entry.getValue()];
                    OWLmap.owlRuleSet rule = new OWLmap.owlRuleSet(eunisClass, ruleCounter);
                    rule.addAll(ruleSet);
                    ruleSet.clear();
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
                    currEunis = factory.getOWLClass(IRI.create("#" + eunisClass));
                    OWLClassExpression newRestriction = factory.getOWLObjectSomeValuesFrom(hasParameter, currEunis);
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
