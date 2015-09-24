package csvToOWLRules;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.opencsv.CSVReader;

import owlAPI.Individual;
import owlAPI.OWLmap;
import owlAPI.OntologyClass;
import owlAPI.OntologyCreator;
import owlAPI.OntologyWriter;

public class CreateClassesFromCSV {

    public static void createClassesfromCSV(String fileName, int colIndex,
            File owlFile) throws IOException, OWLOntologyCreationException,
    OWLOntologyStorageException {
        /* Read from CSV */
        CSVReader reader = null;
        String[] nextLine = null;
        HashMap<String, Integer> nameIndex = null;
        OWLmap rulesMap = null;
        LinkedHashSet<OntologyClass> eunisClasses = new LinkedHashSet<OntologyClass>();
        String ontologyIRI = "http://www.user.tu-berlin.de/niklasmoran/EUNIS/"
                + owlFile.getName().trim();
        IRI owlIRI = IRI.create(owlFile.toURI());
        LinkedHashSet<Individual> individuals = null;
        try {
            File csvFile = new File(fileName);
            /* open file */
            reader = new CSVReader(new FileReader(fileName));
            // nextLine = reader.readNext();
            nameIndex = getColIndexes(fileName);
            OntologyCreator ontCreate = new OntologyCreator();
            ontCreate.createOntology(ontologyIRI, "version_1_0", owlFile);

            eunisClasses = createEUNISObject(fileName, nameIndex, owlIRI);
            individuals = CreateIndividualsFromCSV.createIndividualsFromCSV(fileName, nameIndex);
            CSVToOWLRules therules = new CSVToOWLRules(fileName, owlIRI,
                    nameIndex);
            rulesMap = therules.CSVRules();
            OntologyWriter ontWrite = new OntologyWriter(); // IRI.create(owlFile.toURI()));
            ontWrite.writeAll(eunisClasses, individuals, rulesMap, owlIRI,
                    IRI.create(ontologyIRI));
        } catch (OWLOntologyCreationException e) {
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

    private static LinkedHashSet<OntologyClass> createEUNISObject(
            String fileName, HashMap<String, Integer> nameIndex, IRI iri) {
        LinkedHashSet<OntologyClass> eunis = new LinkedHashSet<OntologyClass>();
        CSVReader reader = null;
        String[] nextLine = null;
        String parent = "EUNIS";
        String className = null;
        String description = null;
        String descriptionDE = null;
        int ruleCounter = 0;
        try {
            reader = new CSVReader(new FileReader(fileName));
            nextLine = reader.readNext();
            Set<OWLClassExpression> ruleSet = new HashSet<OWLClassExpression>();
            while ((nextLine = reader.readNext()) != null) {
                className = nextLine[2];
                description = nextLine[3];
                descriptionDE = nextLine[4];
                if (className == null) {
                    continue;
                }
                className = className.trim();
                if (className.contains("%")) {
                    className = className.replace("%", "");
                }
                if (className.contains(" ")) {
                    if (className.startsWith(" ")) {
                        className = className.replace(" ", "");
                    }
                    className = className.replace(" ", "_");
                }
                OntologyClass eunisObj = new OntologyClass();
                if (description != null) {
                    eunisObj.setDescription(description);
                    eunisObj.setDescriptionDE(descriptionDE);
                }
                if (eunisObj.inSet(className) == false) {
                    eunisObj.setName(className);
                    eunisObj.setParent(parent);
                    eunis.add(eunisObj);
                    /*
                     * if (myMap.get(className) == null){ myMap.put(className,
                     * eunisObj);
                     */
                    System.out.println("Added: " + className);
                }
            }
            String entries = "";
            for (OntologyClass c : eunis) {
                entries += c.getName() + " ";
            }
        } catch (NullPointerException f) {
            f.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return eunis;
    }

    private static HashMap<String, Integer> getColIndexes(String fileName) {
        CSVReader reader = null;
        List<String> headerCols = null;
        HashMap<String, Integer> myHash = new HashMap<String, Integer>();
        try {
            reader = new CSVReader(new FileReader(fileName));
            headerCols = Arrays.asList(reader.readNext());
            for (int i = 0; i < headerCols.size(); i++) { // String column :
                // headerCols){
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