package csvToOWLRules;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.List;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.opencsv.CSVReader;

import owlAPI.Individual;
import owlAPI.OWLmap;
import owlAPI.OWLmap.owlRuleSet;
import owlAPI.OntologyClass;

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
        final FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(
                "csv", "CSV");
        /* ontology manager etc */
        OWLmap owlRulesMap = new OWLmap();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager
                .loadOntologyFromOntologyDocument(this.docIRI);
        OWLDataFactory factory = manager.getOWLDataFactory();
        final File csvFile = new File(this.directory);
        if (!extensionFilter.accept(csvFile)) {
            System.err.println("error: file doesn't end in .csv");
        }
        CSVReader reader = null;
        String[] nextLine;
        LinkedHashSet<Individual> individuals = new LinkedHashSet<Individual>();
        try {
            reader = new CSVReader(new FileReader(this.directory));
            nextLine = reader.readNext();
            List<String> headerCols = (List<String>) Arrays.asList(nextLine);
            List<Integer> myList = new ArrayList<Integer>();
            for (String column : headerCols){
                if (column.startsWith("EUNIS_") || column.startsWith("NATFLO") || 
                        column.startsWith("EAGLE")){
                    myList.add(headerCols.indexOf(column));
                }
            } 
            while ((nextLine = reader.readNext()) != null){
                HashMap<String, String> stringValues = new HashMap<String, String>();
                HashMap<String, Number> values = new HashMap<String, Number>();
                Individual individual = new Individual();
                for (Integer valIndex : myList) {
                   String objectName = nextLine[valIndex].getClass().getName();
                    if (objectName == "java.util.String"){
                        stringValues.put("has_"+ headerCols.get(valIndex), nextLine[valIndex]);
                    } else if (objectName == "java.util.Integer"){
                        values.put("has_"+ headerCols.get(valIndex), Integer.parseInt(nextLine[valIndex]));
                    }
                }
                //individual.setFID("ogc_fid");
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
