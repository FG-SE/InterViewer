package models;

import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.data.validation.Constraints;
import play.db.ebean.Model;

/**
 * This Class is part of the defined dynamic property pattern by Martin Fowler.
 * It must not be instanced directly but will be managed by the Project-class.
 * PropertyTypes are saved by its Projects using the cascade mechanism.
 * 
 * @author Benedikt,
 * @author Thiemo
 * @author Christian
 */
@SuppressWarnings("serial")
@Entity
public class PropertyType extends Model {

	@Id
	private UUID id;
	
	@OneToMany(cascade = CascadeType.REMOVE, mappedBy = "propertyType")
	private List<DynamicProperty> properties;

	/**
	 * The name of this Property type. It has to be project-wide unique
	 */
	@Constraints.Required
	private String name;

	/**
	 * Reference to the parent project. Don't change this attribute. It is
	 * managed by EBeans!!!
	 */
	@ManyToOne
	private Project project;

	/**
	 * PropertyTypes should only be created by Project's
	 * getPropertyTypeByName(). Otherwise the project doesn't know all
	 * PropertyTypes used by it's Interviews.
	 * 
	 * @param name
	 *            - a project-wide unique name
	 */
	protected PropertyType(String name) {
		this.name = name;
	}

	/**
	 * Gives the name.
	 * 
	 * @return name of the PropertyType
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Gives the project.
	 * 
	 * @return
	 */
	public Project getProject() {
		return project;
	}

	@Override
	public String toString() {
		return "Propertytype:" + getName() + " Project: " + getProject();
	}

	/**
	 * Gives the identifier.
	 * 
	 * @return id of the PropertyType
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * Will ignore the id.
	 * 
	 * @see Object
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof PropertyType)) {
			return false;
		}
		PropertyType type = (PropertyType) object;
		return type.getName().equals(this.name);
	}
}
