package models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static java.util.Collections.sort;
import java.util.Comparator;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.json.*;

import play.data.validation.Constraints;
import play.db.ebean.Model;

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
public class Interview extends Model {

	@Id
	private UUID id;

	@OneToMany(cascade = CascadeType.ALL)
	private List<DynamicProperty> dynamicProperties;

	/**
	 * Don't change this attribute. It is managed by EBeans!!!
	 */
	@ManyToOne(cascade = CascadeType.REFRESH)
	private Project project;

	@Constraints.Required
	private String name;

	/**
	 * One Interview has one AudioFile. The AudioFile does not need a reference
	 * to its interview. The Relationship is one-directional. CascadeType.ALL is
	 * used do gain strong ownership. Delete operations have to be directly
	 * invoked on audio.
	 */
	@OneToOne(cascade = CascadeType.ALL)
	private AudioFile audio;

	/**
	 * One Interview has many Statements. This is a bi-directional Relationship
	 * managed by EBeans. Changes to this list will cascade.
	 */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "interview")
	private List<Statement> statements;

	// *************************************************************************
	// METHODS TO ADRESS DATABASE, CREATE AND DELETE INTERVIEWS
	// *************************************************************************

	/**
	 * the database Finder-object for the interview-relation
	 */
	private static Finder<UUID, Interview> finder = new Finder<UUID, Interview>(
			UUID.class, Interview.class);

	/**
	 * Gives Interview from database, identified by project and interviewName.
	 * This combination should be unique.
	 * 
	 * @param project
	 *            the parent project.
	 * @param interviewName
	 *            of the searched interview.
	 * @return the unique interview identified by project and interviewName.
	 */
	public static Interview getInterview(Project project, String interviewName) {
		return finder.where().eq("name", interviewName).where()
				.eq("project.name", project.getName()).findUnique();
	}

	/**
	 * Gives Interview from database, identified by id.
	 * 
	 * @param id
	 *            - the id
	 * 
	 * @return the unique interview identified by uuid.
	 */
	public static Interview getInterview(UUID id) {
		return finder.where().eq("id", id).findUnique();
	}

	/**
	 * This function gives all Interviews existing in the database to the given
	 * project
	 * 
	 * @return list of existing interviews of the given project
	 */
	public static List<Interview> findAllInterviews(Project project) {
		return finder.where().eq("project", project).findList();
	}

	/**
	 * This function gives all Interviews existing in the database
	 * 
	 * @return list of existing interviews
	 */
	public static List<Interview> findAllInterviews() {
		return finder.findList();
	}

	/**
	 * This is the Interview factory method. It needs an existing project and a
	 * name that does not exist in this scope. At the end of this method the new
	 * object is written to the database.
	 * 
	 * @param project
	 *            the parent project. It has to exist in the database.
	 * @param name
	 *            of the new interview. This must not exist in the current
	 *            project
	 */
	public static Interview createInterview(Project project, String name) {
		if (Project.getProject(project.getId()) == null) {
			throw new IllegalArgumentException(
					"Interview creation failed. The parent project does not exist in the database");
		}
		if (project.checkIfInterviewNameExists(name)) {
			throw new IllegalArgumentException(
					"Interview creation failed. This Project already contains an Interview with this name");
		}
		if (name == null || name.equals("")) {
			throw new IllegalArgumentException(
					"Interview creation failed. The interview must have a name.");
		}

		Interview newInterview = new Interview(project, name);

		newInterview.save();

		return newInterview;
	}

	/**
	 * This function will remove the given interview from the database. EBeans
	 * will manage the deletion of all references automatically.
	 * 
	 * @param interview
	 *            - the interview to be deleted
	 */
	public static boolean removeInterview(Interview interview) {
		if (interview == null
				|| Interview.getInterview(interview.getId()) == null
				|| interview.getProject() == null)
			return false;
		interview.getProject().detachInterview(interview);
		try {
			interview.delete();
		} catch (RuntimeException e) {
			return false;
		}
		return true;
	}

	// *************************************************************************
	// PRIVATE CONSTRUCTORS
	// *************************************************************************

	/**
	 * The project constructor. It expects consistent data!
	 * 
	 * @param project
	 *            - the parent project
	 * @param name
	 *            - the name of the new interview
	 */
	private Interview(Project project, String name) {
		this.dynamicProperties = new ArrayList<DynamicProperty>();
		this.name = name;
		this.statements = new ArrayList<Statement>();
		this.audio = null;

		project.attachInterview(this);
		// EBeans will manage this automatically:
		// this.project = project;

	}

	@Override
	public void delete() {
		if (audio != null) {
			audio.deleteFile();
		}
		super.delete();
	}

	// *************************************************************************
	// GETTERS AND SETTERS
	// *************************************************************************

	/**
	 * Gives the id of the interview.
	 * 
	 * @return id
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * Gives the name of the interview.
	 * 
	 * @return the current name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gives the parent project.
	 * 
	 * @return the parent project
	 */
	public Project getProject() {
		return project;
	}

	/**
	 * Sets the name of the interview and saves it to the database.
	 * 
	 * @param name
	 *            - the new name
	 */
	public void setName(String name) {
		if (project.checkIfInterviewNameExists(name) && !name.equals(this.name)) {
			throw new IllegalArgumentException(
					"Interview creation failed. This Project already contains an Interview with this name");
		}
		if (name == null || name.equals("")) {
			throw new IllegalArgumentException("Interview name is empty");

		}
		this.name = name;
		save();
	}

	/**
	 * Gives the audio file.
	 * 
	 * @return the audio file
	 */
	public AudioFile getAudio() {
		return this.audio;
	}

	/**
	 * Sets the new audio of the interview and saves it to the database. This
	 * method throws an Exception if there is already an audio. There is no
	 * consistency check for the assigned audio. The upload has already to be
	 * done when this method is called.
	 * 
	 * @param audio
	 *            - the AudioFile object
	 * @throws IOException 
	 */
	public void setAudio(AudioFile audio) throws IOException {
		if (this.audio == null) {
			this.audio = audio;
			save();
		} else {
			throw new IOException("Die Audio-Datei konnte dem Interview nicht hinzugefügt werden.");
		}

	}

	/**
	 * Deletes the audiofile-Object and associated file from the server
	 */
	protected void deleteAudio() {
		audio.delete();
	}

	// *************************************************************************
	// MANAGEMENT OF STATEMENTS
	// *************************************************************************

	/**
	 * This method gives all statements.
	 * 
	 * @return all statements in this interview.
	 */
	public List<Statement> getStatements() {
		return statements;
	}
	
	/**
	 * This method gives all statements sorted by time.
	 * 
	 * @return all statements in this interview.
	 */
	public List<Statement> getStatementsSorted() {
		List<Statement> result = new ArrayList<Statement>(getStatements());
		sort(result, new Comparator<Statement>() {
			@Override
			public int compare(Statement o1, Statement o2) {
				return o1.getTime().toMillis() - o2.getTime().toMillis(); //negativ <=> first is less than second
			}
		});
		return result;
	}

	/**
	 * This method adds a statement to this interview
	 * 
	 * @param statement
	 *            the new statement
	 */
	public void addStatement(Statement statement) {
		throw new UnsupportedOperationException();
		/*
		 * statements.add(statement); save();
		 */
	}

	/**
	 * Deletes the given statement.
	 * 
	 * @param statement
	 *            that will be deleted
	 */
	public void deleteStatement(Statement statement) {
		throw new UnsupportedOperationException();
		/*
		 * statements.remove(statement);
		 * 
		 * save();
		 */
	}

	// *************************************************************************
	// MANAGEMENT OF DYNAMIC PROPERTIES
	// *************************************************************************

	/**
	 * With this method combinations of a PropertyType and a string value can be
	 * setted. This combinations are internally known as dynamic properties. The
	 * design orientes to Martin Fowlers Defined Dynamic Property-Pattern. The
	 * PropertyTypes are managed by the parent project.
	 * 
	 * @param type
	 *            - the type of the property to be set
	 * @param value
	 *            - the value of the given type
	 */
	public void setProperty(PropertyType type, String value) {
		DynamicProperty oldProperty = getDynamicPropertyIfExists(type);
		if (oldProperty != null) {
			oldProperty.setValue(value);
			oldProperty.save();
		} else {
			dynamicProperties.add(new DynamicProperty(type, value));
			save();
		}

	}

	/**
	 * This method returns the value assigned to the delivered propertyType.
	 * 
	 * @param propertyType
	 *            - the desired propertyType
	 * @return the value of the given propertyType
	 */
	public String getProperty(PropertyType propertyType) {
		DynamicProperty dynamicProperty = getDynamicPropertyIfExists(propertyType);
		if (dynamicProperty == null) {
			dynamicProperty = new DynamicProperty(propertyType, "");
			dynamicProperties.add(dynamicProperty);
			save();
		}
		return dynamicProperty.getValue();

	}

	/**
	 * This internal method handles the dynamic property objects.
	 * 
	 * @param propertyType
	 *            - the desired propertyType
	 * @return the searched DynamicProperty-Object
	 */
	private DynamicProperty getDynamicPropertyIfExists(PropertyType propertyType) {
		for (DynamicProperty dynProperty : this.dynamicProperties) {
			if (dynProperty.getPropertyType().equals(propertyType)) {
				return dynProperty;
			}
		}
		return null;
	}

	/**
	 * This method returns a JSONStringer which represents the interview.
	 * 
	 * To transfer this to a client the code can look like this: <code>
	 * 		public Result getJASON(){
	 * 			Interview interview = Interview.getInterview(UUID.fromString("UUID_STRING"));
	 * 			return ok(interview.toJSON().toString());
	 * 		}
	 * 		</code>
	 * 
	 * On the client site you can access using jquery: <code>
	 * 		$.getJSON("url.to.getJASON", function(result){
	 * 			//do something with the result, mabye you ask for the interview name:
	 * 			var interviewName = result.InterviewName
	 * 		});
	 * 		</code>
	 * 
	 * You have access to the following keys:
	 * 
	 * -InterviewId Returns the interview id, just use it to ask for an
	 * interview in the database with the static methods from the Interview
	 * class. -InterviewName Returns the interview name -AudioFile This string
	 * will be empty if now audiofile was specified -Properties This key
	 * contains an array with all properties which were specified in the related
	 * project. To access to all this properties, just iterate over this array.
	 * Use the key "PropertyType" to access to the name of the Property and use
	 * the key "Value" to access to the related values.
	 * 
	 * @return JSONStringer which represents the interview
	 */

	public JSONStringer toJSON() {
		JSONStringer json = new JSONStringer();
		try {
			json.object();
			json.key("interviewId").value(this.id.toString());
			json.key("interviewName").value(this.name);

			json.key("audio");
			if (this.audio != null) {
				json.value(this.audio.getName());
			} else {
				json.value("");
			}

			json.key("properties").array();

			for (PropertyType propType : this.project.getPropertyTypes()) {
				json.object();
				json.key("propertyType").value(propType.getName());
				json.key("value").value(getProperty(propType));
				json.endObject();
			}
			json.endArray();
			json.endObject();
		} catch (JSONException e) {
			// Give an empty json if creation fails
			return new JSONStringer();
		}
		return json;
	}
}
