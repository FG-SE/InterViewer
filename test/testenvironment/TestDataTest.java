package testenvironment;

import java.io.IOException;
import java.util.List;



import java.util.UUID;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import models.AudioFile;
import models.BaseModelTest;
import models.Code;
import models.DynamicProperty;
import models.Interview;
import models.Project;
import models.PropertyType;
import models.Statement;
import models.Time;
import models.User;

import org.junit.*;

import play.db.ebean.Model.Finder;

/**
 * Methods to get test data are assembled in this class. Unfortunately they
 * don't work as they should. The fillInTestData-method provides a workaround.
 * 
 * @author Thiemo
 * 
 */
public class TestDataTest extends BaseModelTest {
	/**
	 * Test whether test data was properly loaded.
	 * @throws IOException
	 */
	@Test
	public void test_fillInTestData() {
		TestData.fillInTestData();

		// Project
		Project project = Project.getProject("amazingNewMarketingProject");
		assertThat(project, notNullValue());
		
		assertThat(new Finder<UUID, Project>(
				UUID.class, Project.class).findList().size(), is(1));
		
		// Users loaded?
		assertThat(User.findAll().size(), is(4));
		assertThat(project.getUsers().size(), is(3));
		assertThat(User.findByAlias("max"), notNullValue());
		assertThat(User.findByEmail("tinamuster@example.com"), notNullValue());
		
		assertThat(new Finder<UUID, User>(
				UUID.class, User.class).findList().size(), is(4));

		// Times loaded?
		List<Time> listOfTimes = Time.findAllTimes();
		assertThat(listOfTimes.size(), is(6));
		assertThat(Time.getTime(listOfTimes.get(2).getId()), notNullValue());
		
		assertThat(new Finder<UUID, Time>(
				UUID.class, Time.class).findList().size(), is(6));
		
		// Property types
		assertThat(project.getPropertyTypes().size(), is(1));
		assertThat(project.getPropertyTypes().get(0).getName(), equalTo("Datum"));
		
		assertThat(new Finder<UUID, PropertyType>(
				UUID.class, PropertyType.class).findList().size(), is(1));
		
		// Interviews
		assertThat(project.getInterviews().size(), is(2));
		Interview interview1 = Interview.getInterview(project, "conversation1");
		Interview interview2 = Interview.getInterview(project, "conversation2");
		assertThat(interview1,notNullValue());
		assertThat(interview2,notNullValue());
		
		assertThat(new Finder<UUID, Interview>(
				UUID.class, Interview.class).findList().size(), is(2));
		
		// Dynamic Propertys
		assertThat(new Finder<UUID, DynamicProperty>(
				UUID.class, DynamicProperty.class).findList().size(), is(2));
		
		
		// Statements
		assertThat(new Finder<UUID, Statement>(
				UUID.class, Statement.class).findList().size(), is(6));
		
		// AudioFiles
		assertThat(new Finder<UUID, AudioFile>(
				UUID.class, AudioFile.class).findList().size(), is(2));
		
		// Codes
		assertThat(new Finder<UUID, Code>(
				UUID.class, Code.class).findList().size(), is(3));
		
	}
	
	/**
	 * Test whether test data was properly loaded.
	 * @throws IOException
	 */
	@Test
	public void test_cleanUpInTestData() {
		TestData.fillInTestData();
		TestData.cleanUpTestData();

		// Project
		assertThat(new Finder<UUID, Project>(
				UUID.class, Project.class).findList().size(), is(0));
		
		// Users deleted?
		assertThat(new Finder<UUID, User>(
				UUID.class, User.class).findList().size(), is(0));

		// Times deleted?
		assertThat(new Finder<UUID, Time>(
				UUID.class, Time.class).findList().size(), is(0));
		
		// Property types
		assertThat(new Finder<UUID, PropertyType>(
				UUID.class, PropertyType.class).findList().size(), is(0));
		
		// Interviews
		assertThat(new Finder<UUID, Interview>(
				UUID.class, Interview.class).findList().size(), is(0));
		
		// Dynamic Propertys
		assertThat(new Finder<UUID, DynamicProperty>(
				UUID.class, DynamicProperty.class).findList().size(), is(0));
		
		// Statements
		assertThat(new Finder<UUID, Statement>(
				UUID.class, Statement.class).findList().size(), is(0));
		
		// AudioFiles
		assertThat(new Finder<UUID, AudioFile>(
				UUID.class, AudioFile.class).findList().size(), is(0));
		
		// Codes
		assertThat(new Finder<UUID, Code>(
				UUID.class, Code.class).findList().size(), is(0));
	}

	
}
