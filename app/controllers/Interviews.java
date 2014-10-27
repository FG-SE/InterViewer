package controllers;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import play.data.DynamicForm;
import play.data.Form;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import models.*;
import views.html.*;

/**
 * The Interviews Controller manages all requests of the interviews view.
 */
public class Interviews extends Controller {

	/**
	 * /interviews calls this. Return all interviews of the project that is
	 * identified by the given Id.
	 * 
	 * @return rendered interviews view
	 */
	public static Result getAllInterviews() {
		if (!Logins.isLoggedIn())
			return redirect(routes.Logins.login());

		DynamicForm form = Form.form().bindFromRequest();
		Project project = Project.getProject(UUID.fromString(form
				.get("projectId")));

		if (project.findUser(Logins.getLoggedInUser()) == false)
			return badRequest("Sie sind nicht berechtigt die Interviews dieses Projekts anzuzeigen.");

		return ok(interviews.render("", project));
	}

	/**
	 * /addInterview calls this. Receive the data for a new interview as
	 * MultipartFormData, create the new interview and set its values.
	 * 
	 * @return the new interview as Json
	 */
	public static Result addInterview() {
		if (!Logins.isLoggedIn())
			return redirect(routes.Logins.login());

		MultipartFormData body = request().body().asMultipartFormData();
		String interviewName = body.asFormUrlEncoded().get("interviewName")[0]
				.toString();
		String projectId = body.asFormUrlEncoded().get("projectId")[0]
				.toString();
		Project project = Project.getProject(UUID.fromString(projectId));

		if (interviewName == null || interviewName.equals("")) {
			badRequest("Bitte geben sie einen Interviewnamen an!");
		}

		try {
			Interview interview = Interview.createInterview(project,
					interviewName);
			saveInterview(interview, project);
			return ok(interview.toJSON().toString());
		} catch (IllegalArgumentException e) {
			return badRequest(e.getMessage());
		} catch (IOException e) {
			return badRequest(e.getMessage());
		}
	}

	/**
	 * Helper method to register an audio file correctly in the interview modal.
	 * 
	 * @param data
	 *            the audio file
	 * @param interview
	 *            the interview to add to
	 * @throws IOException
	 */
	private static void uploadAudio(FilePart data, Interview interview)
			throws IOException {
		if (data != null) {
			String originalFileName = data.getFilename();
			String contentType = data.getContentType();

			// check for chrome and firefox
			if (contentType.equals("audio/mp3")
					|| contentType.equals("audio/mpeg")) {
				File audioFile = data.getFile();

				AudioFile audio = new AudioFile(audioFile);
				audio.setName(originalFileName);
				interview.setAudio(audio);

			} else {
				throw new IOException("Die Datei hat ein falsches Format ("
						+ contentType + ").");
			}
		} else {
			throw new IOException(
					"Datei-Upload fehlgeschlagen aufgrund fehlender Daten. Bitte geben Sie eine Audio Datei an.");
		}
	}

	/**
	 * Set the received properties and save the audio file of an interview
	 * 
	 * @param interview
	 *            the interview in that the values are to be set
	 * @param project
	 *            the project to which the interview belongs to
	 * @throws IOException
	 */
	private static void saveInterview(Interview interview, Project project)
			throws IOException {

		MultipartFormData body = request().body().asMultipartFormData();
		FilePart data = body.getFile("audio");

		String interviewName = body.asFormUrlEncoded().get("interviewName")[0]
				.toString();

		interview.setName(interviewName);

		// set property values
		for (PropertyType p : project.getPropertyTypes()) {
			// get property value from sended form
			String propertyValue = body.asFormUrlEncoded().get(p.getName())[0]
					.toString();
			interview.setProperty(p, propertyValue);
		}

		// set audio
		if (data != null) {
			Interviews.uploadAudio(data, interview);
		}
	}

	/**
	 * /removeInterview calls this. Delete an interview. The interview is
	 * identified by the Id that is in the received form. Only users who are
	 * members of the corresponding project are able to delete the interview.
	 * 
	 * @return
	 */
	public static Result removeInterview() {
		if (!Logins.isLoggedIn())
			return redirect(routes.Logins.login());

		DynamicForm form = Form.form().bindFromRequest();
		Interview interview = Interview.getInterview(UUID.fromString(form
				.get("interviewId")));

		if (interview.getProject().findUser(Logins.getLoggedInUser()) == false)
			return badRequest("Sie sind nicht berechtigt das Interview zu entfernen.");

		Interview.removeInterview(interview);
		return ok("success");
	}

	/**
	 * /getInterviewInfo/[ID] calls this. Returns the interview as Json. Only
	 * member of the corresponding project are able to retrieve the
	 * informations.
	 * 
	 * @param id
	 *            the id of the interview
	 * @return interview as Json or error message
	 */
	public static Result getInterviewInfo(String id) {
		if (!Logins.isLoggedIn())
			return redirect(routes.Logins.login());

		Interview interview = Interview.getInterview(UUID.fromString(id));

		if (interview.getProject().findUser(Logins.getLoggedInUser()) == false)
			return badRequest("User ist nicht berechtigt das Interview zu sehen.");

		return ok(interview.toJSON().toString());
	}

	/**
	 * /editInterview calls this. Receive the edited interview values as
	 * MultipartFormData and save the changed interview. Only qualified users
	 * can edit the interview.
	 * 
	 * @return
	 */
	public static Result editInterview() {
		if (!Logins.isLoggedIn())
			return redirect(routes.Logins.login());

		MultipartFormData body = request().body().asMultipartFormData();
		String projectId = body.asFormUrlEncoded().get("projectId")[0]
				.toString();
		String interviewId = body.asFormUrlEncoded().get("interviewId")[0]
				.toString();
		Project project = Project.getProject(UUID.fromString(projectId));
		Interview interview = Interview.getInterview(UUID
				.fromString(interviewId));

		if (interview != null
				&& interview.getProject().findUser(Logins.getLoggedInUser()) == false)
			return badRequest("Sie sind nicht berechtigt das Interview zu bearbeiten.");

		if (interview != null) {
			try {
				saveInterview(interview, project);
				return ok(interview.toJSON().toString());
			} catch (IOException e) {
				return badRequest(e.getMessage());
			}
		} else {
			return badRequest("Das Interview wurde nicht in der Datenbank  gefunden.");
		}
	}
}
