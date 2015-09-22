package csvToOWLRules;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import com.opencsv.CSVReader;

import owlAPI.OntologyClass;

public class CreateClassesFromCSV {

    public static LinkedHashSet<OntologyClass> createClassesfromCSV(String fileName,
            int colIndex) throws IOException {
        /* Read from CSV */
        LinkedHashSet<OntologyClass> eunisClasses = new LinkedHashSet<OntologyClass>();
        CSVReader reader = null;
        String[] nextLine;
        String parent = null;
        try {
            reader = new CSVReader(new FileReader(fileName));
            nextLine = reader.readNext();
            List<String> headerCols = (List<String>) Arrays.asList(nextLine);
            List<Integer> myList = new ArrayList<Integer>();
            for (String column : headerCols){
                if (column.startsWith("EUNIS_") && !column.startsWith("EUNIS_N")
                        || column.startsWith("NATFLO") ||
                        column.startsWith("EAGLE")){
                    OntologyClass classObj = new OntologyClass();
                    parent = nextLine[headerCols.indexOf(column)];
                    myList.add(headerCols.indexOf(column));
                    classObj.setName(nextLine[headerCols.indexOf(column)]);
                    classObj.setParent("Parameter");
                    eunisClasses.add(classObj);
                }
            } 
            parent = nextLine[colIndex];
            while ((nextLine = reader.readNext()) != null){
                String className = null;
                String description = null;
                className = nextLine[colIndex];
                description = nextLine[3];
                if (className == null) {
                    continue;
                }
                OntologyClass eunisObj = new OntologyClass();
                //System.out.println(parameter);
                if (className.contains(" ")) {
                    className = className.replace(" ", "_");
                } 
                /* its a linked hashset?! */
                if (eunisClasses.contains(eunisObj.getName()) == false) {
                    eunisObj.setParent(parent);
                    eunisObj.setName(className);
                    if (description != null){
                        eunisObj.setDescription(description);
                    }   
                    eunisClasses.add(eunisObj);
                }
            }
            String entries = "";
            for (OntologyClass c : eunisClasses) {
                entries += c.getName() + " ";
            }
            System.out.println(entries);
        } catch (NullPointerException f) {
            f.printStackTrace();
        }
        return eunisClasses;
    }
} 