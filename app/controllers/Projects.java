package controllers;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONStringer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.*;
import play.mvc.*;
import views.html.*;
import utils.Export;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The Projects Controller manages all requests from the projects view.
 */
public class Projects extends Controller {

	/**
	 * / calls this. Only shown if the user is logged in. Otherwise the user
	 * will be redirect to the login screen. Renders the projects view with all
	 * projects that the user can access.
	 * 
	 * @return rendered projects view
	 * @throws IOException
	 */
	public static Result index() throws IOException {
		if (!Logins.isLoggedIn())
			return redirect(routes.Logins.login());

		// only get projects which the current user is authorized to access
		List<Project> allProjects = Logins.getLoggedInUser().getProjects();

		return ok(projects.render("", allProjects));
	}

	/**
	 * /projects/getInfo calls this. Requires body-data with projectName. All
	 * Project data as JSON will be returned.
	 * 
	 * @return Status with JSON of Project datas
	 */
	public static Result getInfo() {
		if (!Logins.isLoggedIn())
			return redirect(routes.Logins.login());

		// get Project Id as UUID
		JsonNode json = request().body().asJson();
		String projectId = json.findPath("Id").textValue();

		UUID uuid = UUID.fromString(projectId);
		if (uuid == null) {
			return badRequest("Es wurde kein Projekt für diese Aktion gefunden.");
		}

		Project project = Project.getProject(uuid);
		if (project == null) {
			return badRequest("Das Projekt konnte nicht in der Datenbank gefunden werden.");
		}

		// check if the currently logged in user has the right to get the
		// project info
		if (!project.findUser(Logins.getLoggedInUser())) {
			return badRequest("Sie sind nicht berechtigt auf dieses Projekt zuzugreifen.");
		}

		// send informations of the project to the user interface
		try {
			return ok(project.toJSON().toString());
		} catch (JSONException e) {
			e.printStackTrace();

			// return empty json string
			return ok("Die Informationen konnten nicht abgerufen werden: "
					+ e.getMessage());
		}
	}

	/**
	 * /projects/deleteProjectrequires calls this. JSON with "Name": name of
	 * specific Project to be deleted. Deletes the Project.
	 * 
	 * @return Status with error or success message
	 */
	public static Result deleteProject() {
		if (!Logins.isLoggedIn())
			return redirect(routes.Logins.login());

		// get Project Id as UUID
		JsonNode json = request().body().asJson();
		String projectId = json.findPath("Id").textValue();
		UUID uuid = UUID.fromString(projectId);

		if (uuid == null) {
			return badRequest("Es wurde kein Projekt für diese Aktion gefunden.");
		}

		Project project = Project.getProject(uuid);

		// check if the currently logged in user has the right to delete the
		// project
		if (!project.findUser(Logins.getLoggedInUser())) {
			return badRequest("Sie sind nicht berechtigt dieses Projekt zu löschen.");
		}

		// remove Project if possible
		if (!Project.removeProject(uuid)) {
			return badRequest("Das Projekt konnte nicht in der Datenbank gefunden werden.");
		}

		return ok("Das Projekt wurde gelöscht.");
	}

