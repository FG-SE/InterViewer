package testenvironment;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import models.AudioFile;
import models.Code;
import models.Interview;
import models.Project;
import models.Statement;
import models.Time;
import models.User;

public class TestData {
	/**
	 * Loads test data. Used test-data.yml as a template. It will fill in 4
	 * Users, 6 Times, 3 Codes, 2 AudioFiles, 1 PropertyType, 6 Statements, 2
	 * Dynamic Properties, 2 Interviews, 1 Project.
	 * @throws IOException 
	 */
	public static void fillInTestData() {
		new User("mariemuster@example.com", "marie", "super-password", true);

		Project marketing = Project.createProject("amazingNewMarketingProject");

		marketing.addUser(new User("tinamuster@example.com", "tina",
				"super-password", true));
		marketing.addUser(new User("hansmuster@example.com", "hans",
				"super-password", true));
		marketing.addUser(new User("maxmuster@example.com", "max",
				"super-password", false));

		Interview conversation1 = Interview.createInterview(marketing,
				"conversation1");
		Interview conversation2 = Interview.createInterview(marketing,
				"conversation2");

		conversation1.setProperty(marketing.getPropertyTypeByName("Datum"),
				"30.10.2013");
		conversation2.setProperty(marketing.getPropertyTypeByName("Datum"),
				"11.11.2013");
	
		try {
			conversation1.setAudio(new AudioFile(new File("audioFiles/"), File.createTempFile("audio", ".mp3")));
			conversation2.setAudio(new AudioFile(File.createTempFile("audio", ".mp3")));
		} catch (IOException e) {
			fail("Unable to create audiofiles for testing");
		}

		Statement.createStatement(conversation1, new Time(1, 52, 3), "Hallo?",
				Code.getCodeByName("Important"), Code.getCodeByName("Question"));

		Statement.createStatement(conversation1, new Time(2, 45), "Welt",
				Code.getCodeByName("Optional"));

		Statement.createStatement(conversation1, new Time(3, 0, 0),
				"Das ist Wichtig", Code.getCodeByName("Important"));

		Statement.createStatement(conversation2, new Time(4, 2335, 12),
				"Wirklich", Code.getCodeByName("Optional"));

		Statement.createStatement(conversation2, new Time(5, 1, 4), "Nein");

		Statement.createStatement(conversation2, new Time(6, 55, 55), "Ja");

	}
	
	public static void cleanUpTestData() {
		
		
		Project marketing = Project.getProject("amazingNewMarketingProject");
		
		for(User u :User.findAll()){
			u.delete();
		}
		
		for(Code c :Code.findAllCodes(marketing)){
			c.delete();
		}
		
		marketing.delete();
	}
}
