package csvToOWLRules;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.opencsv.CSVReader;

import owlAPI.OWLmap;
import owlAPI.OntologyClass;
import owlAPI.OntologyCreator;
import owlAPI.OntologyWriter;

public class CreateClassesFromCSV {

    public static void createClassesfromCSV(String fileName,
            int colIndex, File owlFile) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException {
        /* Read from CSV */
        CSVReader reader = null;
        try{
            OntologyCreator ontCreate = new OntologyCreator();
            String ontologyIRI = "http://www.user.tu-berlin.de/niklasmoran/EUNIS/"
                    + owlFile.getName().trim();
            ontCreate.createOntology(ontologyIRI, "version_1_0", owlFile);
            File csvFile = new File(fileName);
            LinkedHashSet<OntologyClass> eunisClasses = new LinkedHashSet<OntologyClass>();
            /* open file */
            reader = new CSVReader(new FileReader(fileName));
            eunisClasses = createEUNISObject(fileName);
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
        } catch (OWLOntologyStorageException e2) {
            throw new RuntimeException(e2.getMessage(), e2);
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e.getMessage(), e); 
        } finally {
            try{
                if (reader != null){
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }

    private static LinkedHashSet<OntologyClass> createEUNISObject(
            String fileName) {
        LinkedHashSet<OntologyClass> eunis = new LinkedHashSet<OntologyClass>();
        CSVReader reader = null;
        String[] nextLine = null;
        String parent = "EUNIS";
        String className = null;
        String description = null;
        String descriptionDE = null;
        try {
            reader = new CSVReader(new FileReader(fileName));
            while ((nextLine = reader.readNext()) != null) {
                className = nextLine[2];
                description = nextLine[3];
                descriptionDE = nextLine[4];
                OntologyClass eunisObj = new OntologyClass();
                if (className == null) {
                    continue;
                }
                if (className.contains(" ")) {
                    className = className.replace(" ", "_");
                }
                eunisObj.setDescription(description);
                eunisObj.setDescriptionDE(descriptionDE);
                eunisObj.setParent(parent);
                eunisObj.setName(className);
                eunis.add(eunisObj);
                System.out.println("Added: " + className);
            }
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
        try {
            if (reader != null){
                reader.close();
            }
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