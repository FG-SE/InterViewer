package controllers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONStringer;

import com.fasterxml.jackson.databind.JsonNode;

import models.*;
import play.Logger;
import play.mvc.*;
import utils.Inputcheck;
import utils.Password;
import utils.SqlUtils;
import utils.StringHelper;
import views.html.*;

/**
 * The Admins Controller manages all requests to the Administration-View
 * (/admin)
 */
public class Admins extends Controller {

	/**
	 * @return returns the rendered admins view. all existing users in parameter
	 *         to be shown.
	 */
	public static Result index() {
		if (!Logins.isLoggedIn())
			return redirect(routes.Logins.login());
		if (!Logins.getLoggedInUser().isAdmin())
			return badRequest("Expecting Admin to add Users");
		String message;
		if (Logins.SESSION_MGMT == Logins.SessionManagement.Ignore)
			message = "Session Management is disabled. Please contact developer!";
		else
			message = "";
		List<User> users = User.findAll();
		return ok(admins.render(message, users));
	}

	/**
	 * Checks for PasswordComplexity, valid email, and if alias or email are
	 * already used
	 * 
	 * @param email
	 * @param alias
	 * @param password
	 * @return badrequest if there is a fault or null if everything is ok.
	 */
	private static Result checkInputs(String email, String alias,
			String password) {
		if (email == null || alias == null || password == null) {
			return badRequest("Bitte alle Felder ausfüllen!");
		}
		if (!Inputcheck.isEmail(email)) {
			return badRequest("Bitte eine gültige E-mail-Adresse angeben!");
		}
		if (Password.isPasswordComplex(password) != null) {
			return badRequest("Das Passwort ist nicht komplex genug! "
					+ Password.isPasswordComplex(password));
		}
		if (User.findByAlias(alias) != null) {
			return badRequest("Der Alias bereits belegt!");
		}
		if (User.findByEmail(email) != null) {
			return badRequest("Die E-mail-Adresse ist bereits belegt!");
		}
		return null;
	}

	/**
	 * Checks for PasswordComplexity, valid email and if alias is not empty.
	 * Leaves out alias and user checking (whether already used).
	 * 
	 * @param email
	 * @param alias
	 * @param password
	 * @return a list of faults
	 */
	private static void simpleCheckInputs(String email, String alias,
			String password, List<String> faults) {

		if (email == null || alias == null || alias.equals("")
				|| password == null) {
			faults.add("Bitte alle Felder ausfüllen (Passwort darf frei bleiben)!");
		}
		if (email != null && !Inputcheck.isEmail(email)) {
			faults.add("Bitte eine gültige E-mail-Adresse angeben!");
		}

		if (password != null && !password.equals("")) { // password has been set
			if (Password.isPasswordComplex(password) != null) {
				faults.add("Das Passwort ist nicht komplex genug! "
						+ Password.isPasswordComplex(password));
			}
		}

	}

	/**
	 * /admin/add calls this. Creates a new user depending on the submitted JSON
	 * data. All of these following fields need to be filled: email -
	 * Email-Adress (unique) alias - Name / Alias of the new User (unique)
	 * password - password in cleartext admin - boolean if Administrator or not
	 * 
	 * @return gives the failure/success Message
	 */
	public static Result addUser() {
		if (!Logins.isLoggedIn())
			return redirect(routes.Logins.login());
		if (!Logins.getLoggedInUser().isAdmin())
			return badRequest("Expecting Admin to add Users");

		JsonNode json = request().body().asJson();
		if (json == null)
			return badRequest("Expecting Json data");

		// get submitted data from json
		String email = json.findPath("email").textValue().toLowerCase().trim();
		String alias = json.findPath("alias").textValue();
		String password = json.findPath("password").textValue();
		boolean admin = json.findPath("admin").booleanValue();

		// checks for PasswordComplexity, valid email, and if alias or email
		// already used
		Result check = checkInputs(email, alias, password);
		if (check != null)
			return check;

		User user = new User(email, alias, Password.hashPassword(password),
				admin);
		
		return ok("Der Benutzer wurde hinzugefügt!"
				+ String.valueOf(user.getId()));
	}

