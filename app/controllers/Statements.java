package controllers;

import models.Statement;
import models.Time;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import models.AudioFile;
import models.Code;
import models.Interview;
import models.Project;
import play.api.libs.MimeTypes;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

/**
 * The statements controller manages all request from the editor view
 */
public class Statements extends Controller {
	public enum CodesViewManagement {
		notSupported, useCommaSeperated, useDropDownMenu
	}

	private static final CodesViewManagement CODES_VIEW = CodesViewManagement.useDropDownMenu;

	/**
	 * There are two ways to support the codes. The codes can be shown as a
	 * dropdown menu oder comma seperated
	 * 
	 * @return useCommaSeperated or useDropDownMenu if they will be shown comma
	 *         seperated or as a dropdown menu. notSupported if the feature is
	 *         not supported yet.
	 */
	public static CodesViewManagement getCodesViewStyle() {
		return CODES_VIEW;
	}

	/**
	 * Render the editor with all statements of the corresponding interview.
	 * 
	 * @return the rendered editor
	 */
	public static Result editor() {
		if (!Logins.isLoggedIn())
			return redirect(routes.Logins.login());

		DynamicForm form = Form.form().bindFromRequest();
		String interviewId = form.get("interviewId");
		try {
			if (interviewId == null) {
				return badRequest("Es wurde kein Interview ausgewählt.");
			}
		} catch (IllegalArgumentException e) {
			return badRequest("Das Interview mit der ID '" + interviewId
					+ "' konnte nicht gefunden werden.");
		}

		UUID uuid;
		try {
			uuid = UUID.fromString(interviewId);
		} catch (IllegalArgumentException e) {
			return badRequest("Die gegebene ID kann keinem Interview zugeordnet werden.");
		}

		if (uuid == null) {
			return badRequest("Die gebebene ID '" + interviewId
					+ "' ist keine korrekte ID eines Interviews");
		}

		Interview interview = Interview.getInterview(uuid);

		if (interview == null) {
			return badRequest("Das Interview mit der gebebenen ID '"
					+ interviewId + "' konnte nicht gefunden werden.");
		}

		if (!interview.getProject().getUsers()
				.contains(Logins.getLoggedInUser())) {
			return badRequest("Sie besitzen nicht die Berechtigung dieses Interview zu bearbeiten.");
		}

		AudioFile audio = interview.getAudio();
		if (audio == null) {
			return badRequest("Es existiert keine Audio Datei zum Interview '"
					+ interviewId + "'");
		}
		Project project = Interview.getInterview(UUID.fromString(interviewId))
				.getProject();
		return ok(editor.render(interviewId, project, interview));
	}

	/**
	 * Send the whole audio file or only a range of bytes of the file.
	 * 
	 * @return audio data or error message
	 * @throws IOException
	 */
	public static Result getAudio() throws IOException {
		if (!Logins.isLoggedIn())
			return redirect(routes.Logins.login());

		DynamicForm form = Form.form().bindFromRequest();
		String interviewId = form.get("interviewId");

		// Check if the given string is a valid UUID
		UUID uuid;
		try {
			uuid = UUID.fromString(interviewId);
		} catch (IllegalArgumentException e) {
			return badRequest("Die gegebene ID ist nicht korrekt.");
		}

		// Check if the given uuid corresponds to an interview
		Interview interview = Interview.getInterview(uuid);
		if (interview == null) {
			return badRequest("Die gegebene ID kann keinem Interview zugeordnet werden.");
		} else {

			// Check if the user has the right (member of the project) to
			// retrieve the audio file
			if (interview.getProject().findUser(Logins.getLoggedInUser())) {
				AudioFile audio = interview.getAudio();

				// Check if the interview has an audio file
				if (interview.getAudio() == null) {
					return badRequest("Es konnte keine Audio-Datei zum Interview'"
							+ interview.getName()
							+ "' gefunden werden. Path: "
							+ audio.getPath());
				}

				// get File to return
				File file = audio.getFile();

				// streaming if required
				String rangeheader = request().getHeader(RANGE);
				if (rangeheader != null) {

					String[] split = rangeheader.substring("bytes=".length())
							.split("-");
					if (split.length == 1) {
						long start = Long.parseLong(split[0]);
						long end = file.length() - 1l;
						return stream(start, end, file);
					} else {
						long start = Long.parseLong(split[0]);
						long end = Long.parseLong(split[1]);
						return stream(start, end, file);
					}

				} else {
					// no streaming, so just send audio file
					response().setContentType("audio/mpeg");
					response().setHeader("Content-disposition",
							"attachment; filename=" + audio.getName());
					return ok(audio.getFile());
				}
			} else {
				return badRequest("Sie haben nicht die Berechtigung diese Audio Datei anzuzeigen.");
			}
		}
	}

