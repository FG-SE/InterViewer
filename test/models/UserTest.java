package models;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import utils.Password;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class UserTest extends BaseModelTest{
	
	private User u;
	
	@Before
	public void setUp() throws Exception {
		u = new User("admin@localhost", "admin", Password.hashPassword("mypass"), true);
		u.save();
		u.setSessionId("2014");
		new User("tester1@localhost", "tester1", Password.hashPassword("hispass"), false).save();
		new User("tester2@localhost", "tester2", Password.hashPassword("otherpass"), false).save();
		Project.createProject("MyProject").addUser(u);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFindAll() {
		List<User> all = User.findAll();
		assertThat(all.size(), is(3));
		List<String> expectedEmails = new ArrayList<String>();
		expectedEmails.add("admin@localhost");
		expectedEmails.add("tester1@localhost");
		expectedEmails.add("tester2@localhost");
		List<String> actualEmails = new ArrayList<String>();
		for (User u : all) {
			actualEmails.add(u.getEmail());
		}
		assertThat("wrong users...", actualEmails.containsAll(expectedEmails) && expectedEmails.containsAll(actualEmails));
	}

	@Test
	public void testFindByEmail() {
		assertThat(User.findByEmail("tester1@localhost").getAlias(), is("tester1"));
	}

	@Test
	public void testFindById() {
		assertThat(User.findById(u.getId()), is(u));
	}

	@Test
	public void testFindByAlias() {
		assertThat(User.findByEmail("tester2@localhost").getAlias(), is("tester2"));
	}

	@Test
	public void testDeleteLong() {
		User.delete(u.getId());
		assertThat("delete by id failed...", User.findById(u.getId()) == null);
		assertThat(User.findAll().size(), is(2));
	}

	@Test
	public void testAuthenticate() {
		assertThat("", User.authenticate("null@localhost", "pass") == null);
		assertThat("", User.authenticate("admin@localhost", "wrongpass") == null);
		assertThat("", User.authenticate("admin@localhost", "mypass") != null);
		assertThat(User.authenticate("admin@localhost", "mypass").getId(), is(u.getId()));
	}

	@Test
	public void testIsCorrectlyLoggedIn() {
		assertThat("", User.isCorrectlyLoggedIn("2014", "admin@localhost"));
	}
	
	@Test
	public void testGetProject() {
		assertThat(User.findByEmail(u.getEmail()).getProjects().size(), is(1));
		assertThat(User.findByEmail(u.getEmail()).getProjects().get(0).getName(), equalTo("MyProject"));
	}

}