	public static Result performSqlQueryMustache() {
		if (!Logins.isLoggedIn())
			return redirect(routes.Logins.login());
		if (!Logins.getLoggedInUser().isAdmin())
			return badRequest("Expecting Admin to add Users");

		JsonNode json = request().body().asJson();
		if (json == null)
			return badRequest("Expecting Json data");

		// get submitted data from json
		String queryString = json.findPath("queryString").textValue()
				.toLowerCase().trim();

		if (queryString.toLowerCase().contains("password")) {
			return badRequest("Passwörter dürfen nicht ausgelesen werden!");
		}

		if (!queryString.toLowerCase().startsWith("select")) {
			return badRequest("Nur Select-Anweisungen sind erlaubt.");
		}

		Logger.info("Performed SQL Query: " + queryString + "requested by "
				+ Logins.getLoggedInUser());

		try {
			JSONStringer jsonStringer = SqlUtils
					.getDirectQueryMustacheJSON(queryString);
			return ok(jsonStringer.toString());
		} catch (SecurityException se) {
			return badRequest("Diese Funktion ist aus Sicherheitsgründen nicht verfügbar.");
		} catch (Exception e) {
			return badRequest("Der SQL-Befehl konnte nicht ausgeführt werden.");
		}

	}

	/**
	 * /admin/edit calls this. Edits a user depending on the submitted JSON
	 * data. All of these following fields need to be filled: oldemail - to
	 * identify the edited User email - new Email-Adress password - new Password
	 * in cleartext admin - boolean if User becomes Administrator
	 * 
	 * @return gives a success or badRequest message
	 */
	public static Result editUser() {
		if (!Logins.isLoggedIn() || !Logins.getLoggedInUser().isAdmin())
			return redirect(routes.Logins.login());

		// 1. Check Request and get submitted json Data:
		JsonNode json = request().body().asJson();
		if (json == null)
			return badRequest("Expecting Json data");

		String oldemail = json.findPath("oldemail").textValue().toLowerCase()
				.trim();
		String email = json.findPath("email").textValue().toLowerCase().trim();
		String alias = json.findPath("alias").textValue();
		String password = json.findPath("password").textValue();
		boolean admin = json.findPath("admin").booleanValue();

		// 2. Check Prerequisites:

		// In this list all faults will be collected
		List<String> faultList = new ArrayList<String>();

		// Get the User to be edited
		User usr = User.findByEmail(oldemail);
		if (usr == null)
			faultList.add("Der zu bearbeitende Benutzer " + oldemail
					+ "wurde nicht gefunden!");

		// checks: password complexity, email is valid, alias not empty
		simpleCheckInputs(email, alias, password, faultList);

		if (faultList.size() > 0)
			return badRequest(StringHelper.buildAggragationString(faultList,
					" "));

		// 3. Perform Operations:

		// in this list all performed changes will be collected
		List<String> okList = new ArrayList<String>();

		changeAlias(usr, alias, faultList, okList);

		changeEMail(usr, email, faultList, okList);

		changePassword(usr, password, faultList, okList);

		changeAdminRights(usr, admin, faultList, okList);

		// 4. Give Feedback:

		if (faultList.size() > 0) {
			faultList
					.add(0,
							"Änderungen konnten nicht vollständig durchgeführt werden:");
			if (okList.size() > 0) {
				faultList.add("Folgende Änderungen wurden vorgenommen:");
				faultList.addAll(okList);
			}
			return badRequest(StringHelper.buildAggragationString(faultList,
					" "));

		} else {
			if (okList.size() > 0) {
				okList.add(0, "Änderungen wurden vollständig durchgeführt:");
			} else {
				okList.add("Keine Änderungen.");
			}
			return ok(StringHelper.buildAggragationString(okList, " "));
		}

	}

	/**
	 * This method changes the alias of an existing user
	 * 
	 * @param usr
	 *            - user object from db
	 * @param alias
	 *            - new alias
	 * @return gives a list of human readable faults or an empty list if
	 *         everything is ok.
	 */
	private static void changeAlias(User usr, String alias,
			List<String> faultList, List<String> okList) {
		// checks if alias changed. If changed it has to be unused so far.
		if (!usr.getAlias().equals(alias) && User.findByAlias(alias) != null) {
			faultList.add("Der Alias ist bereits belegt!");
		} else {
			// do changes only if required
			if (!usr.getAlias().equals(alias)) {
				usr.setAlias(alias);
				okList.add("Alias aktualisiert.");
			}
		}
	}

	/**
	 * This method changes the alias of an existing user
	 * 
	 * @param usr
	 *            - user object from db
	 * @param alias
	 *            - new alias
	 * @return gives a list of human readable faults or an empty list if
	 *         everything is ok.
	 */
	private static void changeEMail(User usr, String email,
			List<String> faultList, List<String> okList) {
		// do changes only if required
		if (!usr.getEmail().equals(email)) {
			// email has to stay unique
			if (User.findByEmail(email) != null) {
				faultList.add("Die E-mail-Adresse ist bereits belegt!");
			} else {
				// edit own email needs update of session
				if (Logins.getLoggedInUser().getEmail().equals(usr.getEmail()))
					session("email", email);
				usr.setEmail(email);
				okList.add("E-mail-Adresse aktualisiert.");
			}
		}
	}

