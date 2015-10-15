package owlAPI;

import java.util.HashSet;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class OntologyClass {

    private String parent;
	private String name;
	private String description;
	private String descriptionDE;
	static HashSet<String> classes = new HashSet<String>();

	public boolean addClass(String clazz){
	    if (!inSet(clazz)){
	        System.out.println("Class + " + clazz + " is not in classes hash");
	        return false;
	    }
	    return OntologyClass.classes.add(clazz);
	}
	public boolean inSet(String clazz){
	    return classes.contains(clazz);
	}
	
	public void setName(String name) {
	    classes.add(name);
		this.name = name;
	}

	public void setParent(String pName) {
		this.parent = pName;
	}
	
	public String getParent() {
		return this.parent; 
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public void setDescriptionDE(String description) {
		this.descriptionDE = description;
	}
	
	public String getDescription() {
		return description;
	}

	public String getDescriptionDE() {
		return descriptionDE;
	}
	
	public String getName() {
		return this.name;
	}

	/* TODO: Get rid of magic numbers! */
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(name).append(description)
				.toHashCode();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof OntologyClass))
			return false;
		if (obj == this)
			return true;
		OntologyClass rhs = (OntologyClass) obj;
		return new EqualsBuilder().append(name, rhs.name)
				.append(description, rhs.description).isEquals();
	}
}