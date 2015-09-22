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
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLClassExpression;

//import org.semanticweb.owlapi.model.*;
import com.opencsv.CSVReader;

import owlAPI.OWLmap;
import owlAPI.OntologyClass;
import owlAPI.OntologyCreator;
import owlAPI.OntologyWriter;

public class CreateClassesFromCSV {

    public static void createClassesfromCSV(String fileName,
            int colIndex, File owlFile) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException {
        /* Read from CSV */
        try{
            OntologyCreator ontCreate = new OntologyCreator();
            String ontologyIRI = "http://www.user.tu-berlin.de/niklasmoran/EUNIS/"
                    + owlFile.getName().trim();
            ontCreate.createOntology(ontologyIRI, "version_1_0", owlFile);
            
            //OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            //OWLOntology ontology = manager
            //        .loadOntologyFromOntologyDocument(IRI.create(ontologyIRI)); 
            //OWLDataFactory factory = manager.getOWLDataFactory();
            File csvFile = new File(fileName);
            LinkedHashSet<OntologyClass> eunisClasses = new LinkedHashSet<OntologyClass>();
            CSVReader reader = null;
            String[] nextLine;
            String eunisParent = null;
                
            reader = new CSVReader(new FileReader(fileName));
            eunisClasses = createEUNISObject(reader);
            OWLmap rulesMap = null;
            if (csvFile.isFile()) {
                CSVToOWLRules therules = new CSVToOWLRules(fileName,
                        IRI.create(owlFile.toURI()));
                rulesMap = therules.CSVRules();
            }
            /* if another parameter? */
            OntologyWriter ontWrite = new OntologyWriter();
            ontWrite.writeAll(eunisClasses, rulesMap,
                    IRI.create(owlFile.toURI()), IRI.create(ontologyIRI));
            }
        
            catch (OWLOntologyStorageException e2) {
                throw new RuntimeException(e2.getMessage(), e2);
            }

            catch (OWLOntologyCreationException e) {
                throw new RuntimeException(e.getMessage(), e); 
        }
    }

    private static LinkedHashSet<OntologyClass> createEUNISObject(
            CSVReader reader) {
        LinkedHashSet<OntologyClass> eunis = new LinkedHashSet<OntologyClass>();
        String[] nextLine = null;
        String parent = "EUNIS";
        String className = null;
        String description = null;
        try {
            while ((nextLine = reader.readNext()) != null) {
                className = nextLine[2];
                description = nextLine[3];
                OntologyClass eunisObj = new OntologyClass();
                if (className == null) {
                    continue;
                }
                if (className.contains(" ")) {
                    className = className.replace(" ", "_");
                }
                if (description != null) {
                    eunisObj.setDescription(description);
                }
                eunisObj.setParent(parent);
                eunisObj.setName(className);
                eunis.add(eunisObj);
                System.out.println("Added: " + className);
                // eunisClasses.add(eunisObj);
            }
            // System.out.println(parameter);
            String entries = "";
            for (OntologyClass c : eunis) {
                entries += c.getName() + " ";
            }
            System.out.println(entries);
        } catch (NullPointerException f) {
            f.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return eunis;
    }

    private static HashMap<String, Integer> getColIndexes(
            List<String> headerCols) {
        HashMap<String, Integer> myHash = new HashMap<String, Integer>();
        for (int i=0; i < headerCols.size(); i++){ //String column : headerCols){
            String column = headerCols.get(i);
            if (column.startsWith("EUNIS_") && !column.startsWith("EUNIS_N")
                    || column.startsWith("NATFLO") ||
                    column.startsWith("EAGLE")){
                myHash.put(column, i);
                /*
                    OntologyClass classObj = new OntologyClass();
                    parent = nextLine[headerCols.indexOf(column)];
                    myList.add(headerCols.indexOf(column));
                    classObj.setName(nextLine[headerCols.indexOf(column)]);
                    classObj.setParent("Parameter");
                    eunisClasses.add(classObj);
                 */
            }
        }
        return myHash;
    }
} 