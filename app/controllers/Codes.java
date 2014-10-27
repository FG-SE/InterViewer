package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;
import models.Code;
import models.Project;
import models.Statement;

/**
 * The Codes Controller manages the request to the Codes-View
 * (/codes?projectId=[ID])
 */
public class Codes extends Controller {

	/**
	 * @return returns the rendered codes view. All Codes of the Project with
	 *         the given ID will be shown.
	 */
	public static Result index() throws IOException {
		if (!Logins.isLoggedIn())
			return redirect(routes.Logins.login());

		DynamicForm form = Form.form().bindFromRequest();
		Project project = Project.getProject(UUID.fromString(form
				.get("projectId")));
		
		// check if the user is qualified for watching this codes view
		if (!project.findUser(Logins.getLoggedInUser())) {
			return badRequest("Sie sind nicht berechtigt die Codes dieses Projekts zu betrachten!");
		}

		List<Code> codeslist = Code.findAllCodes(project);
		List<List<Statement>> statements = new ArrayList<List<Statement>>();
		for (Code c : codeslist) {
			statements.add(Statement.findAllStatements(project, c));
		}

		return ok(codes.render(project, codeslist, statements));
	}
}
