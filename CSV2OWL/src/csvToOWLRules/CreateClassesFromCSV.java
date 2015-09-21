package csvToOWLRules;

import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;

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
            parent = nextLine[colIndex];
            while ((nextLine = reader.readNext()) != null){
                String className = nextLine[colIndex];
                String description = nextLine[3];
                if (className == null) {
                    continue;
                }
                OntologyClass eunisObj = new OntologyClass();
                //System.out.println(parameter);
                if (className.contains(" ")) {
                    className = className.replace(" ", "_");
                } 
                if (eunisClasses.contains(eunisObj.getName()) == false) {
                    eunisObj.setParent(parent);
                    eunisObj.setName(className);
                    eunisObj.setDescription(description);
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