	/**
	 * This method changes the alias of an existing user
	 * 
	 * @param usr
	 *            - user object from db
	 * @param alias
	 *            - new alias
	 * @return gives a list of human readable faults or an empty list if
	 *         everything is ok.
	 */
	private static void changePassword(User usr, String password,
			List<String> faultList, List<String> okList) {
		// do changes only if required
		if (!password.equals("")) {
			usr.setPassword(Password.hashPassword(password));
			okList.add("Password geändert.");
		}
	}

	/**
	 * This method changes the admin rights of an existing user
	 * 
	 * @param usr
	 *            - user object from db
	 * @param admin
	 *            - true - gets admin rights, false looses admin rights.
	 * @return gives a list of human readable faults or an empty list if
	 *         everything is ok.
	 */
	private static void changeAdminRights(User usr, boolean admin,
			List<String> faultList, List<String> okList) {
		// don't allow to change own admin rights
		if (!admin && Logins.getLoggedInUser().getId() == usr.getId()) {
			faultList
					.add("Sie können sich nicht selbst die Admin-Rechte entziehen!");
		} else {
			// do changes only if required
			if (usr.isAdmin() != admin) {
				usr.setAdmin(admin);
				okList.add("Admin-Berechtigungen aktualisiert.");
			}
		}
	}

	/**
	 * /admin/delete calls this. Deletes a user depending on the submitted JSON
	 * data. id - ID of specific User
	 * 
	 * @return gives a success or badRequest message
	 */
	public static Result deleteUser() {
		if (!Logins.isLoggedIn())
			return redirect(routes.Logins.login());
		if (!Logins.getLoggedInUser().isAdmin())
			return badRequest("Expecting Admin to add Users");

		JsonNode json = request().body().asJson();
		if (json == null)
			return badRequest("Expecting Json data");

		// find the specific User
		long id = json.findPath("id").longValue();
		User usr = User.findById(id);
		if (usr == null)
			return badRequest("Der zu löschende Benutzer " + id
					+ " wurde nicht gefunden!");

		// deleting himself is not possible
		if (Logins.getLoggedInUser().getEmail().equals(usr.getEmail()))
			return badRequest("Man kann sich nicht selber löschen! Bitte mit einem anderen Benutzer "
					+ "(Admin) anmelden um diesen Administrator zu löschen!");

		// Store Email and Alias before delete to create a useful Success
		// message
		String email = usr.getEmail();
		String alias = usr.getAlias();
		usr.delete();
		return ok("User " + id + "(" + email + ": " + alias + ") deleted!");
	}

	/**
	 * to Update User-Information to actual version (reload) /admin/reload/:id
	 * calls this
	 * 
	 * @param id
	 *            of the specific user
	 * @return JSON with Id, email, alias, admin
	 */
	public static Result getUser() {
		if (!Logins.isLoggedIn())
			return redirect(routes.Logins.login());
		if (!Logins.getLoggedInUser().isAdmin())
			return badRequest("Expecting Admin to add Users");

		// get requested id
		JsonNode data = request().body().asJson();
		if (data == null)
			return badRequest("Expecting Json data");
		long id = data.findPath("id").longValue();

		// find the User
		User usr = User.findById(id);
		if (usr == null) {
			return badRequest("User " + id + " wurde nicht gefunden!");
		}

		// creates json with the data
		JSONStringer json = new JSONStringer();
		try {
			json.object().key("Id").value(usr.getId()).key("email")
					.value(usr.getEmail()).key("alias").value(usr.getAlias())
					.key("admin").value(usr.isAdmin()).endObject();
			return ok(json.toString());

		} catch (JSONException e) {
			e.printStackTrace();
			return badRequest("JSON konnte nicht erstellt werden!");
		}
	}

	/**
	 * Deletes all (non-admin) Users (dangerous).
	 * 
	 * @return Success or failure message
	 */
	public static Result deleteAllUser() {
		if (!Logins.isLoggedIn())
			return redirect(routes.Logins.login());
		if (!Logins.getLoggedInUser().isAdmin())
			return badRequest("Expecting Admin to add Users");

		// get all Users
		List<User> users = User.findAll();
		if (users.size() <= 0)
			return badRequest("Es wurden keine Benutzer gefunden!");

		// delete all non-admins and not himself
		for (User usr : users) {
			if (!usr.isAdmin()
					&& !Logins.getLoggedInUser().getEmail()
							.equals(usr.getEmail()))
				usr.delete();
		}

		return ok("Es wurden alle normalen Benutzer gelöscht!");
	}
}
