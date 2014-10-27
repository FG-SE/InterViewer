package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;

import org.json.*;

/**
 * 
 * @author Christian
 * @author Sven
 * @author Thiemo
 * @author Benedikt
 * @author Florian
 */
@SuppressWarnings("serial")
@Entity
public class Project extends Model {

	@Id
	private UUID id;

	@Constraints.Required
	private String name;

	/**
	 * One Project has many Interviews. This is a bi-directional Relationship
	 * managed by EBeans. Changes to this list will cascade.
	 */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "project")
	private List<Interview> interviews;

	/**
	 * One Project has many PropertyTypes. This is a bi-directional Relationship
	 * managed by EBeans. Changes to this list will cascade.
	 */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "project")
	private List<PropertyType> propertyTypes;

	/**
	 * The Relation between Users and Projects is managed on both sides using
	 * EBeans and the save cascade mechanism.
	 */
	@ManyToMany(cascade = CascadeType.ALL)
	private List<User> users;

	// *************************************************************************
	// METHODS TO ADRESS DATABASE, CREATE AND DELETE PROJECTS
	// *************************************************************************

	/**
	 * the database Finder-object for the project-relation
	 */
	public static Finder<UUID, Project> finder = new Finder<UUID, Project>(
			UUID.class, Project.class);

	/**
	 * Gives a Project from database, identified by projectName.
	 * 
	 * @param projectName
	 *            - the name
	 * 
	 * @return the unique project identified by name.
	 */
	public static Project getProject(String projectName) {
		return finder.where().eq("name", projectName).findUnique();
	}

	/**
	 * Gives a Project from database, identified by projectId.
	 * 
	 * @param projectId
	 *            - the id
	 * 
	 * @return the unique project identified by name.
	 */
	public static Project getProject(UUID projectId) {
		return finder.where().eq("Id", projectId).findUnique();
	}

	/**
	 * This function gives all Projects existing in the database
	 * 
	 * @return list of existing projects
	 */
	public static List<Project> findAllProjects() {
		return finder.all();
	}

	/**
	 * This is the Project factory method. It needs a name that does not exist
	 * in this scope. At the end of this method the new object is written to the
	 * database.
	 * 
	 * @param name
	 *            of the new project. It must not exist!
	 */
	public static Project createProject(String name) {
		if (Project.getProject(name) != null) {
			throw new IllegalArgumentException(
					"Project creation failed. A project of this "
							+ "name already exists in the database");

		}

		if (name == null || name.equals("")) {
			throw new IllegalArgumentException("Project name is empty");

		}

		Project result = new Project(name);
		result.save();
		return result;
	}

	/**
	 * Call this function to delete a Project.
	 * 
	 * @param name
	 *            - the projects name
	 * @return true - success / false - fail
	 */
	public static boolean removeProject(String name) {
		Project project = Project.getProject(name);
		if (project == null)
			return false;
		try {
			project.delete();
		} catch (RuntimeException e) {
			return false;
		}
		return true;
	}

	/**
	 * Call this function to delete a Project by ID.
	 * 
	 * @param name
	 *            - the project ID
	 * @return true - success / false - fail
	 */
	public static boolean removeProject(UUID Id) {
		Project project = Project.getProject(Id);
		if (project == null)
			return false;
		project.delete();
		return true;
	}

	// *************************************************************************
	// PRIVATE CONSTRUCTORS
	// *************************************************************************

	/**
	 * The Project constructor. It expects a project name that is not existent
	 * in the database!
	 * 
	 * @param name
	 *            - the name of the new project
	 */
	private Project(String name) {
		this.name = name;
		this.propertyTypes = new Vector<PropertyType>();
		this.interviews = new Vector<Interview>();
		this.users = new Vector<User>();
	}

	@Override
	public void delete() {
		// Objects need to be load from database to manage deletion of
		// audiofiles.
		for (Interview i : getInterviews()) {
			AudioFile audio = i.getAudio();
			if(audio != null){
				audio.deleteFile();
			}
				
		}
		super.delete();
	}

	// *************************************************************************
	// GETTERS AND SETTERS
	// *************************************************************************

	/**
	 * Gives the id of the project.
	 * 
	 * @return id
	 */
	public UUID getId() {
		return this.id;
	}

	/**
	 * Gives the name of the project.
	 * 
	 * @return the current name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the name of the project.
	 * 
	 * @return the current name
	 */
	public void setName(String newName) {
		if (newName == null || newName.equals("")) {
			throw new IllegalArgumentException("Project name is empty");

		}
		
		if (Project.getProject(newName) != null && !newName.equals(name)) {
			throw new IllegalArgumentException(
					"Project creation failed. A project of this "
							+ "name already exists in the database");

		}


		this.name = newName;
		save();
	}

	/**
	 * Gives all users of the project
	 * 
	 * @return all users
	 */
	public List<User> getUsers() {
		return this.users;
	}

	// *************************************************************************
	// USER MANAGEMENT
	// *************************************************************************

	/**
	 * Checks whether a given user has the right to edit this project.
	 * 
	 * @param user
	 *            - the user
	 * @return true - given User has right to edit project / false - User has no
	 *         right
	 */
	public boolean findUser(User user) {
		return users.contains(user);
	}

	/**
	 * Adds a user to the project. After that he has the authorization to change
	 * the project and all of its interviews
	 * 
	 * @param user
	 *            - the user
	 */
	public void addUser(User user) {
		if (!this.users.contains(user)) {
			this.users.add(user);
			save();
		}
	}

	/**
	 * Removes a user from the project. After that he can not edit the project
	 * or its interview
	 * 
	 * @param user
	 *            - the user
	 */
	public void removeUser(User user) {
		if (this.users.contains(user)) {
			this.users.remove(user);
			save();
		}
	}

	// *************************************************************************
	// MANAGEMENT OF PROPERTY TYPES
	// *************************************************************************

	/**
	 * This method (creates and) gives PropertyType-Objects. The new
	 * propertytype will be registered in the project. The implementation is
	 * alike the singleton-pattern.
	 * 
	 * The method will save the updated list of property types to the database.
	 * 
	 * @param name
	 *            - the name of the property type, that will be created, if it
	 *            does not already exist
	 * @return the property type that was created or found in the dynamic
	 *         properties list
	 */
	public PropertyType getPropertyTypeByName(String name) {

		if (name == null || name.equals("")) {
			throw new IllegalArgumentException("Empty name is not allowed");
		}

		for (PropertyType type : this.propertyTypes) {
			if (type.getName().equals(name)) {
				return type;
			}
		}

		PropertyType newType = new PropertyType(name);
		this.propertyTypes.add(newType);
		save();
		return newType;
	}

	/**
	 * This method gives all property types defined in the project.
	 * 
	 * @return list of all property types
	 */
	public List<PropertyType> getPropertyTypes() {
		return propertyTypes;
	}
	/**
	 * This method gives the names of all property types.
	 * @return
	 */
	public List<String> getPropertyTypesAsStrings() {
		List<String> properties = new ArrayList<String>();
		for (PropertyType property : getPropertyTypes()) {
			properties.add(property.getName());
		}
		return properties;
	}

	public void removePropertyType(PropertyType type) {
		if (this.propertyTypes.contains(type)) {
			this.propertyTypes.remove(type);
			type.delete();
			save();
		}
	}

	// *************************************************************************
	// MANAGEMENT OF INTERVIEWS
	// *************************************************************************

	/**
	 * This method checks whether the given name exists as a interview name in
	 * the current project or not.
	 * 
	 * @param name
	 *            - the name that needs to be checked
	 * @return true, if interview with this name exists.
	 */
	public boolean checkIfInterviewNameExists(String name) {
		for (Interview interview : this.interviews) {
			if (interview.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Attaches an interview to the project. Does nothing if it is already
	 * assigned to this project. This method is protected to ensure that there
	 * is no package-external changing possible. It is not checked whether the
	 * interview is already assigned to another project. Use the
	 * Interview.createInterview method instead, its save against errors in
	 * reasoning. This method will be called by the Interview-constructor.
	 * 
	 * @param interview
	 *            to be assigned
	 */
	protected void attachInterview(Interview interview) {
		if (!this.interviews.contains(interview)) {
			this.interviews.add(interview);
		}
		save();
	}

	/**
	 * Detaches an interview from a project. This method is protected to ensure
	 * that there is no package-external changing possible. It is invoked by
	 * Interview.removeInterview.
	 * 
	 * @param interview
	 *            to be deleted
	 */
	protected void detachInterview(Interview interview) {
		interviews.remove(interview);
		save();
	}

	/**
	 * Gives a list of all existing interviews in this project.
	 * 
	 * @return list of interviews
	 */
	public List<Interview> getInterviews() {
		return interviews;
	}

	// *************************************************************************
	// JSON
	// *************************************************************************

	/**
	 * Provides functionality to convert projects to JSON.
	 * 
	 * @return JSONStringer-Object
	 * @throws JSONException
	 */
	public JSONStringer toJSON() throws JSONException {
		JSONStringer json = new JSONStringer();

		json.object().key("Id").value(id).key("Name").value(name);
		String link = controllers.routes.Projects.exportProject(
				this.id.toString()).toString()
				+ "";
		json.key("Link").value(link);

		if (users.size() > 0) {
			json.key("Users").array();

			for (User u : User.findAll()) {
				json.object().key("Alias").value(u.getAlias());
				json.key("ProjectMember").value(
						(users.contains(u)) ? true : false);
				json.key("Id").value(u.getId()).endObject();

			}

			json.endArray();
		}

		if (this.propertyTypes.size() > 0) {
			json.key("Properties").array();

			for (PropertyType p : this.propertyTypes) {
				json.object().key("Name").value(p.getName()).endObject();
			}

			json.endArray();
		}

		json.endObject();
		return json;
	}
}