	// 206 Partial content Byte range requests - cf.
	// https://gist.github.com/stopher/5495353
	private static Result stream(long start, long end, File file)
			throws IOException {
		int length = (int) ((end - start) + 1l);
		byte[] buffer = new byte[length];
		FileInputStream fis = new FileInputStream(file);
		fis.skip(start);
		fis.read(buffer);
		fis.close();

		response().setContentType(MimeTypes.forExtension("mp3").get());
		response().setHeader(CONTENT_LENGTH, length + "");
		response().setHeader(CONTENT_RANGE,
				String.format("bytes %d-%d/%d", start, end, file.length()));
		response().setHeader(ACCEPT_RANGES, "bytes");
		response().setHeader(CONNECTION, "keep-alive");
		return status(PARTIAL_CONTENT, buffer);
	}

	/**
	 * Save a statement. The data is reveived as Json.
	 * 
	 * @return the new statement as Json
	 */
	public static Result addStatement() {
		JsonNode json = request().body().asJson();

		if (json == null) {
			return badRequest("Die Eingabe-Daten sind leer.");
		}

		Interview interview = Interview.getInterview(UUID.fromString(json
				.findPath("interviewId").asText()));
		
		if (!interview.getProject().getUsers()
				.contains(Logins.getLoggedInUser())) {
			return badRequest("Sie besitzen nicht die Berechtigung diese Aussage zu erstellen.");
		}
		
		Time time = new Time(json.findPath("statementTime").asText());
		Statement statement = Statement.createStatement(interview, time, "");
		return ok(statement.toJSON().toString());
	}

	/**
	 * Remove a Statement by id.
	 * 
	 * @param id
	 *            the statement id
	 * @return success or error message
	 */
	public static Result removeStatement(String id) {
		Statement statement = Statement.getStatement(UUID.fromString(id));
		
		if (!statement.getInterview().getProject().getUsers()
				.contains(Logins.getLoggedInUser())) {
			return badRequest("Sie besitzen nicht die Berechtigung diese Aussage zu entfernen.");
		}
		
		if (Statement.removeStatement(statement)) {
			return ok();
		}
		return badRequest("Das System konnte die Aussage nicht entfernen.");
	}

	/**
	 * Edit a statement which the data of the received Json. Supports comma
	 * seperated codes or codes in the Json format.
	 * 
	 * @return success or error message
	 */
	public static Result saveStatement() {
		JsonNode json = request().body().asJson();

		if (json == null) {
			return badRequest("Die Eingabe-Daten sind leer.");
		}

		Statement statement = Statement.getStatement(UUID.fromString(json
				.findPath("statementId").asText()));
		
		if (!statement.getInterview().getProject().getUsers()
				.contains(Logins.getLoggedInUser())) {
			return badRequest("Sie besitzen nicht die Berechtigung diese Aussage zu bearbeiten.");
		}
		
		statement.setDescription(json.findPath("description").asText());
		statement.setTime(new Time(json.findPath("statementTime").asText()));

		if (CODES_VIEW == CodesViewManagement.useCommaSeperated) {
			String[] codeStrings = json.findPath("codes").asText().split(",");
			statement.removeAllCodes();
			for (String s : codeStrings) {
				if (s != null && !s.trim().equals("")) {
					statement.addCode(Code.getCodeByName(s.trim()));
				}
			}
		} else if (CODES_VIEW == CodesViewManagement.useDropDownMenu) {
			JsonNode codes = json.findPath("codes");
			statement.removeAllCodes();
			for (JsonNode codeNode : codes) {
				String codeValue = codeNode.asText();
				statement.addCode(Code.getCodeByName(codeValue));
			}
		}
		return ok();
	}

	/**
	 * Return a statement by id.
	 * 
	 * @param id
	 *            the statement id
	 * @return
	 */
	public static Result getStatement(String id) {
		Statement statement = Statement.getStatement(UUID.fromString(id));
		return ok(statement.toJSON().toString());
	}
}
