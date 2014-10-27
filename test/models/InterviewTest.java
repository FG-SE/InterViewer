package models;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.util.ArrayList;
import java.util.List;

import models.Interview;

import org.json.*;
import org.junit.*;

public class InterviewTest extends BaseModelTest {

	/* those are some old tests */

	private Project project;

	@Before
	public void initialize() {
		project = Project.createProject("Android-Apps");
		Interview.createInterview(project, "Interview");
		Interview.createInterview(project, "AN01");
		Interview iv = Interview.createInterview(project, "MyInterview");
		Statement.createStatement(iv, new Time(0, 0, 0), "greeting");
		iv.setProperty(project.getPropertyTypeByName("Language"), "German");
		iv.refresh();
		project.refresh();
	}

	@Test(expected = RuntimeException.class)
	public void test_adding_Interviews_with_equal_name() {
		Interview.createInterview(project, "Interview");
	}

	@Test
	public void test_if_interviews_in_db() {
		assertThat("database failed",
				Interview.getInterview(project, "Interview") != null);
		assertThat("database failed",
				Interview.getInterview(project, "AN01") != null);
		assertThat("database failed",
				Interview.getInterview(project, "MyInterview") != null);
		assertThat("database failed",
				Interview.getInterview(project, "NonExistentInterview") == null);
		assertThat("database failed", Interview.findAllInterviews().size() == 6);
	}

	@Test
	public void test_Interview() {
		Interview interview = Interview.getInterview(project, "AN01");
		assertThat(interview, is(not(nullValue())));
		assertThat(project.checkIfInterviewNameExists("AN01"), is(true));
	}

	@Test
	public void test_set_value_of_dynamic_propertyType() {
		Interview interview = Interview.getInterview(project, "AN01");
		PropertyType type = project
				.getPropertyTypeByName("Programming-Knowledge");
		assertThat("", type != null);
		interview.getProperty(type);
		interview.setProperty(type, "Very Good!");
		assertThat(interview.getProperty(type), equalTo("Very Good!"));
	}

	@Test
	public void test_set_existing_dynamicPropertyType() {
		Interview interview = Interview.getInterview(project, "AN01");
		PropertyType type = project
				.getPropertyTypeByName("Programming-Knowledge");
		interview.setProperty(type, "Very good!");
		interview.setProperty(type, "Not so good!");
		assertThat(interview.getProperty(type), equalTo("Not so good!"));
	}

	@Test
	public void test_db_persistence() {
		Interview iv = Interview.getInterview(project, "MyInterview");
		assertThat(Statement.findAllStatements(iv, new Time(0, 0, 0), new Time(
				0, 0, 0)), notNullValue());
		assertThat("Statement not persistent...", iv.getStatements().size() > 0);

		assertThat(project.getPropertyTypeByName("Language").getName(),
				is("Language"));

		assertThat("Property not persistent... got \"\"",
				!iv.getProperty(project.getPropertyTypeByName("Language"))
						.equals(""));
		assertThat("Property not persistent",
				iv.getProperty(project.getPropertyTypeByName("Language"))
						.equals("German"));
	}

	@Test
	public void test_db_deletion() {
		List<Statement> s1 = Statement.findAllStatements(Interview
				.getInterview(project, "MyInterview"), new Time(0, 0, 0),
				new Time(0, 0, 0));
		assertThat("Statement not persistent...", s1.size() == 1);
		Interview.getInterview(project, "MyInterview").delete();
		List<Statement> s2 = Statement.findAllStatements(Interview
				.getInterview(project, "MyInterview"), new Time(0, 0, 0),
				new Time(0, 0, 0));
		assertThat("Didn't get rid of statement...", s2.size() == 0);
	}

	@Test
	public void test_to_JSON() {
		JSONStringer json = Interview.getInterview(project, "MyInterview")
				.toJSON();
		String result = json.toString();
		assertThat(result, containsString("\"interviewName\":\"MyInterview\""));
	}

	/* here are the new tests */
	private Interview i;

	@Before
	public void setUp() throws Exception {
		Project.createProject("MyProject");
		i = Interview.createInterview(Project.getProject("MyProject"),
				"MyInterview");
		Interview.createInterview(Project.getProject("MyProject"),
				"SecondInterview");
		Interview.createInterview(Project.getProject("MyProject"),
				"ThirdInterview");
	}

	@Test
	public void testGetInterviewProjectString() {
		assertThat(Interview.getInterview(Project.getProject("MyProject"),
				"MyInterview"), notNullValue());
		assertThat(Interview.getInterview(Project.getProject("MyProject"),
				"MyInterview"), is(i));
		assertThat(
				Interview.getInterview(Project.getProject("MyProject"), "NULL"),
				nullValue());
	}

	@Test
	public void testGetInterviewUUID() {
		assertThat(Interview.getInterview(i.getId()), is(i));
	}

	@Test
	public void testFindAllInterviewsProject() {
		assertThat(Interview.findAllInterviews(Project.getProject("MyProject"))
				.size(), is(3));
		List<String> expected = new ArrayList<String>();
		expected.add("MyInterview");
		expected.add("SecondInterview");
		expected.add("ThirdInterview");
		List<String> actual = new ArrayList<String>();
		for (Interview i : Interview.findAllInterviews(Project
				.getProject("MyProject"))) {
			actual.add(i.getName());
		}
		assertThat("",
				expected.containsAll(actual) && actual.containsAll(expected));
	}

	@Test
	public void testFindAllInterviews() {
		assertThat(Interview.findAllInterviews().size(), is(6));
	}

	@Test
	public void testRemoveInterview() {
		Interview.removeInterview(Interview.getInterview(i.getId()));
		assertThat(Interview.findAllInterviews(Project.getProject("MyProject"))
				.size(), is(2));
		List<String> expected = new ArrayList<String>();
		expected.add("SecondInterview");
		expected.add("ThirdInterview");
		List<String> actual = new ArrayList<String>();
		for (Interview i : Interview.findAllInterviews(Project
				.getProject("MyProject"))) {
			actual.add(i.getName());
		}
		assertThat("",
				expected.containsAll(actual) && actual.containsAll(expected));
	}

	@Test
	public void testGetProject() {
		assertThat(Interview.getInterview(i.getId()).getProject().getName(),
				equalTo("MyProject"));
	}

	@Test
	public void testGetStatements() {
		Statement.createStatement(Interview.getInterview(i.getId()), new Time("00:00"), "test");
		i.refresh();
		assertThat(i.getStatements().get(0).getDescription(), equalTo("test"));
	}

	@Test
	public void testGetSetProperty() {
		PropertyType language = i.getProject()
				.getPropertyTypeByName("Language");
		i.setProperty(language, "German");

		i.refresh();
		language.refresh();

		assertThat(i.getProperty(language), equalTo("German"));
	}
}
