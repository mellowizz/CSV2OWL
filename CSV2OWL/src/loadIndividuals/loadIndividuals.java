package loadIndivduals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.opencsv.CSVReader;

import owlAPI.OntologyCreator;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import test.testReasoner;
import databaseToOWL.DBToOWLIndividualConverter;
import rlpUtils.RLPUtils;

public class LoadIndividuals{

    public static void loadIndividuals(String[] args) throws IOException, InterruptedException {
        File owlFile = new File("/home/niklasmoran/ownCloud/grassland.owl");
        String OS = (System.getProperty("os.name")).toUpperCase();
        try {
            /* Read from CSV */
            /* create ontology */
            OntologyCreator ontCreate = new OntologyCreator();
            ontCreate.loadOntology(owlFile);
            //String parameter = "natflo_wetness";
            String tableName = "test";
            String colName = parameter;
            //AddIndividuals 
            DBToOWLIndividualConverter test = new DBToOWLIndividualConverter("jdbc:postgresql://localhost:5432/rlp_spatial?user=postgres&password=BobtheBuilder",
                    tableName, parameter, rule, colName, numRules, algorithm);
            File owl = new File("C:/Users/Moran/tubCloud/grassland.owl");
            File owlFile = test.convertDB(owl); //tableName, rule, "dt", numRules, parameter); // , fields);
            
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (OWLOntologyStorageException e2) {
            throw new RuntimeException(e2.getMessage(), e2);
        } catch (NumberFormatException e) {
            e.printStackTrace();
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
}
