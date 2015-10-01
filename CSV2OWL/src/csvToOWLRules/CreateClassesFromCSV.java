package csvToOWLRules;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import com.opencsv.CSVReader;

import owlAPI.OWLmap;
import owlAPI.OWLmap.owlRuleSet;
import owlAPI.OntologyClass;
import owlAPI.OntologyCreator;
import owlAPI.OntologyWriter;

public class CreateClassesFromCSV {

    public static void createClassesfromCSV(String fileName, int colIndex,
            File owlFile) throws IOException, OWLOntologyCreationException,
    OWLOntologyStorageException {
        /* Read from CSV */
        CSVReader reader = null;
        //String[] nextLine = null;
        LinkedHashMap<String, Integer> nameIndex = null;
        //OWLmap rulesMap = null;
        List<AddAxiom> rulesList = null;
        LinkedHashSet<OntologyClass> eunisClasses = new LinkedHashSet<OntologyClass>();
        String iriString = "http://www.user.tu-berlin.de/niklasmoran/EUNIS/"
                + owlFile.getName().trim();
        IRI owlIRI = IRI.create(owlFile.toURI());
        OWLmap theMap = null;
        try {
            /* open file */
            reader = new CSVReader(new FileReader(fileName));
            // nextLine = reader.readNext();
            nameIndex = getColIndexes(fileName);
            OntologyCreator ontCreate = new OntologyCreator();
            ontCreate.createOntology(iriString, "version_1_0", owlFile);
            IRI ontologyIRI = IRI.create(iriString);
            theMap = createEUNISObject(fileName, nameIndex, IRI.create(owlFile)); //owlIRI);
            OntologyWriter ontWrite = new OntologyWriter(); // IRI.create(owlFile.toURI()));
            ontWrite.writeMap(theMap, IRI.create(owlFile.toURI()), ontologyIRI);
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (FileNotFoundException e){
            throw new RuntimeException(e.getMessage(), e);
        }
         finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static OWLmap createEUNISObject(
            String fileName, LinkedHashMap<String, Integer> nameIndex, IRI iri) throws OWLOntologyCreationException {
       
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager
                .loadOntologyFromOntologyDocument(iri); 
        OWLDataFactory factory = manager.getOWLDataFactory();
        LinkedHashSet<OntologyClass> eunis = new LinkedHashSet<OntologyClass>();
        CSVReader reader = null;
        String[] nextLine = null;
        String parent = null; //EUNIS";
        String className = null;
        String description = null;
        String descriptionDE = null;
        String paramName = "Parameter";
        String paramValue = null;
        HashMap<String, OWLmap> owlObj = new HashMap<String, OWLmap>();
        //owlobj.put("EUNIS", new OWLmap.owlRuleSet(, ))
        //int ruleCounter = 0;
        OWLmap owlRulesMap = new OWLmap();
        int ruleCounter = 0;
        Set<OWLClassExpression> ruleSet = new HashSet<OWLClassExpression>();
        OWLClassExpression myRestriction = null;
        OWLClass currEunis = null;
        OWLClass parameterValue = null;
        OWLObjectProperty hasParameter = null;
        try {
            reader = new CSVReader(new FileReader(fileName));
            // skip header
            nextLine = reader.readNext();
            int intVal = -1;
            while ((nextLine = reader.readNext()) != null) {
                createOntoClass("EUNIS", nextLine[2], nextLine[3], nextLine[4]);
                OWLmap.owlRuleSet rule = new OWLmap.owlRuleSet (nextLine[2]);
                rule.addAll(ruleSet);
                System.out.println("RuleSet size: " + ruleSet.size());
                ruleSet.clear();
                /* add rule */
                if (owlRulesMap.get(nextLine[2]) == null){
                    ArrayList <owlRuleSet> newRules = new ArrayList<owlRuleSet>();
                    newRules.add(rule);
                    owlRulesMap.put(nextLine[2], newRules);
                    //continue;
                } else{
                    ruleCounter = 0;
                    /* already seen this class! --update by or'ing the rules! */
                    owlRulesMap.get(nextLine[2]).add(rule);
                } 
                for (Map.Entry<String, Integer> headerEntry : nameIndex.entrySet()){
                    /* paramName is the ObjectProperty */
                    paramName = headerEntry.getKey();
                    paramValue = nextLine[headerEntry.getValue()];
                    paramValue = paramValue.trim();
                    paramValue = paramValue.replaceAll("\\s", "_");
                            
                    /* TODO: refactor */ 
                    if (paramValue.contains("%")) {
                        paramValue= paramValue.replace("%", "");
                    } else if (paramValue.startsWith(" ")) {
                            continue;
                    } else if (paramValue.startsWith("0") || paramValue.startsWith("1")){
                        try{
                            if (paramValue.contains(",")){
                                paramValue = paramValue.replace(",", ".");
                            }
                           intVal = Integer.parseInt(paramValue);
                        } catch(NumberFormatException e){
                          e.printStackTrace(); 
                       }
                    }
                    parent = "Parameter";
                    description = "A parameter from " + paramName;
                    descriptionDE = "Ein Parameter von " + paramName;
                    /* create class */
                    createOntoClass(parent, paramValue, description,
                            descriptionDE);
                    
                    System.out.println("paramName: " + paramName);
                    /* create rule */ 
                    hasParameter = factory.getOWLObjectProperty(IRI.create("#" + "has_" + paramName));
                    currEunis = factory.getOWLClass(IRI.create("#" + nextLine[2]));
                    parameterValue = factory.getOWLClass(IRI.create("#" + paramValue));
                    myRestriction = factory.getOWLObjectSomeValuesFrom(hasParameter, parameterValue);
                    ruleSet.add(myRestriction);
                    //OWLmap.owlRuleSet rule = new OWLmap.owlRuleSet (paramValue); 
                    //rule.addAll(ruleSet);
                    }
                    /* add all rules */
                    manager.addAxiom(ontology, factory.getOWLEquivalentClassesAxiom(myRestriction, factory.getOWLObjectIntersectionOf(ruleSet)));
                    //OWLEquivalentClassesAxiom ax1 = factory.getOWLEquivalentClassesAxiom(currEunis, myRestriction);
                    //AddAxiom addAx = new AddAxiom(ontology, ax1);
                    //manager.applyChange(addAx);
                    ruleSet.clear();
            }
        manager.saveOntology(ontology);
        } catch (NullPointerException f) {
            f.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        }
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return owlRulesMap;
    }

    private static void createOntoClass(String parent, String cls,
            String description, String descriptionDE) {
        OntologyClass eunisObj = new OntologyClass();
        eunisObj.setName(cls);
        eunisObj.setParent(parent);
        if (description != null){
            eunisObj.setDescription(description);
            eunisObj.setDescriptionDE(descriptionDE);
        }
        if (OWLmap.eunisClasses.contains(eunisObj) == false){
            OWLmap.addClass(eunisObj);
        } else{
            System.out.println("Class already exists!");
        }
    }

    private static LinkedHashMap<String, Integer> getColIndexes (String fileName) {
        CSVReader reader = null;
        List<String> headerCols = null;
        LinkedHashMap<String, Integer> myHash = new LinkedHashMap<String, Integer>();
        try {
            reader = new CSVReader(new FileReader(fileName));
            headerCols = Arrays.asList(reader.readNext());
            for (int i = 0; i < headerCols.size(); i++) { 
                String column = headerCols.get(i);
                if (column.startsWith("EUNIS_") && !column.startsWith("EUNIS_N")
                        || column.startsWith("NATFLO")
                        || column.startsWith("EAGLE")) {
                    myHash.put(column, i);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myHash;
    }
}