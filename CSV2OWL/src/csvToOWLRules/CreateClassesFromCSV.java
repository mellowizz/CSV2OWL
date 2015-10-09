package csvToOWLRules;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semarglproject.vocab.XSD;

import com.opencsv.CSVReader;

import owlAPI.OntologyCreator;

public class CreateClassesFromCSV {

    public static void createClassesfromCSV(String fileName, int colIndex,
            File owlFile) throws IOException, OWLOntologyCreationException,
                    OWLOntologyStorageException {
        /* Read from CSV */
        CSVReader reader = null;
        LinkedHashMap<String, Integer> nameIndex = null;
        String iriString = "http://www.user.tu-berlin.de/niklasmoran/EUNIS/"
                + owlFile.getName().trim();
        try {
            /* open file */
            reader = new CSVReader(new FileReader(fileName));
            nameIndex = getColIndexes(fileName);
            /* create ontology */
            OntologyCreator ontCreate = new OntologyCreator();
            ontCreate.createOntology(iriString, "version_1_0", owlFile);
            IRI ontologyIRI = IRI.create(iriString);
            createEUNISObject(fileName, nameIndex, IRI.create(owlFile),
                    ontologyIRI); // owlIRI);
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void createEUNISObject(String fileName,
            LinkedHashMap<String, Integer> nameIndex, IRI iri, IRI ontologyIRI)
                    throws OWLOntologyCreationException {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(iri);
        OWLDataFactory factory = manager.getOWLDataFactory();
        CSVReader reader = null;
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
        OWLDatatype booleanDataType = factory.getOWLDatatype(OWL2Datatype.XSD_BOOLEAN.getIRI());
        OWLDatatypeRestriction newDataRestriction = null;
        try {
            reader = new CSVReader(new FileReader(fileName));
            // skip header
            nextLine = reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                /* "eunis" */
                parents.add("EUNIS");
                currEunis = createOntoClass(manager, ontology, ontologyIRI,
                        factory, parents, nextLine[2], nextLine[3],
                        nextLine[4]);
                /* loop over parameters and cleanup */
                for (Map.Entry<String, Integer> headerEntry : nameIndex
                        .entrySet()) {
                    /* paramName is the ObjectProperty */
                    paramName = headerEntry.getKey();
                    paramValue = nextLine[headerEntry.getValue()];
                    if (paramValue == "" || paramValue == null || !paramValue.matches("\\w+")) {
                        continue;
                    }
                    /* TODO: refactor */
                    paramValue = paramValue.replaceAll("\\s", "_");
                    if (paramValue.contains("%")) {
                        paramValue = paramValue.replace("%", "");
                    }
                    if (paramValue.matches("\\^\\d+")){
                        if (paramValue.contains(",")) {
                            try{
                                paramValue = paramValue.replace(",", ".");
                                doubleVal = Double.parseDouble(paramValue);
                            }catch (NumberFormatException e){
                                e.printStackTrace();
                            }
                        } else if (paramValue.startsWith("0")){
                            //literal = factory.getOWLLiteral("false", booleanDataType);
                            //OWLDataOneOf blah = factory.getOWLDataOneOf(
                            //               literal);
                            factory.getOWLLiteral(true);
                            //OWLDataHasValue owlBoolean = factory.getOWLDataHasValue(newDataRestriction,XSD.BOOLEAN);
                            //OWLDataPropertyAssertionAxiom
                            hasDataProperty = factory.getOWLDataProperty(IRI.create(base + "hasName"));
                            OWLNamedIndividual currCls = factory.getOWLNamedIndividual("#" + "has_" + paramName);
                            newDataRestriction = factory.getOWLDatatypeRestriction(hasParameter, literal);
                            OWLObjectPropertyAssertionAxoim boo = factory.getOWLObjectPropertyAssertionAxiom();
                            OWLDataPropertyAssertionAxiom dataPropertyAssertion = factory
                                    .getOWLDataPropertyAssertionAxiom(dataProp, obj,
                                            literal);
                            //OWLDataHasValue owlBoolean = factory.getOWLDataHasValue(newDataRestriction, XSD.BOOLEAN);
                        } else if (paramValue.startsWith("1")){
                            //literal = factory.getOWLLiteral("true", booleanDataType);
                            //literal = factory.getOWLLiteral(paramName, booleanDataType);
                            newDataRestriction = factory.getOWLDatatypeRestriction(, arg1);
                    
                        }
                        if (paramName.contains("max")){
                            newDataRestriction = factory
                                    .getOWLDatatypeMaxExclusiveRestriction(doubleVal);
                        } else if (paramName.contains("min")){
                            newDataRestriction = factory
                                    .getOWLDatatypeMinExclusiveRestriction(doubleVal);
                            
                        }
                        
                    }
                    /* got number hopefully */
                    /* paramName contains max/min?! */
                        
                    parents.clear();
                    parents.add("Parameter");
                    parents.add(paramName);
                    parents.add(paramValue);
                    description = "A parameter from " + paramName;
                    descriptionDE = "Ein Parameter von " + paramName;
                    /* create class */
                    createOntoClass(manager, ontology, ontologyIRI, factory,
                            parents, paramValue, description, descriptionDE);

                    System.out.println("paramName: " + paramName);
                    /* create rule */
                    hasParameter = factory.getOWLObjectProperty(
                            IRI.create("#" + "has_" + paramName));
                    parameterValue = factory
                            .getOWLClass(IRI.create("#" + paramValue));
                    /* which restriction */
                    myRestriction = factory.getOWLObjectSomeValuesFrom(
                            hasParameter, parameterValue); // parameterValue);
                    myRestriction = factory
                            .getOWLDataSomeValuesFrom(hasDataProperty,
                                    newDataRestriction); 
                 
                    ruleSet.add(myRestriction);
                }
                /* add all rules */
                manager.addAxiom(ontology,
                        factory.getOWLEquivalentClassesAxiom(currEunis,
                                factory.getOWLObjectIntersectionOf(ruleSet)));
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
    }

    private static OWLClass createOntoClass(OWLOntologyManager manager,
            OWLOntology ontology, IRI ontologyIRI, OWLDataFactory factory,
            List<String> parents, String clazz, String description,
            String descriptionDE) {
        OWLClass topParentCls = null; 
        OWLClass parentCls = null;
        OWLClass thing = factory.getOWLThing();
        OWLClass cls = null;
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        OWLClass ancestor = factory.getOWLClass(IRI.create(ontologyIRI + "#" + parents.remove(0)));
        /* loop over children */
        cls = factory
                .getOWLClass(IRI.create(ontologyIRI + "#" + clazz));
        if (parents.isEmpty()){
            axioms.add(factory.getOWLSubClassOfAxiom(cls, ancestor));
        } else if (parents.size() == 1){
            parentCls = factory
                    .getOWLClass(IRI.create(ontologyIRI + "#" + parents.remove(0)));
            axioms.add(factory.getOWLSubClassOfAxiom(cls, parentCls));
            axioms.add(factory.getOWLSubClassOfAxiom(parentCls, ancestor));
            
        } else{
            System.out.println("more than two parents!");
            topParentCls = factory
                    .getOWLClass(IRI.create(ontologyIRI + "#" + parents.remove(0)));
            parentCls = factory
                    .getOWLClass(IRI.create(ontologyIRI + "#" + parents.remove(0)));
            axioms.add(factory.getOWLSubClassOfAxiom(parentCls, topParentCls));
            axioms.add(factory.getOWLSubClassOfAxiom(topParentCls, ancestor));
        }
        axioms.add(factory.getOWLSubClassOfAxiom(ancestor, thing));
        manager.addAxioms(ontology, axioms);
        if (description != null) {
            OWLAnnotation commentAnno = factory.getOWLAnnotation(
                    factory.getRDFSComment(),
                    factory.getOWLLiteral(description, "en"));
            OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(cls.getIRI(),
                    commentAnno);
            manager.applyChange(new AddAxiom(ontology, ax));
            OWLAnnotation commentDE = factory.getOWLAnnotation(
                    factory.getRDFSComment(),
                    factory.getOWLLiteral(descriptionDE, "de"));
            OWLAxiom axDE = factory.getOWLAnnotationAssertionAxiom(cls.getIRI(),
                    commentDE);
            manager.applyChange(new AddAxiom(ontology, axDE));
        }
        return cls;
    }

    private static LinkedHashMap<String, Integer> getColIndexes(
            String fileName) {
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