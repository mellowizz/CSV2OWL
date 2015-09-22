package csvToOWLRules;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.opencsv.CSVReader;

import owlAPI.Individual;
import owlAPI.OWLmap;

public class CSVToOWLRules {
    String directory;
    IRI docIRI;
    int numRules;

    public CSVToOWLRules(String csvDir, IRI documentIRI){
        this.directory = csvDir;
        this.docIRI = documentIRI;
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
        String parent = null;
        OWLDataProperty hasParameter = null;
        try {
            reader = new CSVReader(new FileReader(this.directory));
            nextLine = reader.readNext();
            HashMap<String, Integer> colIndexes = new HashMap<String, Integer>();
            List<String> headerCols = (List<String>) Arrays.asList(nextLine);
            for (String column : headerCols){
                if (column.startsWith("EUNIS_") && !column.startsWith("EUNIS_N")
                        || column.startsWith("NATFLO") ||
                        column.startsWith("EAGLE")){
                    colIndexes.put(column, headerCols.indexOf(column));
                }
            }
            /* grab values  */
            while ((nextLine = reader.readNext()) != null){
                HashMap<String, String> stringValues = new HashMap<String, String>();
                HashMap<String, Number> values = new HashMap<String, Number>();
                Individual individual = new Individual();
                String individualName = null;
                String objectType = null;
                for (Entry<String, Integer> entry : colIndexes.entrySet()){
                   objectType = nextLine[entry.getValue()].getClass().getName();
                   individualName = entry.getKey();
                   // individual name?! 
                   String parameter = "has_"+ individualName;
                   factory.getOWLObjectProperty(IRI.create("#" + parameter));
                   // parameter = factory.getOWLDataProperty(IRI.create("#"
                   //        + parameter));
                    if (objectType == "java.util.String"){
                        stringValues.put("has_"+ individualName, nextLine[entry.getValue()]);
                    } else if (objectType== "java.util.Integer"){
                        values.put("has_"+ individualName, Integer.parseInt(nextLine[entry.getValue()]));
                    }
                }
                
                individual.setName(individualName);
                individual.setValues(values);
                individual.setValueString(stringValues);
                // add to individuals
                individuals.add(individual);
                // System.out.println(individual.getDataPropertyNames() + " : "
                // + individual.getValues());
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        manager.saveOntology(ontology);
        reader.close();
        return owlRulesMap;
    }
}
