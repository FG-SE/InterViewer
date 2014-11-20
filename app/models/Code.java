package models;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import play.data.validation.Constraints;
import play.db.ebean.Model;

/**
 * Codes are set on Statements to further characterize them. They are
 * system-wide unique and support auto-completion. To manage the uniqueness a
 * factory method is used.
 *
 * @author Sven
 * @author Thiemo
 * @author Benedikt
 */
@SuppressWarnings("serial")
@Entity
public class Code extends Model {

	@Id
	private UUID id;

	@Constraints.Required
	private final String name;

	/**
	 * One Code occurs in many Statements. One Statement has many Codes. This is
	 * a bi-directional Relationship managed on both sides using EBeams.
	 */
	@ManyToMany(cascade = CascadeType.ALL)
	private List<Statement> occurrence;

	// *************************************************************************
	// METHODS TO ADRESS DATABASE, CREATE AND DELETE CODES
	// *************************************************************************

	/**
	 * the database Finder-object for the code-relation
	 */
	private static Finder<UUID, Code> finder = new Finder<UUID, Code>(
			UUID.class, Code.class);

	/**
	 * Gives the Code instances to a specified name. Creates a new object if no
	 * such code exists.
	 *
	 * @param name
	 *            - the name of the desired code
	 * @return the project wide unique Code.
	 */
	public static Code getCodeByName(String name) {
		Code result = finder.where().eq("name", name).findUnique();
		if (result == null) {
			result = new Code(name);
			result.save();
		}
		return result;
	}

	/**
	 * @deprecated use findAllCodes() for name consistency
	 */
	@Deprecated
	public static List<Code> getAllCodes() {
		return findAllCodes(null);
	}

	/**
	 * Gives all codes saved to the database.
	 * @return all codes
	 */
	public static List<Code> findAllCodes(Project p) {
		final LinkedHashSet<Code> result = new LinkedHashSet<Code>();
		for(final Interview i : p.getInterviews()){
			for(final Statement s : i.getStatements()){
				for(final Code c : s.getCodes()){
					result.add(c);
				}
			}
		}
		return new ArrayList<Code>(result);
	}

	/**
	 * Gives the code identified by id.
	 * @param id
	 */
	public static Code getCode(UUID id) {
		return finder.where().eq("id", id).findUnique();
	}

	// *************************************************************************
	// PRIVATE CONSTRUCTOR
	// *************************************************************************

	/**
	 * The Constructor for a Code-Object. It takes a name. Be sure that
	 * this name does not exist in the database yet.
	 *
	 * @param name
	 */
	private Code(String name) {
		this.name = name;
	}

	// *************************************************************************
	// GETTERS AND SETTERS
	// *************************************************************************

	public UUID getId(){
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public List<Statement> getOccurrence() {
		return this.occurrence;
	}

	/**
	 * This Setter is not used externally thus addition and removal of Codes is managed from Statement-side.
	 */
	protected void addOccurrence(Statement statement){
		if(this.occurrence.contains(statement)) {
			return;
		}
		this.occurrence.add(statement);
		this.saveManyToManyAssociations("occurrence");
	}

	/**
	 * This Setter is not used externally thus addition and removal of Codes is managed from Statement-side.
	 */
	protected void deleteOccurrence(Statement statement){
		if(!this.occurrence.contains(statement)) {
			return;
		}
		this.occurrence.remove(statement);
		this.saveManyToManyAssociations("occurrence");
	}


}
