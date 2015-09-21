package owlAPI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;

public class OWLmap {

	static int ruleNum = 0;

	public static class owlRuleSet {
		String clsName;
		int ruleNumCounter;
		int ruleNumInSet = 0;
		HashSet<OWLClassExpression> rules = new HashSet<OWLClassExpression>();

		/* constructor */
		public owlRuleSet(String clsName, int ruleNumInSet) {
			/* starts at 0 */
			this.clsName = clsName;
			this.ruleNumCounter = OWLmap.ruleNum;
			this.ruleNumInSet = ruleNumInSet;
			OWLmap.ruleNum++;
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
	Map<String, ArrayList<owlRuleSet>> map = new java.util.HashMap<String, ArrayList<owlRuleSet>>();

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

}
