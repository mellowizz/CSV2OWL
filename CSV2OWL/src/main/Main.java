package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.opencsv.CSVReader;

import owlAPI.OntologyCreator;

public class Main {
    private static LinkedHashMap<String, Integer> getColIndexes(
            String fileName) {
        CSVReader reader = null;
        List<String> headerCols = null;
        LinkedHashMap<String, Integer> myHash = 
                new LinkedHashMap<String, Integer>();
        try {
            reader = new CSVReader(new FileReader(fileName));
            headerCols = Arrays.asList(reader.readNext());
            for (int i = 0; i < headerCols.size(); i++) {
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
    /**
     * Print usage information to provided OutputStream.
     * 
     * @param applicationName Name of application to list in usage.
     * @param options Command-line options to be part of usage.
     * @param out OutputStream to which to write the usage information.
     */
    public static void printUsage(
       final String applicationName,
       final Options options,
       final OutputStream out)
    {
       final PrintWriter writer = new PrintWriter(out);
       final HelpFormatter usageFormatter = new HelpFormatter();
       usageFormatter.printUsage(writer, 80, applicationName, options);
       writer.close();
    }

    /**
     * Write "help" to the provided OutputStream.
     */
    public static void printHelp(
       final Options options,
       final int printedRowWidth,
       final String header,
       final String footer,
       final int spacesBeforeOption,
       final int spacesBeforeOptionDescription,
       final boolean displayUsage,
       final OutputStream out)
    {
       final String commandLineSyntax = "java -cp ApacheCommonsCLI.jar";
       final PrintWriter writer = new PrintWriter(out);
       final HelpFormatter helpFormatter = new HelpFormatter();
       helpFormatter.printHelp(
          writer,
          printedRowWidth,
          commandLineSyntax,
          header,
          options,
          spacesBeforeOption,
          spacesBeforeOptionDescription,
          footer,
          displayUsage);
       writer.close();
    }
    
    public static Options constructCLIOptions(){
        Options myOptions = new Options ();
        myOptions.addOption("h", false, "Prints the help");
        Option input = OptionBuilder.withArgName("input")
                                    .hasArg()
                                    .withDescription("use given path as input")
                                    .create("inputpath");
        Option output = OptionBuilder.withArgName("output")
                                    .hasArg()
                                    .withDescription("use given path to save OWL file")
                                    .create("output");
        Option property  = OptionBuilder.withArgName( "property=value" )
                .hasArgs(2)
                .withValueSeparator()
                .withDescription( "use value for given property" )
                .create( "D" );
        myOptions.addOption(input);
        myOptions.addOption(output);
        myOptions.addOption(property);
        return myOptions;
    }
    
    public static void useApacheParser(final String[] commandLineArguments)
    {
       final CommandLineParser parser = new DefaultParser();
       final Options myOpts = constructCLIOptions();
       CommandLine commandLine;
       try
       {
          commandLine = parser.parse(myOpts, commandLineArguments);
          if ( commandLine.hasOption("print") )
          {
              printHelp(
                      myOpts, 80, "help", "End of help",
                         3, 5, true, System.out);
          }
       }
       catch (ParseException parseException)  // checked exception
       {
          System.err.println(
               "Encountered exception while parsing using PosixParser:\n"
             + parseException.getMessage() );
       }
    }

    public static void main(String[] args) throws IOException,
                                                  InterruptedException, ParseException{
       
        CommandLineParser parser = new DefaultParser();
        String baseOWLFile = ""; /* make some default? */
        useApacheParser(args);
        //CommandLine cmd = parser.parse( options, args );
        //if (cmd.hasOption("f")){
            // process option
        //    baseOWLFile = cmd.getOptionValue("f"); 
        //}
        File file = new File(".");
        String gDocFileName = args[0];
        String myOutFile = args[1];
        String gDocLocation = file.getCanonicalPath()
                + "/src/get_google_doc/get_google_doc.py";
        String OS = (System.getProperty("os.name")).toUpperCase();
        String pythonLoc = null;
        if (OS.contains("WIN")) {
            pythonLoc = System.getenv("PYTHONPATH") + "/python.exe"; 

        }
        // Otherwise, we assume Linux or Mac
        else {
            pythonLoc = "python2";
        }
        System.out.println("Fetching CSV from Google spreadsheet");
        Process process = new ProcessBuilder(pythonLoc, gDocLocation,
                gDocFileName).start();
        int exitCode = process.waitFor();
        String message = "";
        if (exitCode != 0){
           message = "Failed to get google doc file!";
        } else{
           message = "CSV file written to: " + gDocFileName; 
        }
        System.out.println(message);
        File owlFile = new File(myOutFile);
        CSVReader reader = null;
        try {
            /* Read from CSV */
            LinkedHashMap<String, Integer> nameIndex = null;
            String iriString = "http://www.user.tu-berlin.de/niklasmoran/"
                    + owlFile.getName().trim();
            /* open file */
            reader = new CSVReader(new FileReader(gDocFileName));
            nameIndex = getColIndexes(gDocFileName);
            /* create ontology */
            OntologyCreator ontCreate = new OntologyCreator();
            ontCreate.createOntology(iriString, "version_1_0", owlFile);
            ontCreate.createOntologyObject(nameIndex, gDocFileName);
            System.out.println("OWL file written: " + myOutFile);
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