package csvToOWLRules;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
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

import owlAPI.OWLmap;
import owlAPI.OWLmap.owlRuleSet;

import com.opencsv.CSVReader;

public class CSVToOWLRulesConverter {
	String directory;
	IRI docIRI;
	int numRules;

	public CSVToOWLRulesConverter(String csvDir, IRI documentIRI,
			int numberOfRules) {
		this.directory = csvDir;
		this.docIRI = documentIRI;
		this.numRules = numberOfRules;
	}

	//public defaultDict<String, List<OWLClassExpression>> CSVRulesConverter()
	public OWLmap CSVRulesConverter()
			throws OWLOntologyCreationException, NumberFormatException,
			IOException, OWLOntologyStorageException {
		final FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(
				"csv", "CSV");
		final File file = new File(this.directory);
		CSVReader reader = null;
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager
				.loadOntologyFromOntologyDocument(this.docIRI);
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLmap owlRulesMap = new OWLmap();
		//defaultDict<String, List<OWLClassExpression>> classesExpressions = new defaultDict<String, List<OWLClassExpression>>(
		//		ArrayList.class);
		
		for (final File csvFile : file.listFiles()) {
			if (extensionFilter.accept(csvFile)) {
				reader = new CSVReader(new FileReader(csvFile));
				//List<String> classNames = Arrays.asList("aquatic", "dry", "mesic", "very_wet"); 
				String fileNameNoExt = FilenameUtils.removeExtension(csvFile
						.getName());
				String[] classNames = fileNameNoExt.split("-");
				if (classNames[0].contains(" ")){
					classNames[0] = classNames[0].replace(" ", "_");
				}
				if (classNames[1].contains(" ")){
					classNames[1] = classNames[1].replace(" ", "_");
				}
				System.out.println("class: " + classNames[0] + " target: "
						+ classNames[1]);
				String[] nextLine;
				int lineNum = 1;
				OWLDatatypeRestriction newRestriction = null;
				OWLDataProperty hasParameter = null;
				//OWLDatatypeRestriction newRestrictionOpp = null;
				Set<OWLClassExpression> ruleSet = new HashSet<OWLClassExpression>();
				//Set<OWLClassExpression> ruleSetOpp = new HashSet<OWLClassExpression>();
				int ruleCounter = 0;
				/* could make MAX_EXCLUSIVE set by if/else than emit right code */
				while ((nextLine = reader.readNext()) != null
						&& lineNum <= this.numRules) {
					String parameter = nextLine[0]; /* why has_? */
					/*
					if (parameter.equals("aquatic") || parameter.equals("dry")
							|| parameter.equals("mesic")
							|| parameter.equals("very_wet")) {
						//ignore
						continue;
					}
					*/
					try {
						ruleCounter++;
						/* try to create a rule out of line */
                        parameter = "has_" + parameter;
                        String direction = nextLine[2];
                        String threshold = nextLine[3];
                        System.out.println(parameter + " direction: " + direction
                                + " threshold: " + threshold);
                        hasParameter = factory.getOWLDataProperty(IRI.create("#"
                                + parameter));
                        /* TODO: turn this into a Hashmap of newRestrictions*/
						switch (direction) {
                            case ">=":
                                newRestriction = factory
                                        .getOWLDatatypeMinInclusiveRestriction(Double
                                                .parseDouble(threshold));
                                /*newRestrictionOpp = factory
                                        .getOWLDatatypeMaxExclusiveRestriction(Double
                                                .parseDouble(threshold));*/
                                break;
                            case "<=":
                                newRestriction = factory
                                        .getOWLDatatypeMaxInclusiveRestriction(Double
                                                .parseDouble(threshold));
                                
                                /*newRestrictionOpp = factory
                                        .getOWLDatatypeMinExclusiveRestriction(Double
                                                .parseDouble(threshold)); */
                                break;

                            case "<":
                                newRestriction = factory
                                        .getOWLDatatypeMaxExclusiveRestriction(Double
                                                .parseDouble(threshold));
                                
                                /*newRestrictionOpp = factory
                                        .getOWLDatatypeMinExclusiveRestriction(Double
                                                .parseDouble(threshold)); */
                                break;
                            case ">":
                                newRestriction = factory
                                        .getOWLDatatypeMinExclusiveRestriction(Double
                                                .parseDouble(threshold));
                                
                                /*newRestrictionOpp = factory
                                        .getOWLDatatypeMaxExclusiveRestriction(Double
                                                .parseDouble(threshold));*/
                                break;
                            default:
                                System.out
                                        .println("DID NOT FIND > or < in CSV file: "
                                                + csvFile.getName());
                                break;
                        }
						if (newRestriction == null || hasParameter == null) {
							System.out.println("Something went wrong!!");
							continue;
						}
						// add new data restriction!
						OWLClassExpression newWetnessRestriction = factory
								.getOWLDataSomeValuesFrom(hasParameter,
										newRestriction);
						//OWLClassExpression newWetnessRestrictionOpp = factory
						//		.getOWLDataSomeValuesFrom(hasParameter,
						//				newRestrictionOpp);
						ruleSet.add(newWetnessRestriction);
						//ruleSetOpp.add(newWetnessRestrictionOpp);
						lineNum++;
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
				}
				/*done with CSV, add rules */
                if (owlRulesMap.get(classNames[0]) == null){
                    OWLmap.owlRuleSet rule = new OWLmap.owlRuleSet (classNames[0], ruleCounter);
                    //OWLmap.owlRuleSet rule_target = new OWLmap.owlRuleSet (classNames[1], ruleCounter);
                    rule.addAll(ruleSet);
                    //rule_target.addAll(ruleSetOpp);
                    //ruleSetOpp.clear();
                    ArrayList <owlRuleSet> newRules = new ArrayList<owlRuleSet>();
                    newRules.add(rule);
                    owlRulesMap.put(classNames[0], newRules);
                    //continue;
                } else{
                	ArrayList<owlRuleSet> existingRules = owlRulesMap.pop(classNames[0]);
                	owlRuleSet the_rules = existingRules.remove(0);
                	ruleSet.addAll(the_rules.getRuleList(classNames[0]));
                    OWLmap.owlRuleSet rule = new OWLmap.owlRuleSet (classNames[0], ruleCounter);
                    ArrayList <owlRuleSet> newRules = new ArrayList<owlRuleSet>();
                    rule.addAll(ruleSet);
                    newRules.add(rule);
                	owlRulesMap.put(classNames[0], newRules);
                }
                    ruleSet.clear();
			}
		}
		manager.saveOntology(ontology);
		reader.close();
		//System.out.println("There are: " + classesExpressions.size()
		//		+ " keys: " + classesExpressions.keySet().size());
		return owlRulesMap; //classesExpressions;
	}
}