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

import org.semanticweb.HermiT.structural.OWLAxioms;
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

    public static String extractNumber(final String myStr) {

        if (myStr == null || myStr.isEmpty())
            return "";
        String foundDigit = "";
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        for (char c : myStr.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append(c);
                found = true;
            } else if (Character.compare(c, ',') == 0) {
                sb.append('.');
            } else if (found) {
                // If we already found a digit before and this char is not a
                // digit, stop looping
                break;
            }
        }
        if (sb != null) {
            foundDigit = sb.toString();
        }
        return foundDigit;
    }

    public void createOntologyObject(
            LinkedHashMap<String, Integer> nameIndex, String fileName)
                    throws OWLOntologyCreationException, OWLOntologyStorageException {
        CSVReader reader = null;
        String[] nextLine = null;
        try {
            reader = new CSVReader(new FileReader(fileName));
            // skip header
            nextLine = reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                /* "eunis" */
                String className = nextLine[2];
                owlRuleSet owlRules = buildRuleSet(nextLine, nameIndex);
                if (this.owlRulesMap.get(className) == null) {
                    ArrayList<owlRuleSet> newRules = new ArrayList<owlRuleSet>();
                    newRules.add(owlRules);
                    this.owlRulesMap.put(className, newRules);
                } else {
                    ArrayList<owlRuleSet> existingRules = this.owlRulesMap
                            .pop(className);
                    existingRules.add(owlRules);
                    this.owlRulesMap.put(className, existingRules);
                }
            }
        } catch (NullPointerException f) {
            f.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /* done with file */
        /* write rules */
        OWLClassExpression firstRuleSet = null;
        OWLClass owlCls = null;
        // owlCls.getIRI().getShortForm();
        OWLObjectUnionOf totalunion = null;
        Iterator it = this.owlRulesMap.map.entrySet().iterator(); // .itor(); // entrySet().iterator();
        Set<OWLClassExpression> unionSet = new HashSet<OWLClassExpression>();
        OWLClass topParentCls = null;
        OWLClass parentCls = null;
        OWLDataFactory dataFactory = this.manager.getOWLDataFactory();
        OWLClass thing = dataFactory.getOWLThing();
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String currCls = (String) pair.getKey();
            owlCls = dataFactory.getOWLClass(IRI.create("#" + currCls));
            ArrayList<owlRuleSet> currRuleset = (ArrayList<owlRuleSet>) pair
                    .getValue();
            List<String> currParents = currRuleset.get(0).getParent();
            OWLClass ancestor = dataFactory
                    .getOWLClass(IRI.create("#" + currParents.remove(0)));

            for (int i = 0; i < currRuleset.size(); i++) {
                firstRuleSet = dataFactory.getOWLObjectIntersectionOf(
                        currRuleset.get(i).getRuleList(currCls));
                unionSet.add(firstRuleSet);
            }
            totalunion = dataFactory.getOWLObjectUnionOf(unionSet);
            unionSet.clear();
            manager.addAxiom(ontology, dataFactory
                    .getOWLEquivalentClassesAxiom(owlCls, totalunion));
        }
        manager.saveOntology(ontology, this.documentIRI);
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private owlRuleSet buildRuleSet(String[] nextLine,
            LinkedHashMap<String, Integer> nameIndex) {
        OWLDataFactory dataFactory = this.manager.getOWLDataFactory();
        List<String> parents = new ArrayList<String>(); // "Parameter, "EUNIS";
        String description = null;
        String descriptionDE = null;
        String paramName = "Parameter";
        String paramValue = null;
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        OWLClassExpression myRestriction = null;
        OWLClass parameterValue = null;
        OWLObjectProperty hasParameter = null;
        OWLDataProperty hasDataProperty = null;
        OWLLiteral literal = null;
        OWLDatatype booleanDataType = dataFactory.getBooleanOWLDatatype();
        OWLDatatypeRestriction newDataRestriction = null;
        Double myVal = -1.0;
        String extractedValue = "";
        parents.add("EUNIS");
        String className = nextLine[2];
        description = nextLine[3];
        descriptionDE = nextLine[4];
        /* create rules for EUNIS */
        OWLmap.owlRuleSet owlRules = new OWLmap.owlRuleSet(className, parents,
                description, descriptionDE);
        /* loop over parameters and cleanup */
        for (Map.Entry<String, Integer> headerEntry : nameIndex.entrySet()) {
            /* paramName is the ObjectProperty */
            paramName = headerEntry.getKey();
            paramValue = nextLine[headerEntry.getValue()];
            if (paramValue.length() == 0 || paramValue == null
                    || paramValue.contains("?")) {
                continue;
            }
            /* TODO: refactor */
            paramValue = paramValue.trim();
            paramValue = paramValue.replaceAll("\\s", "_");
            if (paramValue.contains("%")) {
                paramValue = paramValue.replace("%", "");
            }
            /* got number hopefully */
            extractedValue = extractNumber(paramValue);
            // System.out.println("extractedValue: " + extractedValue);
            if (extractedValue != "" || extractedValue.isEmpty()) {
                hasDataProperty = dataFactory.getOWLDataProperty(
                        IRI.create("#" + "has_" + paramName));
                // System.out.println("paramName: " + paramValue);
                if (paramName.contains("max") || paramName.contains("min")
                        && !paramName.contains("dominant")) {
                    /* dataType restriction */
                    try {
                        myVal = Double.parseDouble(extractedValue);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (paramName.contains("max")) {
                        newDataRestriction = dataFactory
                                .getOWLDatatypeMaxExclusiveRestriction(myVal);
                        myRestriction = dataFactory.getOWLDataSomeValuesFrom(
                                hasDataProperty, newDataRestriction);
                    } else {
                        newDataRestriction = dataFactory
                                .getOWLDatatypeMinExclusiveRestriction(myVal);
                        myRestriction = dataFactory.getOWLDataSomeValuesFrom(
                                hasDataProperty, newDataRestriction);
                    }
                } else if (paramValue.matches("0")) {
                    literal = dataFactory.getOWLLiteral("false",
                            booleanDataType);
                    OWLDataOneOf boolFalse = dataFactory
                            .getOWLDataOneOf(literal);
                    myRestriction = dataFactory.getOWLDataSomeValuesFrom(
                            hasDataProperty, boolFalse);

                } else if (paramValue.matches("1")) {
                    literal = dataFactory.getOWLLiteral("true",
                            booleanDataType);
                    OWLDataOneOf boolFalse = dataFactory
                            .getOWLDataOneOf(literal);
                    myRestriction = dataFactory.getOWLDataSomeValuesFrom(
                            hasDataProperty, boolFalse);
                }
            } else {
                /* neither boolean nor starting with a digit */
                parents.clear();
                String[] paramList = null;
                OWLObjectUnionOf totalunion1 = null;
                Set<OWLClassExpression> unionSet1 = new HashSet<OWLClassExpression>();
                hasParameter = dataFactory.getOWLObjectProperty(
                        IRI.create("#" + "has_" + paramName));
                if (paramValue.contains("/")) {
                    paramList = paramValue.split("/");
                    for (String s : paramList) {
                        parameterValue = dataFactory
                                .getOWLClass(IRI.create("#" + s));
                        myRestriction = dataFactory.getOWLObjectSomeValuesFrom(
                                hasParameter, parameterValue); // parameterValue);
                        unionSet1.add(myRestriction);
                        parents.add("Parameter");
                        parents.add(paramName);
                        parents.add(paramValue);
                        description = "A parameter from " + paramName;
                        descriptionDE = "Ein Parameter von " + paramName;
                        /* create class */
                    }
                    totalunion1 = dataFactory.getOWLObjectUnionOf(unionSet1);
                    owlRules.rules.add(totalunion1);
                    myRestriction = null;
                } else {
                    parameterValue = dataFactory
                            .getOWLClass(IRI.create("#" + paramValue));
                    /* which restriction */
                    myRestriction = dataFactory.getOWLObjectSomeValuesFrom(
                            hasParameter, parameterValue); // parameterValue);
                    /* before was outside */
                    parents.add("Parameter");
                    parents.add(paramName);
                    parents.add(paramValue);
                    description = "A parameter from " + paramName;
                    descriptionDE = "Ein Parameter von " + paramName;
                    /* create class */
                    // createOntoClass(manager, this.ontology,
                    // this.ontologyIRI, dataFactory, parents,
                    // paramValue, description, descriptionDE);
                }
            }

            /* create rule */
            if (myRestriction != null) {
                // ruleSet.add(myRestriction);
                owlRules.addRule(myRestriction);
            }
        }
        return owlRules;
    }

}