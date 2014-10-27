package controllers;

import java.io.File;
import java.io.IOException;

import models.AudioFile;
import models.Code;
import models.Interview;
import models.Project;
import models.Statement;
import models.Time;
import models.User;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Password;
import views.html.*;

/**
 * Administrate the session of the users.
 */
public class Logins extends Controller {

	private enum TestDataManagement {
		noTestData, useTestData
	}

	// For development it might be useful to load testdata on login.
	// TestDataManagement.noTestData
	// logged-in users will not be resettet
	protected static final TestDataManagement TESTDATA = TestDataManagement.noTestData;

	public enum SessionManagement {
		Ignore, Active
	}

	// for development it might be useful to disable Session management.
	protected static final SessionManagement SESSION_MGMT = SessionManagement.Active;

	/**
	 * @return the currently logged in User or "marie" if Session_mgmt =
	 *         SessionManagement.Ignore
	 */
	public static User getLoggedInUser() {
		if (SESSION_MGMT == SessionManagement.Active) {
			if (User.isCorrectlyLoggedIn(session("uuid"), session("email")))
				return User.findByEmail(session("email"));
			else
				return null;
		}
		return User.findByEmail("mariemuster@example.com");
	}

	/**
	 * @return true if User has logged in, false if no User has logged in
	 */
	public static boolean isLoggedIn() {
		if (SESSION_MGMT == SessionManagement.Active) {
			if (User.isCorrectlyLoggedIn(session("uuid"), session("email")))
				return true;
			else
				return false;
		}
		return true;
	}

	/**
	 * Fill the database with test data. Only called if TestDataManagement is
	 * set on useTestData.
	 */
	public static void fillInTestData() {
		if (User.findByAlias("marie") == null)
			new User("mariemuster@example.com", "marie",
					Password.hashPassword("12345"), true);

		Project marketing = Project.createProject("amazingNewMarketingProject");

		if (User.findByAlias("tina") == null)
			marketing.addUser(new User("tinamuster@example.com", "tina",
					Password.hashPassword("12345"), true));
		if (User.findByAlias("hans") == null)
			marketing.addUser(new User("hansmuster@example.com", "hans",
					Password.hashPassword("12345"), true));
		if (User.findByAlias("max") == null)
			marketing.addUser(new User("maxmuster@example.com", "max", Password
					.hashPassword("12345"), false));

		Interview conversation1 = Interview.createInterview(marketing,
				"conversation1");
		Interview conversation2 = Interview.createInterview(marketing,
				"conversation2");

		conversation1.setProperty(marketing.getPropertyTypeByName("Datum"),
				"30.10.2013");
		conversation2.setProperty(marketing.getPropertyTypeByName("Datum"),
				"11.11.2013");

		try {
			conversation1.setAudio(new AudioFile(new File("audioFiles/"), File
					.createTempFile("audio", ".mp3")));
			conversation2.setAudio(new AudioFile(File.createTempFile("audio",
					".mp3")));
		} catch (IOException e) {
		}

		Statement
				.createStatement(conversation1, new Time(1, 52, 3), "Hallo?",
						Code.getCodeByName("Important"),
						Code.getCodeByName("Question"));

		Statement.createStatement(conversation1, new Time(2, 45), "Welt",
				Code.getCodeByName("Optional"));

		Statement.createStatement(conversation1, new Time(3, 0, 0),
				"Das ist Wichtig", Code.getCodeByName("Important"));

		Statement.createStatement(conversation2, new Time(4, 2335, 12),
				"Wirklich", Code.getCodeByName("Optional"));

		Statement.createStatement(conversation2, new Time(5, 1, 4), "Nein");

		Statement.createStatement(conversation2, new Time(6, 55, 55), "Ja");

	}

	/**
	 * Deletes all Projects, codes an users, that were created for tests.
	 * 
	 * @param all
	 *            true if the currently logged-in user should also be deleted.
	 */
	public static void cleanUpTestData(boolean all) {
		for (Project project : Project.findAllProjects()) {
			project.delete();

			for (Code c : Code.findAllCodes(project)) {
				c.delete();

			}

		}

		for (User u : User.findAll()) {
			if (all) {
				u.delete();
			} else {
				if (u.getSessionId() == null)
					u.delete();
			}
		}

	}

	/**
	 * /login calls this. Checks whether a is is logged in and redirect him to
	 * the projects overview or render the login view.
	 * 
	 * @return rendered login view
	 */
	public static Result login() {
		if (User.isCorrectlyLoggedIn(session("uuid"), session("email")))
			return redirect(routes.Projects.index());
		else {
			if (TESTDATA == TestDataManagement.useTestData) {
				cleanUpTestData(true);
				fillInTestData();
			}
			return ok(login.render("", "", ""));
		}
	}

	/**
	 * /about calls this. Render about view.
	 * 
	 * @return rendered about view
	 */
	public static Result about() {
		return ok(about.render());
	}

	/**
	 * Create a new session and log in the user.
	 * 
	 * @return rendered projects view if correctly logged in. Otherwise the
	 *         rendered login view with an error message.
	 */
	public static Result authenticateUser() {
		DynamicForm dynform = Form.form().bindFromRequest();

		String password = dynform.get("password");
		String email = dynform.get("email");

		email = email.trim();

		User usr = User.authenticate(email, password);
		if (usr != null) {
			session().clear();

			// Generate a unique id for this login-session
			String uuid = java.util.UUID.randomUUID().toString();
			usr.setSessionId(uuid);
			// save in session and user object
			session("uuid", usr.getSessionId());
			session("email", usr.getEmail());

			Logger.info(usr + " logged in.");
			return redirect(routes.Projects.index());
		} else {
			Logger.info("Denied access! Alias/Email:" + email);
			return ok(login.render(email, password,
					"Email oder Passwort fehlerhaft!"));
		}
	}

	/**
	 * Destroys the session of the current user.
	 * 
	 * @return rendered login view
	 * @throws Exception
	 */
	public static Result logout() throws Exception {
		if (!Logins.isLoggedIn())
			return badRequest("Expecting Logged-in User to log him out!");

		String sessionID = session("uuid");
		String email = session("email");

		User usr = User.findByEmail(email);

		if (usr == null || sessionID == null)
			throw new Exception(
					"User not found - check email and sessionID in Cookies");
		if (!usr.getSessionId().equals(sessionID))
			throw new Exception(
					"User ist nicht eingeloggt... check User-sessionID!");

		usr.setSessionId("");
		session().clear();

		return redirect(routes.Logins.login());
	}
}
