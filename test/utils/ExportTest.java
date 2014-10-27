package utils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import models.Code;
import models.Interview;
import models.Project;
import models.AudioFile;
import models.Statement;
import models.Time;
import static utils.Export.exportProject;

import java.io.*;

import static org.hamcrest.MatcherAssert.assertThat;

public class ExportTest extends models.BaseModelTest {
	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	private File audio1;
	private File audio2;
	private File audioFiles;

	@Before
	public void setupFiles() throws IOException {
		File in = temp.newFolder("in");
		audio1 = new File(in.getAbsolutePath() + "/audio1.mp3");
		audio1.createNewFile();
		audio2 = new File(in.getAbsolutePath() + "/audio2.mp3");
		audio2.createNewFile();
		audioFiles = temp.newFolder("audioFiles");
	}

	@Test
	public void testSimpleProject() throws IOException {
		Project testProject = Project.createProject("TestProject");
		Interview i1 = Interview.createInterview(testProject, "TestInterview1");
		i1.setProperty(testProject.getPropertyTypeByName("Language"), "English");
		i1.setProperty(testProject.getPropertyTypeByName("Age"), "18");
		i1.setProperty(testProject.getPropertyTypeByName("Course"), "SWP");
		i1.setAudio(new AudioFile(audioFiles, audio1));
		i1.addStatement(Statement.createStatement(i1, new Time(1, 13),
				"talking strange things", Code.getCodeByName("useless")));
		i1.addStatement(Statement.createStatement(i1, new Time(1, 33),
				"SWP is great", Code.getCodeByName("useless"),
				Code.getCodeByName("praise")));

		Interview i2 = Interview.createInterview(testProject, "TestInterview2");
		i2.setAudio(new AudioFile(audioFiles, audio2));
		try {
			File result = exportProject(testProject);
			System.out.println("Check the exported files at: "
					+ result.getAbsolutePath());
			assertThat("JSON export wrong", result.exists());
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
}
