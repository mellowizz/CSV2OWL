package owlAPI;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.AddAxiom;
//import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.SetOntologyID;

import com.opencsv.CSVReader;

import owlAPI.OWLmap.owlRuleSet;

public class OntologyCreator {
    /*
     * 
     * Creates ontology with onotology IRI and saves it to input OWL File
     */
    private OWLOntologyManager manager;
    OWLOntology ontology;
    private IRI ontoIRI;
    private OWLOntologyID newOntologyID;
    private SetOntologyID setOntologyID;
    private IRI versionIRI;
    private IRI ontologyIRI;
    private IRI documentIRI;
    private OWLmap owlRulesMap = new OWLmap();

    //private defaultDict<String, Set<OWLAxiom>> ontoDict = new defaultDict<String, Set<OWLAxiom>>(ArrayList.class);
    //private defaultDict<String, List<Set<OWLClassExpression>>> ontoDict = new defaultDict<String, List<Set<OWLClassExpression>>>(ArrayList.class);

    public void createOntology(String ontologyIRIasString, String version,
            File owlFile) throws OWLOntologyCreationException,
                    OWLOntologyStorageException {
        try {
            this.manager = OWLManager.createOWLOntologyManager();
            // PriorityCollection<OWLOntologyIRIMapper> iriMappers =
            // manager.getIRIMappers();
            this.ontologyIRI = IRI.create(ontologyIRIasString);

            this.documentIRI = IRI.create(owlFile.toURI());
            // SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI,
            // documentIRI);
            // manager.addIRIMapper(mapper);

            this.ontology = manager.createOntology(ontologyIRI);

            this.versionIRI = IRI.create(ontologyIRI + "/version1");

            this.newOntologyID = new OWLOntologyID(ontologyIRI, versionIRI);

            this.setOntologyID = new SetOntologyID(ontology, newOntologyID);
            manager.applyChange(setOntologyID);

            save(ontologyIRI, ontology, owlFile);
        } catch (OWLOntologyCreationException e) {
            System.out.println("Could not load ontology: " + e.getMessage());
        }
    }

    public void save(IRI ontologyIRI, OWLOntology ontology, File owlFile)
            throws OWLOntologyCreationException, OWLOntologyStorageException {

        // OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        OWLXMLDocumentFormat owlxmlFormat = new OWLXMLDocumentFormat();
        /* Save OWL to file */
        manager.saveOntology(ontology, owlxmlFormat,
                IRI.create(owlFile.toURI()));
    }

    public OWLOntologyID getOWLOntologyID() {
        return this.newOntologyID;
    }

    public IRI getIRI() {
        return this.ontoIRI;
    }