	/**
	 * /projects/getAllUserNames calls this. Get all registered Users
	 * 
	 * @return error if no users exists or success and json with all users
	 */
	public static Result getAllUserNames() {
		if (!Logins.isLoggedIn())
			return redirect(routes.Logins.login());

		JSONStringer json = new JSONStringer();

		// call static toJSON method
		try {
			json = User.toJSON();

			return ok(json.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			// return empty json string
			return ok((new JSONStringer().toString()));
		}
	}

	/**
	 * /projects/editProject calls this. requires JSON with "Id": projectId
	 * "Users": [ {"Value": UserId, "selected": boolean}, ... ] "Properties" :
	 * [{"Value": PropertyName, ... ] to edit a new Project
	 * 
	 * @return Status with Success or Error message
	 */
	public static Result editProject() {
		if (!Logins.isLoggedIn())
			return redirect(routes.Logins.login());

		JsonNode json = request().body().asJson();

		if (json == null)
			return badRequest("Fehler beim Empfangen der Daten.");

		String projectId = json.findPath("Id").asText();

		UUID uuid = UUID.fromString(projectId);

		Project project = Project.getProject(uuid);

		// check if the currently logged in user has the right to edit the
		// project
		if (!project.findUser(Logins.getLoggedInUser())) {
			return badRequest("Sie sind nicht berechtigt dieses Projekt zu bearbeiten.");
		}

		try {
			setEditedValues(json, project);

		} catch (IOException e) {

			e.printStackTrace();
			return badRequest("Die Projekt-Eigenschaften konnten nicht geändert werden: "
					+ e.getMessage());
		} catch (IllegalArgumentException e) {

			e.printStackTrace();
			return badRequest(e.getMessage());
		}

		// return the project (as JSON) to show changes instant in view without
		try {
			return ok(project.toJSON().toString());

		} catch (JSONException e) {

			e.printStackTrace();
			return ok("Es ist ein Problem beim Ändern des Projekts aufgetreten: "
					+ e.getMessage());
		}
	}

	/**
	 * Helper method to edit a project with the values of the json string
	 * 
	 * @param json
	 *            - JsonNode object with the values to set in project
	 * @param project
	 *            - the project to edit
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	private static void setEditedValues(JsonNode json, Project project)
			throws JsonProcessingException, IOException,
			IllegalArgumentException {
		JsonNode node = new ObjectMapper().readTree(json.toString());

		// add/remove users from/to the project
		JsonNode arrNode = node.get("Users");

		if (arrNode.isArray()) {
			for (JsonNode objNode : arrNode) {
				int userId = objNode.get("Value").asInt();

				if (objNode.get("Selected").asBoolean()) {
					project.addUser(User.findById(userId));
				} else {
					// check if the current user has access to the project
					if (userId == Logins.getLoggedInUser().getId()) {
						throw new IllegalArgumentException(
								"Sie müssen sich zu den berechtigten Benutzern hinzufügen.");
					} else {
						project.removeUser(User.findById(objNode.get("Value")
								.asInt()));
					}
				}
			}
		}
		// add/remove properties from/to the project
		arrNode = node.get("Properties");
		if (arrNode.isArray()) {
			List<String> sendedProperties = new ArrayList<String>();

			for (JsonNode objNode : arrNode) {
				sendedProperties.add(objNode.get("Value").asText());
			}

			List<String> projectProperties = project
					.getPropertyTypesAsStrings();

			// copy sendedProperties to use to add new properties later
			List<String> allSendedProperties = new ArrayList<String>(
					sendedProperties);

			// sendedProperties contains the properties which stay unedited
			sendedProperties.retainAll(projectProperties);

			// projectProperties contains properties that should be deleted
			projectProperties.removeAll(sendedProperties);

			// remove properties
			for (String propertyName : projectProperties) {
				project.removePropertyType(project
						.getPropertyTypeByName(propertyName));
			}

			// allSendedProperties contains new properties
			allSendedProperties.removeAll(project.getPropertyTypesAsStrings());

			// add new properties
			for (String propertyName : allSendedProperties) {
				project.getPropertyTypeByName(propertyName);
			}
		}

		// set project name
		String projectName = node.get("Name").asText();
		project.setName(projectName);
	}

	/**
	 * /projects/add calls this. requires JSON with "Name": projectName "Users":
	 * [ UserId, UserId, ... ] "Properties" : [Property, Property, ...] to
	 * create a new Project
	 * 
	 * @return Status with Success or Error message
	 */
	public static Result addProject() {
		if (!Logins.isLoggedIn())
			return redirect(routes.Logins.login());

		// retrieve the asynchronous send JSON data
		JsonNode json = request().body().asJson();

		// check whether the body contains data
		if (json == null)
			return badRequest("Es wurden keine Daten übermittelt.");

		String projectName = json.findPath("Name").asText();

		// check whether the user does not chose a name
		if (projectName == null || projectName.isEmpty())
			return badRequest("Es wird ein Projektname benötigt.");

		// check if the name already exists
		if (Project.getProject(projectName) != null)
			return badRequest("Es ist bereits ein Projekt mit Namen \""
					+ projectName + "\" vorhanden.");

		Project newProject = Project.createProject(projectName);

		// try to extract JSON data and set the project values
		try {
			setValues(json, newProject);
		} catch (IOException e) {
			e.printStackTrace();
			return badRequest(e.getMessage());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return badRequest(e.getMessage());
		}

		String result = (new JSONStringer()).toString();

		try {
			result = newProject.toJSON().toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return badRequest("Das Projekt konnte nicht angezeigt werden.");
		}

		return ok(result);
	}

	/**
	 * Helper method to set the values of a project
	 * 
	 * @param json
	 *            - JsonNode object with the values to set in project
	 * @param project
	 *            - the project to edit
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	private static void setValues(JsonNode json, Project newProject)
			throws JsonProcessingException, IOException,
			IllegalArgumentException {
		JsonNode node = new ObjectMapper().readTree(json.toString());

		// select JSON node
		JsonNode arrNode = node.get("Users");

		if (arrNode.size() == 0) {
			throw new IllegalArgumentException(
					"Sie können kein Projekt mit 0 Benutzern erstellen.");
		}

		// get user IDs and add the respective users to project
		if (arrNode.isArray()) {
			for (JsonNode objNode : arrNode) {
				newProject.addUser(User.findById(objNode.asInt()));
			}
		}

		if (!newProject.getUsers().contains(Logins.getLoggedInUser())) {
			newProject.delete();
			throw new IllegalArgumentException(
					"Sie müssen sich zu den berechtigten Benutzern hinzufügen.");
		}

		// select next JSON node
		arrNode = node.get("Properties");

		// get property names and add them to the project
		if (arrNode.isArray()) {
			for (JsonNode objNode : arrNode) {
				System.out.println(objNode.get("Value").asText());
				newProject.getPropertyTypeByName(objNode.get("Value").asText());
			}
		}
	}

	/**
	 * /exportProject/[ID] calls this. Find the project by name and use the
	 * Export class in package 'utils' to create a ZIP-file and send it directly
	 * to the end-user.
	 * 
	 * @param projectName
	 * @return
	 * @throws IOException
	 */
	public static Result exportProject(String projectId) throws IOException {
		if (!Logins.isLoggedIn())
			return redirect(routes.Logins.login());

		// get Project Id as UUID
		UUID uuid = UUID.fromString(projectId);
		if (uuid == null) {
			return badRequest("Es konnte kein Projekt zum gegebenen Namen gefunden werden!");
		}

		// find project by uuid
		Project project = Project.getProject(uuid);
		if (project == null) {
			return badRequest("Es konnte kein Projekt zum gegebenen Namen gefunden werden!");
		}

		// check if the currently logged in user has the right to export the
		// project
		if (!project.findUser(Logins.getLoggedInUser())) {
			return badRequest("Sie sind nicht berechtigt dieses Projekt zu exportieren.");
		}

		// create ZIP-file
		File exportedZip = Export.exportProject(project);

		// send ZIP-file to the end-user
		response().setContentType("application/zip");
		response().setHeader(
				"Content-disposition",
				"attachment; filename="
						+ FilenameUtils.getBaseName(exportedZip.getName())
						+ ".zip");
		return ok(exportedZip);
	}
}