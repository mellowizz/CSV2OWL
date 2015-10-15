package owlAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class OWLmap {

	static int ruleNum = 0;
	public static LinkedHashSet<OntologyClass> eunisClasses = new LinkedHashSet<OntologyClass>();

	public static void addClass(OntologyClass clazz){
	    OWLmap.eunisClasses.add(clazz);
	}
	
	public static OntologyClass getClass(String clazz){
	    /* not efficient! */
	    for (OntologyClass c: OWLmap.eunisClasses){
	        if (c.getName() == clazz){
	            return c;
	        }
	    }
	    return null;
	}
	
	public static class owlRuleSet {
		String clsName;
		List<String> parent;
		String description;
		String descriptionDE;
		int ruleNumCounter;
		int ruleNumInSet = 0;
		HashSet<OWLClassExpression> rules = new HashSet<OWLClassExpression>();

		/* constructor */
		public owlRuleSet(String clsName) {//, int ruleNumInSet) {
			/* starts at 0 */
			this.clsName = clsName;
			//this.ruleNumCounter = OWLmap.ruleNum;
			//this.ruleNumInSet = ruleNumInSet;
			OWLmap.ruleNum++;
		}
		
		public void addDescription(String english){
            this.addDescription(english, "");
        }
		
		public void addDescription(String english, String deutsch){
		    this.description = english;
		    this.description = deutsch; 
		}
		
        public String getDescription() {
            return this.description;
        }
        public String getDescriptionDE() {
            return this.descriptionDE;
        }
        
		public void addParent(String parent){
		    this.parent.add(parent);
		}
		public List<String> getParent(){
		    return this.parent;
		}

		public void addRule(OWLClassExpression rule) {
			this.rules.add(rule);
		}

		public void addAll(Set<OWLClassExpression> ruleSet) {
			for (OWLClassExpression r : ruleSet) {
				this.rules.add(r);
			}
		}

		public void clear() {
			this.rules.clear();
		}

		public Set<OWLClassExpression> getRuleList(String clsName) {
			return this.rules;
		}

		public String getName() {
			return this.clsName;
		}

		public int getRuleNum() {
			return this.ruleNumCounter;
		}


	}

	/* owlRules methods have access to this! */
	Map<String, ArrayList<owlRuleSet>> map = new HashMap<String, ArrayList<owlRuleSet>>();

	public ArrayList<owlRuleSet> put(String key, ArrayList<owlRuleSet> value) {
		return map.put(key, value);
	}

	public ArrayList<owlRuleSet> get(String key) {
		return map.get(key);
	}
	public ArrayList<owlRuleSet> pop(String key) {
		return map.remove(key); 
	}
	
	public ArrayList<owlRuleSet> replace(String key, 
	        ArrayList<owlRuleSet> value) {
		return map.replace(key, value); 
	}
	private static OWLClass createOntoClass(OWLOntologyManager manager,
            OWLOntology ontology, IRI ontologyIRI, OWLDataFactory dataFactory,
            List<String> parents, String clazz, String description,
            String descriptionDE) {
        OWLClass topParentCls = null;
        OWLClass parentCls = null;
        OWLClass thing = dataFactory.getOWLThing();
        OWLClass cls = null;
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        OWLClass ancestor = dataFactory
                .getOWLClass(IRI.create(ontologyIRI + "#" + parents.remove(0)));
        /* loop over children */
        cls = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#" + clazz));
        if (parents.isEmpty()) {
            axioms.add(dataFactory.getOWLSubClassOfAxiom(cls, ancestor));
        } else if (parents.size() == 1) {
            parentCls = dataFactory.getOWLClass(
                    IRI.create(ontologyIRI + "#" + parents.remove(0)));
            axioms.add(dataFactory.getOWLSubClassOfAxiom(cls, parentCls));
            axioms.add(dataFactory.getOWLSubClassOfAxiom(parentCls, ancestor));

        } else {
            // System.out.println("more than two parents!");
            topParentCls = dataFactory.getOWLClass(
                    IRI.create(ontologyIRI + "#" + parents.remove(0)));
            parentCls = dataFactory.getOWLClass(
                    IRI.create(ontologyIRI + "#" + parents.remove(0)));
            axioms.add(
                    dataFactory.getOWLSubClassOfAxiom(parentCls, topParentCls));
            axioms.add(
                    dataFactory.getOWLSubClassOfAxiom(topParentCls, ancestor));
        }
        axioms.add(dataFactory.getOWLSubClassOfAxiom(ancestor, thing));
        manager.addAxioms(ontology, axioms);
        if (description != null) {
            OWLAnnotation commentAnno = dataFactory.getOWLAnnotation(
                    dataFactory.getRDFSComment(),
                    dataFactory.getOWLLiteral(description, "en"));
            OWLAxiom ax = dataFactory
                    .getOWLAnnotationAssertionAxiom(cls.getIRI(), commentAnno);
            manager.applyChange(new AddAxiom(ontology, ax));
            OWLAnnotation commentDE = dataFactory.getOWLAnnotation(
                    dataFactory.getRDFSComment(),
                    dataFactory.getOWLLiteral(descriptionDE, "de"));
            OWLAxiom axDE = dataFactory
                    .getOWLAnnotationAssertionAxiom(cls.getIRI(), commentDE);
            manager.applyChange(new AddAxiom(ontology, axDE));
        }
        return cls;
    }	

}
