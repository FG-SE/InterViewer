package models;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.PersistenceException;

import models.Project;
import models.Interview;

import org.junit.*;

import play.db.ebean.Model;

public class ProjectTest extends BaseModelTest {
	
	private Project firstproject;
	private PropertyType propertyType;

	@Before
	public void setUp() throws Exception {
		
	}
	
	@Before
	public void initialize() {
		//for old tests
		Project.createProject("Programming-Language");
		Project project = Project.getProject("Programming-Language");
		Interview.createInterview(project, "Klaus Kunze Interview");
		//for new tests
		firstproject = Project.createProject("FirstProject");
		Project.createProject("SecondProject");
		firstproject.addUser(new User("tester1@localhost", "tester1", "", false));
		propertyType = firstproject.getPropertyTypeByName("Language");
		firstproject.getPropertyTypeByName("Date");
		Interview.createInterview(firstproject, "ExistingInterview").setProperty(propertyType, "German");
	}

	@Test
	public void test_getName() {
		Project project = Project.getProject("Programming-Language");
		refreshAll(project);
		assertThat(project.getName(), equalTo("Programming-Language"));
	}
	
	@Test
	public void test_setName() {
		Project project = Project.getProject("Programming-Language");
		try{
			project.setName(null);
			fail();
		} catch (IllegalArgumentException e) {}
		try{
			project.setName("");
			fail();
		} catch (IllegalArgumentException e) {}
		refreshAll(project);
		assertThat(project.getName(),equalTo("Programming-Language"));
		project.setName("Language");
		assertThat(project.getName(),equalTo("Language"));
	}

	@Test
	public void test_adding_propertyType() {
		Project project = Project.getProject("Programming-Language");
		PropertyType propTypeA = project.getPropertyTypeByName("JUnit");
		PropertyType propTypeB = project.getPropertyTypeByName("JUnit");
		refreshAll(project,propTypeA,propTypeB);
		assertThat(propTypeA, is(propTypeB));
		assertThat(propTypeA, notNullValue());

	}

	@Test(expected = IllegalArgumentException.class)
	public void test_adding_empty_propertyType() {
		Project project = Project.getProject("Programming-Language");
		refreshAll(project);
		project.getPropertyTypeByName("");
	}

	@Test
	public void test_addition_and_removal_of_Interviews() {
		Project project = Project.getProject("Programming-Language");
		Interview maxMustermannInterview = Interview.createInterview(project,
				"Max Mustermann Interview");
		//adding of interview should propagate without refresh
		assertThat(project.getInterviews().contains(maxMustermannInterview),
				is(true));
		assertThat(maxMustermannInterview.getProject().getId(),is(project.getId()));
		
		Interview.removeInterview(maxMustermannInterview);
		project = Project.getProject("Programming-Language");
		//deletion of interview should propagate without refresh
		assertThat(project.getInterviews().contains(maxMustermannInterview),
				is(false));
		
		try{
			maxMustermannInterview.refresh();
			fail("exception expected"); 
		} catch(PersistenceException e){
		} catch(Exception e){
			fail("wrong exception occured");
		}	
	}

	@Test
	public void test_checkIfInterviewNameExists() {
		Project project = Project.getProject("Programming-Language");
		assertThat(
				project.checkIfInterviewNameExists("Hans Mustermann Interview"),
				is(false));
		Interview hansMustermannInterview = Interview.createInterview(project,
				"Hans Mustermann Interview");
		//adding of interview should propagate without refresh
		assertThat(
				project.checkIfInterviewNameExists("Hans Mustermann Interview"),
				is(true));
		Interview.removeInterview(hansMustermannInterview);
		
		refreshAll(project);
		assertThat(
				project.checkIfInterviewNameExists("Hans Mustermann Interview"),
				is(false));
	}

	@Test
	public void testGetProjectString() {
		assertThat(Project.getProject("FirstProject").getId(), equalTo(firstproject.getId()));
		assertThat(Project.getProject("SecondProject").getId(), not(equalTo(firstproject.getId())));
		assertThat(Project.getProject("NULL"), nullValue());
	}

	@Test
	public void testGetProjectUUID() {
		assertThat(Project.getProject(firstproject.getId()).getName(), equalTo("FirstProject"));
		assertThat(Project.getProject(new UUID(0, 0)), nullValue());
	}

