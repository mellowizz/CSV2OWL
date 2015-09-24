package csvToOWLRules;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
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

    public CSVToOWLRules(String csvDir, IRI documentIRI,
            HashMap<String, Integer> nameIndex) {
        this.directory = csvDir;
        this.docIRI = documentIRI;
        this.nameIndex = nameIndex;
    }

    public static boolean isInteger(String s, int radix) {
        Scanner sc = new Scanner(s.trim());
        if (!sc.hasNextInt(radix))
            return false;
        // we know it starts with a valid int, now make sure
        // there's nothing left!
        sc.nextInt(radix);
        return !sc.hasNext();
    }

    public List<AddAxiom> CSVRules() 
            throws OWLOntologyCreationException, NumberFormatException,
            IOException, OWLOntologyStorageException {
        /* ontology manager etc */
        OWLmap owlRulesMap = new OWLmap();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager
                .loadOntologyFromOntologyDocument(this.docIRI);
        OWLDataFactory factory = manager.getOWLDataFactory();
        final File csvFile = new File(this.directory);
        CSVReader reader = null;
        String[] nextLine;
        OWLObjectProperty hasParameter = null;
        String paramName = null;
        String paramValue = null;
        String eunisClass = null;
        OWLClassExpression myRestriction = null;
        Set<OWLClassExpression> ruleSet = new HashSet<OWLClassExpression>();
        List<AddAxiom> axiomList = new ArrayList<AddAxiom>(); 
        OWLClass currEunis = null;
        OWLClass parameterValue = null; 
        int ruleCounter = 0;
        try {
            reader = new CSVReader(new FileReader(csvFile));
            while ((nextLine = reader.readNext()) != null){
                for (Entry<String, Integer> entry : nameIndex.entrySet()) {
                    // EUNIS = 2
                    eunisClass = nextLine[2];
                    paramName = entry.getKey();
                    paramValue = nextLine[entry.getValue()];
                    paramValue = paramValue.trim();
                    if (paramValue.contains("%")) {
                        paramValue = paramValue.replace("%", "");
                    }
                    if (paramValue.contains(" ")) {
                        if (paramValue.startsWith(" ")) {
                            continue;
                        } else{
                            paramValue = paramValue.replace(" ", "_");
                        }
                    }
                    /* TODO: parse min/max labels */
                    
                    if (paramValue == null || paramValue == " " || isInteger(paramValue, 10)){
                        //System.out.println("!!! ");
                        continue;
                    } else{
                            hasParameter = factory.getOWLObjectProperty(IRI.create("#" + "has_" + paramName));
                            currEunis = factory.getOWLClass(IRI.create("#" + eunisClass));
                            parameterValue = factory.getOWLClass(IRI.create("#" + paramValue));
                            myRestriction = factory.getOWLObjectSomeValuesFrom(hasParameter, parameterValue);
                            OWLEquivalentClassesAxiom ax1 = factory.getOWLEquivalentClassesAxiom(currEunis, myRestriction);
                            AddAxiom addAx = new AddAxiom(ontology, ax1);
                            manager.applyChange(addAx);
                    //axiomList.add(addAx);
                    //ruleSet.add(myRestriction);
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        manager.saveOntology(ontology);
        reader.close();
        return axiomList;
        //return owlRulesMap;
    }
}
