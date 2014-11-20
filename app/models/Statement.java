package models;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.json.JSONException;
import org.json.JSONStringer;

import play.data.validation.Constraints;
import play.db.ebean.Model;

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
	private final Interview interview;

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
		final List<Statement> all = finder.where().eq("interview", interview)
				.findList();
		final List<Statement> result = new ArrayList<Statement>();
		for (final Statement s : all) {
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
			for (final Code c : codes) {
				if (Code.getCode(c.getId()) == null) {
					throw new IllegalArgumentException(
							"Statement creation failed. The Code " + c
									+ " does not exist in the database");

				}
			}
		}

		final Statement newStatement = new Statement(interview, time, description,
				codes);
		newStatement.save();
		return newStatement;

	}

	public static boolean removeStatement(Statement statement) {
		if (statement == null
				|| Statement.getStatement(statement.getId()) == null
				|| statement.getInterview() == null) {
			return false;
		}
		// Deletion of references is not needed, cause EBeans will manage this
		// automatically::
		// statement.getInterview().removeStatement(statement);
		try{
		statement.delete();
		} catch (final RuntimeException e){
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
		for (final Code c : codes) {
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
		return this.id;
	}

	public UUID getParentId() {
		return this.interview.getId();
	}

	public Interview getInterview() {
		return this.interview;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
		this.save();
	}

	public Time getTime() {
		return this.time;
	}

	public void setTime(Time time) {
		this.time = time;
		this.save();
	}

	@Override
	public String toString() {
		return this.getInterview() + this.getDescription() + this.getTime();

	}

	// *************************************************************************
	// MANAGEMENT OF CODES
	// *************************************************************************

	public List<Code> getCodes() {
		return this.codes;
	}

	/**
	 * Adds Codes to the Statement and refreshes the Code_Statement intersection
	 * table.
	 *
	 * @param code
	 */
	public void addCode(Code code) {
		if (this.codes.contains(code)) {
			return;
		}
		this.codes.add(code);
		this.save();
	}

	/**
	 * Removes Codes from the Statement and refreshes the Code_Statement
	 * intersection table.
	 *
	 * @param code
	 */
	public void removeCode(Code code) {
		if (!this.codes.contains(code)) {
			return;
		}
		this.codes.remove(code);
		this.save();
	}

	/**
	 * Removes all Codes from the Statement and refreshes the Code_Statement
	 * intersection table.
	 *
	 */
	public void removeAllCodes() {
		this.codes = new ArrayList<Code>();
		this.save();
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
		final JSONStringer json = new JSONStringer();

		try {
			json.object().key("statementId").value(this.id);
			json.key("statementTime").value(this.time.toString());
			json.key("statementSeconds").value(this.time.toMillis()/1000);
			json.key("description").value(this.description);

			if (this.codes.size() > 0) {
				json.key("codes").array();

				for (final Code c : this.codes) {
					json.object().key("Alias").value(c.getName());
					json.key("Id").value(c.getId()).endObject();
				}

				json.endArray();
			}

			json.key("missingCodes").array();
			for (final Code c : this.determineMissingCodes()) {
				json.object().key("Alias").value(c.getName());
				json.key("Id").value(c.getId()).endObject();
			}
			json.endArray();

			json.endObject();
			return json;

		} catch (final JSONException e) {
			throw new RuntimeException("Could not create JSON from Statement", e);
		}
	}

	private Set<Code> determineMissingCodes() {
		final LinkedHashSet<Code> result = new LinkedHashSet<Code>(Code.findAllCodes(this.getInterview().getProject()));
		result.removeAll(this.codes);
		return result;
	}
}