	@Test
	public void testFindAllProjects() {
		List<Project> all = Project.findAllProjects();
		assertThat(all.size(), is(3));
		List<String> expected = new ArrayList<String>();
		expected.add("FirstProject");
		expected.add("SecondProject");
		List<String> actual = new ArrayList<String>();
		for (Project p : all) {
			actual.add(p.getName());
		}
		assertThat("", actual.containsAll(expected));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateProjectSameName() {
		Project.createProject("FirstProject");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateProjectEmptyName() {
		Project.createProject("");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateProjectNullName() {
		Project.createProject(null);
	}

	@Test
	public void testRemoveProjectString() {
		assertThat(Project.removeProject("FirstProject"), is(true));
		//project deleted
		assertThat(Project.getProject("FirstProject"), nullValue());
		assertThat(Project.findAllProjects().size(), is(2));
	}
	
	@Test
	public void testRemoveProjectNotExisting() {
		assertThat(Project.removeProject("azudkdfhjd"),is(false));	
	}

	@Test
	public void testRemoveProjectUUID() {
		Project.removeProject(firstproject.getId());
		assertThat(Project.getProject(firstproject.getId()), nullValue());
		assertThat(Project.findAllProjects().size(), is(2));
	}

	@Test
	public void testAddGetUsers() {
		User user = new User("email@example.de", "myname", "", false);
		Project.getProject("SecondProject").addUser(user);
		Project.getProject("SecondProject").addUser(user);
		List<User> result = Project.getProject("SecondProject").getUsers();
		assertThat(result.size(), is(1));
		assertThat(result.get(0).getAlias(), equalTo("myname"));
	}

	@Test
	public void testFindUser() {
		assertThat(Project.getProject("FirstProject").findUser(User.findByEmail("tester1@localhost")), is(true));
		assertThat(Project.getProject("SecondProject").findUser(User.findByEmail("tester1@localhost")), is(false));
	}

	@Test
	public void testGetPropertyTypeByName() {
		assertThat(Project.getProject("FirstProject").getPropertyTypeByName("Language"), is(propertyType));
		assertThat(Project.getProject("FirstProject").getPropertyTypeByName("OS"), not(is(propertyType)));
		assertThat(Project.getProject("FirstProject").getPropertyTypes().size(), is(3));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetPropertyTypeByNameException1() {
		Project.getProject("FirstProject").getPropertyTypeByName("");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetPropertyTypeByNameException2() {
		Project.getProject("FirstProject").getPropertyTypeByName(null);
	}

	@Test
	public void testGetPropertyTypes() {
		assertThat(Project.getProject("FirstProject").getPropertyTypes().size(), is(2));
		List<String> expected = new ArrayList<String>();
		expected.add("Language");
		expected.add("Date");
		List<String> actual = new ArrayList<String>();
		for (PropertyType type : Project.getProject("FirstProject").getPropertyTypes()) {
			actual.add(type.getName());
		}
		assertThat("", actual.containsAll(expected) && expected.containsAll(actual));
	}

	@Test
	public void testCheckIfInterviewNameExists() {
		assertThat(Project.getProject("FirstProject").checkIfInterviewNameExists("ExistingInterview"), is(true));
		assertThat(Project.getProject("FirstProject").checkIfInterviewNameExists("NonExistingInterview"), is(false));
		assertThat(Project.getProject("SecondProject").checkIfInterviewNameExists("ExistingInterview"), is(false));
	}

	@Test
	public void testGetInterviews() {
		assertThat(Project.getProject("FirstProject").getInterviews().size(), is(1));
		assertThat(Project.getProject("FirstProject").getInterviews().get(0).getName(), equalTo("ExistingInterview"));
	}
	
	@Test
	public void testRemovePropertyType() {
		Project.getProject("FirstProject").removePropertyType(firstproject.getPropertyTypeByName("Language"));
		Model.Finder<UUID, PropertyType> finder = new Model.Finder<UUID, PropertyType>(UUID.class, PropertyType.class);
		assertThat(finder.findList().size(), is(1));
		assertThat(finder.findList().get(0).getName(), equalTo("Date"));
	}
	
	@Test
	public void testGetPropertyTypesAsStrings() {
		List<String> actual = Project.getProject("FirstProject").getPropertyTypesAsStrings();
		assertThat(actual.size(), is(2));
		List<String> expected = new ArrayList<String>();
		expected.add("Language");
		expected.add("Date");
		assertThat("", expected.containsAll(actual) && actual.containsAll(expected));
	}

}
