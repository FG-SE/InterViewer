package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.json.JSONException;
import org.json.JSONStringer;

import play.db.ebean.Model;
import play.data.validation.*;

/**
 * 
 * @author Thiemo
 * @author Benedikt
 */
@SuppressWarnings("serial")
@Entity
public class Statement extends Model {
	@Id
	private UUID id;

	private String description;

	/**
	 * One Statement has one Time. The Time-Object does not need a reference to
	 * its statement. The Relationship is one-directional. The statement has
	 * strong ownership.
	 */
	@OneToOne(cascade = CascadeType.ALL)
	private Time time;

	/**
	 * One Code occurs in many Statements. One Statement has many Codes. This is
	 * a bi-directional Relationship managed on both sides using EBeams.
	 */
	@ManyToMany(mappedBy = "occurrence", cascade = CascadeType.ALL)
	private List<Code> codes;

	@Constraints.Required
	@ManyToOne(cascade = CascadeType.REFRESH)
	private Interview interview;

	// *************************************************************************
	// METHODS TO ADRESS DATABASE, CREATE AND DELETE INTERVIEWS
	// *************************************************************************

	/**
	 * the database Finder-object for the interview-relation
	 */
	private static Finder<UUID, Statement> finder = new Finder<UUID, Statement>(
			UUID.class, Statement.class);

	/**
	 * Gives all statements from database that match the given project and code.
	 * 
	 * @param project
	 *            the parent project of all interviews.
	 * @param code
	 *            the code
	 * @return a list of interviews.
	 */
	public static List<Statement> findAllStatements(Project project, Code code) {
		return finder.fetch("interview").fetch("codes").where()
				.eq("interview.project", project).eq("codes.id", code.getId())
				.findList();
	}

	/**
	 * Gives all statements from database that have the given parent interview
	 * and lie between left and right threshold (both inclusive).
	 * 
	 * @param interview
	 *            the parent interview.
	 * @param leftThreshold
	 * @param rightThreshold
	 * @return a list of interviews.
	 */
	public static List<Statement> findAllStatements(Interview interview,
			Time leftThreshold, Time rightThreshold) {
		List<Statement> all = finder.where().eq("interview", interview)
				.findList();
		List<Statement> result = new ArrayList<Statement>();
		for (Statement s : all) {
			if (s.getTime().toMillis() >= leftThreshold.toMillis()
					&& s.getTime().toMillis() <= rightThreshold.toMillis()) {
				result.add(s);
			}
		}
		return result;

	}

	/**
	 * Gives the Statement identified by id.
	 * 
	 * @param id
	 * @return the unique statement
	 */
	public static Statement getStatement(UUID id) {
		return finder.where().eq("id", id).findUnique();
	}

	public static Statement createStatement(Interview interview, Time time,
			String description, Code... codes) {
		// DB consistency check
		if (Interview.getInterview(interview.getId()) == null) {
			throw new IllegalArgumentException(
					"Statement creation failed. The parent interview does not exist in the database");
		}

		if (codes != null) {
			for (Code c : codes) {
				if (Code.getCode(c.getId()) == null) {
					throw new IllegalArgumentException(
							"Statement creation failed. The Code " + c
									+ " does not exist in the database");

				}
			}
		}

		Statement newStatement = new Statement(interview, time, description,
				codes);
		newStatement.save();
		return newStatement;

	}

	public static boolean removeStatement(Statement statement) {
		if (statement == null
				|| Statement.getStatement(statement.getId()) == null
				|| statement.getInterview() == null)
			return false;
		// Deletion of references is not needed, cause EBeans will manage this
		// automatically::
		// statement.getInterview().removeStatement(statement);
		try{
		statement.delete();
		} catch (RuntimeException e){
			return false;
		}
		return true;
	}

	// *************************************************************************
	// PRIVATE CONSTRUCTORS
	// *************************************************************************

	/**
	 * The statement constructor. It expects consistent data!
	 * 
	 * @param interview
	 *            - the parent interview
	 * @param time
	 *            - timecode of this statement in the audio file
	 * @param description
	 *            - the statement text, that contains what the interviewed had
	 *            said.
	 * @param codes
	 *            - kind of buzzwords to be assigned to this statement for
	 *            better relocating.
	 */
	private Statement(Interview interview, Time time, String description,
			Code... codes) {
		this.interview = interview;
		this.description = description;
		this.time = time;
		this.codes = new ArrayList<Code>();
		for (Code c : codes) {
			this.codes.add(c);
		}
	}

	// *************************************************************************
	// GETTERS AND SETTERS
	// *************************************************************************

	/**
	 * Gives the id of the statement.
	 * 
	 * @return id
	 */
	public UUID getId() {
		return id;
	}

	public UUID getParentId() {
		return interview.getId();
	}

	public Interview getInterview() {
		return interview;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
		save();
	}

	public Time getTime() {
		return time;
	}

	public void setTime(Time time) {
		this.time = time;
		save();
	}

	@Override
	public String toString() {
		return getInterview() + getDescription() + getTime();

	}

	// *************************************************************************
	// MANAGEMENT OF CODES
	// *************************************************************************

	public List<Code> getCodes() {
		return codes;
	}

	/**
	 * Adds Codes to the Statement and refreshes the Code_Statement intersection
	 * table.
	 * 
	 * @param code
	 */
	public void addCode(Code code) {
		if (codes.contains(code))
			return;
		codes.add(code);
		save();
	}

	/**
	 * Removes Codes from the Statement and refreshes the Code_Statement
	 * intersection table.
	 * 
	 * @param code
	 */
	public void removeCode(Code code) {
		if (!codes.contains(code))
			return;
		codes.remove(code);
		save();
	}

	/**
	 * Removes all Codes from the Statement and refreshes the Code_Statement
	 * intersection table.
	 * 
	 */
	public void removeAllCodes() {
		codes = new ArrayList<Code>();
		save();
	}

	// *************************************************************************
	// JSON
	// *************************************************************************

	/**
	 * Provides functionality to convert statements to JSON.
	 * 
	 * @return JSONStringer-Object
	 */
	public JSONStringer toJSON() {
		JSONStringer json = new JSONStringer();

		try {
			json.object().key("statementId").value(id);
			json.key("statementTime").value(time.toString());
			json.key("statementSeconds").value(time.toMillis()/1000);
			json.key("description").value(description);

			if (codes.size() > 0) {
				json.key("codes").array();

				for (Code c : codes) {
					json.object().key("Alias").value(c.getName());
					json.key("Id").value(c.getId()).endObject();
				}

				json.endArray();
			}

			json.endObject();
			return json;

		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not create JSON from Statement");
		}
	}
}