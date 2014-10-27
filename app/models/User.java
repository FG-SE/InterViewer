package models;

import java.util.List;
import java.util.Vector;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import org.json.JSONException;
import org.json.JSONStringer;

import controllers.Logins;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import utils.Password;
/**
 * 
 * @author Florian
 * @author Sven
 *
 */
@SuppressWarnings("serial")
@Entity
public class User extends Model {

	@Id
	private long id;

	@Constraints.Required
	private String email;

	@Constraints.Required
	private String alias;

	@Constraints.Required
	private String password;

	@Constraints.Required
	private boolean admin;

	private String sessionId;

	/**
	 * The Relation between Users and Projects is managed on both sides using
	 * EBeans and the save cascade mechanism.
	 */
	@ManyToMany(cascade = CascadeType.ALL, mappedBy = "users")
	private List<Project> projects;

	// *************************************************************************
	// METHODS TO ADRESS DATABASE, CREATE AND DELETE USERS
	// *************************************************************************

	public static Model.Finder<Long, User> find = new Model.Finder<Long, User>(
			Long.class, User.class);

	public static List<User> findAll() {
		return find.all();
	}

	public static User findByEmail(String email) {
		return find.where().eq("email", email.toLowerCase()).findUnique();
	}

	public static User findById(long id) {
		return find.where().eq("id", id).findUnique();
	}

	public static User findByAlias(String alias) {
		return find.where().eq("alias", alias).findUnique();
	}
	
	/**
	 * deletes user from database by id
	 * 
	 * @param id
	 *            of the user to be deleted
	 */
	public static void delete(Long id) {
		User user = User.find.byId(id);
		user.delete();
	}

	// *************************************************************************
	// AUTHENTIFICATION AND SESSION MANAGEMENT
	// *************************************************************************	
	
	/**
	 * returns the user where email and password matches
	 * 
	 * @param email
	 *            String with entered email
	 * @param password
	 *            String with entered password(cleartext)
	 * @return null if no user found or User object of found user
	 */
	public static User authenticate(String email, String password) {
		User found = null;
		found = find.where().eq("email", email.toLowerCase())
				.eq("password", Password.hashPassword(password)).findUnique();
		if(found != null) 
			return found;
		else
			return find.where().eq("alias", email.toLowerCase())
					.eq("password", Password.hashPassword(password)).findUnique();
	}
	
	/**
	 * Returns true if the user (email) is correctly logged in (the sessionIDs
	 * are identically)
	 * 
	 * @param sessionID
	 *            should be the actual sessionID of the Browser-Session. Can be
	 *            get through session("uuid") by a class extended with
	 *            play.mavc.Contoller
	 * @param email
	 *            identifier of the user. Is stored in cookies
	 *            (session("email")) too.
	 * @return answer
	 */
	public static boolean isCorrectlyLoggedIn(String sessionID, String email) {
		if (email != null && sessionID != null && !email.isEmpty()
				&& !sessionID.isEmpty()) {
			User usr = findByEmail(email);
			if (usr == null || sessionID == null)
				return false;
			if (sessionID.equals(usr.sessionId))
				return true;
		}
		return false;
	}

	// *************************************************************************
	// CONSTRUCTOR
	// *************************************************************************

	public User(String email, String alias, String password, boolean admin) {
		this.email = email.toLowerCase();
		this.password = password;
		this.alias = alias;
		this.admin = admin;
		this.sessionId = null;
		this.projects = new Vector<Project>();
		save();
	}

	// *************************************************************************
	// GETTERS AND SETTERS
	// *************************************************************************

	public String getAlias() {
		return this.alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
		save();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
		save();
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
		save();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
		save();
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
		save();
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
		save();
	}
	
	public List<Project> getProjects() {
		return projects;
	}

	/**
	 * Gives a description excluding password
	 * @see Object
	 */
	@Override
	public String toString() {
		return "User [id=" + getId() + ", alias=" + getAlias() + ", email=" + getEmail() + "]";
	}
	
	/**
	 * Collect all properties of all registered users
	 * 
	 * @return JSONStringer object which is empty or filled with the users properties
	 */
	public static JSONStringer toJSON() throws JSONException {
		List<User> users = findAll();
		
		//create empty json object
		JSONStringer json = new JSONStringer();
		
		if( ! users.isEmpty()) {
			// add key to json object
			json.object().key("Users").array();
			
			// iterate over all users an add their properties to the json object
			for(User u : users) {
				json.object()
					.key("Alias").value(u.getAlias())
					.key("Id").value(u.getId())
					.key("currentUser").value(u.equals(Logins.getLoggedInUser()) ? true : false)
				    .endObject();
			}
			
			// close json object
			json.endArray().endObject();
		}
		
		// return empty json object if no users registered
		return json;
	}
}
