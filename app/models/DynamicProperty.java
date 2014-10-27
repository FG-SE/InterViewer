package models;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;
import play.data.validation.*;

/**
 * This class is part of the defined dynamic property pattern by Martin Fowler.
 * It encapsulates the properties that can be set on Interviews.
 * 
 * @author Christian
 * @author Benedikt
 * @author Florian
 */
@SuppressWarnings("serial")
@Entity
public class DynamicProperty extends Model {

	@Id
	private UUID id;

	@Constraints.Required
	@ManyToOne
	private PropertyType propertyType;

	@Constraints.Required
	private String value;

	/**
	 * This Constructor will create a default Dynamic Property for a
	 * PropertyType that has no meaningful value yet.
	 * 
	 * @param propertyType
	 */
	protected DynamicProperty(PropertyType propertyType) {
		this(propertyType, "");
	}

	/**
	 * Construct a given propertyType-value-combination.
	 * @param propertyType
	 * @param value
	 */
	protected DynamicProperty(PropertyType propertyType, String value) {
		this.propertyType = propertyType;
		this.value = value;
	}

	protected PropertyType getPropertyType() {
		return propertyType;
	}

	protected String getValue() {
		return value;
	}

	protected void setValue(String value) {
		this.value = value;
	}

	public UUID getId() {
		return id;
	}
	
}