    public void createOntologyObject(LinkedHashMap<String, Integer> nameIndex,
            String fileName) throws OWLOntologyCreationException {
        CSVReader reader = null;
        Set<OWLClassExpression> unionRule = new HashSet<OWLClassExpression>();
        OWLDataFactory dataFactory = this.manager.getOWLDataFactory();
        String[] nextLine = null;
        List<String> parents = new ArrayList<String>(); // "Parameter, "EUNIS";
        String description = null;
        String descriptionDE = null;
        String paramName = "Parameter";
        String paramValue = null;
        Set<OWLClassExpression> ruleSet = new HashSet<OWLClassExpression>();
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        OWLClassExpression myRestriction = null;
        OWLClass currEunis = null;
        OWLClass parameterValue = null;
        OWLObjectProperty hasParameter = null;
        OWLDataProperty hasDataProperty = null;
        Double doubleVal = -1.0;
        OWLLiteral literal = null;
        OWLDatatype booleanDataType = dataFactory.getBooleanOWLDatatype();
        OWLDatatypeRestriction newDataRestriction = null;
        OWLLiteral hasBoolean = null;
        Double myVal = null;
        try {
            reader = new CSVReader(new FileReader(fileName));
            // skip header
            nextLine = reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                /* "eunis" */
                parents.add("EUNIS");
                currEunis = createOntoClass(this.manager, this.ontology,
                        this.ontologyIRI, dataFactory, parents, nextLine[2],
                        nextLine[3], nextLine[4]);
                String className = nextLine[2];
                /* loop over parameters and cleanup */
                for (Map.Entry<String, Integer> headerEntry : nameIndex
                        .entrySet()) {
                    /* paramName is the ObjectProperty */
                    paramName = headerEntry.getKey();
                    paramValue = nextLine[headerEntry.getValue()];
                    if (paramValue.length() == 0 || paramValue == null
                            || paramValue.contains("?")) {
                        System.out.println("Skipped: " + paramValue);
                        continue;
                    }
                    /* TODO: refactor */
                    paramValue = paramValue.trim();
                    paramValue = paramValue.replaceAll("\\s", "_");
                    if (paramValue.contains("%")) {
                        paramValue = paramValue.replace("%", "");
                    }
                    /* got number hopefully */
                    /* paramName contains max/min?! */
                    /* fix! */

                    if (paramValue.matches("^\\d+")) {
                        if (paramValue.startsWith("?"))
                            continue;
                        hasDataProperty = dataFactory.getOWLDataProperty(
                                IRI.create("#" + "has_" + paramName));
                        // System.out.println("paramName: " + paramValue);
                        if (paramName.contains("max")
                                || paramName.contains("min")
                                        && !paramName.contains("dominant")) {
                            /* dataType restriction */
                            try {
                                paramValue = paramValue.replace(",", ".");
                                myVal = Double.parseDouble(paramValue);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                            if (paramName.contains("max")) {
                                newDataRestriction = dataFactory
                                        .getOWLDatatypeMaxExclusiveRestriction(
                                                myVal);
                                myRestriction = dataFactory
                                        .getOWLDataSomeValuesFrom(
                                                hasDataProperty,
                                                newDataRestriction);
                            } else {
                                newDataRestriction = dataFactory
                                        .getOWLDatatypeMinExclusiveRestriction(
                                                myVal);
                                myRestriction = dataFactory
                                        .getOWLDataSomeValuesFrom(
                                                hasDataProperty,
                                                newDataRestriction);
                            }
                        } else if (paramValue.matches("0")) {
                            literal = dataFactory.getOWLLiteral("false",
                                    booleanDataType);
                            OWLDataOneOf boolFalse = dataFactory
                                    .getOWLDataOneOf(literal);
                            myRestriction = dataFactory
                                    .getOWLDataSomeValuesFrom(hasDataProperty,
                                            boolFalse);

                        } else if (paramValue.matches("1")) {
                            literal = dataFactory.getOWLLiteral("true",
                                    booleanDataType);
                            OWLDataOneOf boolFalse = dataFactory
                                    .getOWLDataOneOf(literal);
                            myRestriction = dataFactory
                                    .getOWLDataSomeValuesFrom(hasDataProperty,
                                            boolFalse);
                        }
                    } else {
                        /* neither boolean nor starting with a digit */
                        parents.clear();
                        String[] paramList = null;
                        OWLObjectUnionOf totalunion = null;
                        Set<OWLClassExpression> unionSet = new HashSet<OWLClassExpression>();
                        hasParameter = dataFactory.getOWLObjectProperty(
                                IRI.create("#" + "has_" + paramName));
                        if (paramValue.contains("/")) {
                            paramList = paramValue.split("/");
                            for (String s : paramList) {
                                parameterValue = dataFactory
                                        .getOWLClass(IRI.create("#" + s));
                                myRestriction = dataFactory
                                        .getOWLObjectSomeValuesFrom(
                                                hasParameter, parameterValue); // parameterValue);
                                unionSet.add(myRestriction);
                                parents.add("Parameter");
                                parents.add(paramName);
                                parents.add(paramValue);
                                description = "A parameter from " + paramName;
                                descriptionDE = "Ein Parameter von "
                                        + paramName;
                                /* create class */
                                /* ontologyIRI */
                                createOntoClass(manager, this.ontology,
                                        this.ontologyIRI, dataFactory, parents,
                                        paramValue, description, descriptionDE);
                            }
                            totalunion = dataFactory
                                    .getOWLObjectUnionOf(unionSet);
                            //this.ontoDict.get(nextLine[2]).add(unionSet);
                            //dataFactory.getOWLEquivalentClassesAxiom(currEunis, totalunion);
                            ruleSet.add(totalunion);
                            //manager.addAxiom(ontology,
                            //        dataFactory.getOWLEquivalentClassesAxiom(
                            //                currEunis, totalunion));
                        } else {
                            parameterValue = dataFactory
                                    .getOWLClass(IRI.create("#" + paramValue));
                            /* which restriction */
                            myRestriction = dataFactory
                                    .getOWLObjectSomeValuesFrom(hasParameter,
                                            parameterValue); // parameterValue);
                            /* before was outside */
                            parents.add("Parameter");
                            parents.add(paramName);
                            parents.add(paramValue);
                            description = "A parameter from " + paramName;
                            descriptionDE = "Ein Parameter von " + paramName;
                            /* create class */
                            /* ontologyIRI */
                            createOntoClass(manager, this.ontology,
                                    this.ontologyIRI, dataFactory, parents,
                                    paramValue, description, descriptionDE);
                        }
                    }

                    /* create rule */
                    if (myRestriction != null) {
                        ruleSet.add(myRestriction);
                    }
                }
                /* add all rules 
               
                manager.addAxiom(ontology,
                        dataFactory.getOWLEquivalentClassesAxiom(currEunis,
                                dataFactory
                                        .getOWLObjectIntersectionOf(ruleSet)));
                this.ontoDict.get(nextLine[2]).add(ruleSet); */
                if (this.owlRulesMap.get(className) == null){
                    OWLmap.owlRuleSet rule = new OWLmap.owlRuleSet (className);
                    rule.addAll(ruleSet);
                    ArrayList <owlRuleSet> newRules = new ArrayList<owlRuleSet>();
                    newRules.add(rule);
                    this.owlRulesMap.put(className, newRules);
                } else{
                    ArrayList<owlRuleSet> existingRules = this.owlRulesMap.pop(className);
                    owlRuleSet the_rules = existingRules.remove(0);
                    ruleSet.addAll(the_rules.getRuleList(className));
                    OWLmap.owlRuleSet rule = new OWLmap.owlRuleSet (className); //, ruleCounter);
                    ArrayList <owlRuleSet> newRules = new ArrayList<owlRuleSet>();
                    rule.addAll(ruleSet);
                    newRules.add(rule);
                    this.owlRulesMap.put(className, newRules);
                }
                    ruleSet.clear();
            }
            /* done with file */
            /* write rules */
            OWLClassExpression firstRuleSet= null;
            OWLClass owlCls = null;
            OWLObjectUnionOf totalunion = null;
            Iterator it = this.owlRulesMap.map.entrySet().iterator();
            Set<OWLClassExpression> unionSet = new HashSet<OWLClassExpression>();
            while (it.hasNext()){
                Map.Entry pair = (Map.Entry) it.next();
                String currCls = (String) pair.getKey();
                owlCls = dataFactory.getOWLClass(IRI.create("#" + currCls ));
                ArrayList<owlRuleSet> currRuleset = (ArrayList<owlRuleSet>) pair.getValue();
                for (int i=0; i< currRuleset.size(); i++){
                    firstRuleSet = dataFactory.getOWLObjectIntersectionOf(currRuleset.get(i).getRuleList(currCls));
                    unionSet.add(firstRuleSet);
                }
                totalunion = dataFactory.getOWLObjectUnionOf(unionSet);
                unionSet.clear();
                manager.addAxiom(ontology, dataFactory.getOWLEquivalentClassesAxiom(owlCls, totalunion));
            }            
            manager.saveOntology(ontology, this.documentIRI);
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
    }

    private static OWLClass createOntoClass(OWLOntologyManager manager,
            OWLOntology ontology, IRI ontologyIRI, OWLDataFactory dataFactory,
            List<String> parents, String clazz, String description,
            String descriptionDE) {
        OWLClass topParentCls = null;
        OWLClass parentCls = null;
        OWLClass thing = dataFactory.getOWLThing();
        OWLClass cls = null;
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        OWLClass ancestor = dataFactory
                .getOWLClass(IRI.create(ontologyIRI + "#" + parents.remove(0)));
        /* loop over children */
        cls = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#" + clazz));
        if (parents.isEmpty()) {
            axioms.add(dataFactory.getOWLSubClassOfAxiom(cls, ancestor));
        } else if (parents.size() == 1) {
            parentCls = dataFactory.getOWLClass(
                    IRI.create(ontologyIRI + "#" + parents.remove(0)));
            axioms.add(dataFactory.getOWLSubClassOfAxiom(cls, parentCls));
            axioms.add(dataFactory.getOWLSubClassOfAxiom(parentCls, ancestor));

        } else {
            // System.out.println("more than two parents!");
            topParentCls = dataFactory.getOWLClass(
                    IRI.create(ontologyIRI + "#" + parents.remove(0)));
            parentCls = dataFactory.getOWLClass(
                    IRI.create(ontologyIRI + "#" + parents.remove(0)));
            axioms.add(
                    dataFactory.getOWLSubClassOfAxiom(parentCls, topParentCls));
            axioms.add(
                    dataFactory.getOWLSubClassOfAxiom(topParentCls, ancestor));
        }
        axioms.add(dataFactory.getOWLSubClassOfAxiom(ancestor, thing));
        manager.addAxioms(ontology, axioms);
        if (description != null) {
            OWLAnnotation commentAnno = dataFactory.getOWLAnnotation(
                    dataFactory.getRDFSComment(),
                    dataFactory.getOWLLiteral(description, "en"));
            OWLAxiom ax = dataFactory
                    .getOWLAnnotationAssertionAxiom(cls.getIRI(), commentAnno);
            manager.applyChange(new AddAxiom(ontology, ax));
            OWLAnnotation commentDE = dataFactory.getOWLAnnotation(
                    dataFactory.getRDFSComment(),
                    dataFactory.getOWLLiteral(descriptionDE, "de"));
            OWLAxiom axDE = dataFactory
                    .getOWLAnnotationAssertionAxiom(cls.getIRI(), commentDE);
            manager.applyChange(new AddAxiom(ontology, axDE));
        }
        return cls;
    }
}