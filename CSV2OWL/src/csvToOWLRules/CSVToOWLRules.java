package csvToOWLRules;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.opencsv.CSVReader;


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
        if (!sc.hasNextInt(radix)){
        	sc.close();
        	return false;
        }
        // we know it starts with a valid int, now make sure
        // there's nothing left!
        sc.nextInt(radix);
        boolean returnVal = !sc.hasNext();
        sc.close();
        return returnVal;
    }

    public List<AddAxiom> CSVRules() 
            throws OWLOntologyCreationException, NumberFormatException,
            IOException, OWLOntologyStorageException {
        /* ontology manager etc */
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
        OWLClass currEunis = null;
        OWLClass parameterValue = null; 
        List<AddAxiom> axiomList = new ArrayList<AddAxiom>();
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
                    } else if ("#" + paramValue == "#"){
                        continue;
                    } else {
                            hasParameter = factory.getOWLObjectProperty(IRI.create("#" + "has_" + paramName));
                            currEunis = factory.getOWLClass(IRI.create("#" + eunisClass));
                            parameterValue = factory.getOWLClass(IRI.create("#" + paramValue));
                            myRestriction = factory.getOWLObjectSomeValuesFrom(hasParameter, parameterValue);
                            OWLEquivalentClassesAxiom ax1 = factory.getOWLEquivalentClassesAxiom(currEunis, myRestriction);
                            AddAxiom addAx = new AddAxiom(ontology, ax1);
                            manager.applyChange(addAx);
                            /*
                        if (myExpressions.get("#" + eunisClass) == null){
                            HashSet<OWLClassExpression> lis = new HashSet<OWLClassExpression>();
                            lis.add(myRestriction);
                            //lis.add(addAx);
                            myExpressions.put("#" + eunisClass, lis);
                        }else{
                           HashSet<OWLClassExpression> myList = myExpressions.get("#" + eunisClass); 
                            //myList.add(addAx);
                           myList.add(myRestriction);
                           myExpressions.put("#" + eunisClass, myList);
                        }
                        */
                    axiomList.add(addAx);
                    //ruleSet.add(myRestriction);
                    }
                }
            }
            /*
            OWLObjectUnionOf totalunion = null;
            OWLClass owlCls = null;
            Set<OWLClassExpression> setOWL = new HashSet<OWLClassExpression>();// null; 
            OWLClassExpression rule = null;
            for (Entry<String, HashSet<OWLClassExpression>> entry : myExpressions.entrySet()){
                //setOWL = entry.getValue(); 
                owlCls = factory.getOWLClass(IRI.create(entry.getKey()));
                //totalunion = factory.getOWLObjectUnionOf(entry.getValue());
                //factory.getOWLEquivalentClassesAxiom(currEunis, totalunion);//myRestriction));)
                for (OWLClassExpression e : entry.getValue()){
                    OWLEquivalentClassesAxiom ax1 = factory.getOWLEquivalentClassesAxiom(owlCls, e); //myRestriction);
                    AddAxiom addAx = new AddAxiom(ontology, ax1);
                    manager.applyChange(addAx);
                    rule = factory.getOWLObjectIntersectionOf(e);
                   setOWL.add(rule);
                }
                totalunion = factory.getOWLObjectUnionOf(setOWL);
                setOWL.clear();
                manager.addAxiom(ontology, factory.getOWLEquivalentClassesAxiom(currEunis, totalunion));//myRestriction));
            }*/
        } catch (IOException e){
            e.printStackTrace();
        }
        manager.saveOntology(ontology);
        reader.close();
        return axiomList;
        //return owlRulesMap;
    }
}
