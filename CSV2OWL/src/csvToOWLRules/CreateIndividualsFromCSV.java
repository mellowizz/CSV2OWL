package csvToOWLRules;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import com.opencsv.CSVReader;

import owlAPI.Individual;

public class CreateIndividualsFromCSV {

    public static LinkedHashSet<Individual> createIndividualsFromCSV(String fileName, HashMap<String, Integer> namedIndex){
        LinkedHashSet<Individual> individuals = new LinkedHashSet<Individual>();
        CSVReader reader = null;
        String[] nextLine;
        String paramName = null;
        //String eunisClass = null;
        String paramValue = null;
        try {
            reader = new CSVReader(new FileReader(fileName));
            nextLine = reader.readNext();
            while ((nextLine = reader.readNext()) != null){
                HashMap<String, String> stringValues = new HashMap<String, String>();
                HashMap<String, Number> values = new HashMap<String, Number>();
                Individual individual = new Individual();
                for (Entry<String, Integer> entry:  namedIndex.entrySet()) {
                  paramName = entry.getKey();
                  paramValue = nextLine[entry.getValue()];
                   String objectName = nextLine[entry.getValue()].getClass().getName();
                    if (objectName == "java.util.String"){
                        stringValues.put("has_"+ paramName, paramValue);
                    } else if (objectName == "java.util.Integer"){
                        values.put("has_"+ paramName, Integer.parseInt(paramValue));
                    }
                }
                individual.setName("has_" + paramName);
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
        return individuals;
    }
}
