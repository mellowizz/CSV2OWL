package csvToOWLRules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;

import owlAPI.Individual;

public class CreateIndividualsFromCSV {

    public static LinkedHashSet<Individual> createIndividualsFromCSV(String fileName){
        LinkedHashSet<Individual> individuals = new LinkedHashSet<Individual>();
        CSVReader reader = null;
        String[] nextLine;
        try {
            reader = new CSVReader(new FileReader(fileName));
            nextLine = reader.readNext();
            List<String> headerCols = (List) Arrays.asList(nextLine);
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
                // individual.setFID("ogc_fid");
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
