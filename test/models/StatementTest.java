package models;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import testenvironment.TestData;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class StatementTest extends BaseModelTest{
	
	/* those are some old tests */
	
	private Interview baseIV;
	
	@Before
	public void createBaseInterview() {
		baseIV = Interview.createInterview(Project.createProject("TestProject"), "TestInterview");
	}
	
	
	@Test
	public void test_createStatement_Time() {
		Statement testS = Statement.createStatement(baseIV,new Time(3, 2, 1), "important statement!", new Code[]{});
		assertThat("Minute has wrong value: "+testS.getTime().getMinutes(), testS.getTime().getMinutes()==3);
		assertThat("Second has wrong value: "+testS.getTime().getSeconds(), testS.getTime().getSeconds()==2);
		assertThat("Millisecond has wrong value: "+testS.getTime().getMillis(), testS.getTime().getMillis()==1);
	}
	
	@Test
	public void test_createStatement_Description() {
		Statement testS = Statement.createStatement(baseIV, new Time(0, 0, 0), "fancy statement!", new Code[]{});
		assertThat("setting/getting description failed", testS.getDescription().equals("fancy statement!"));
	}
	
	@Test
	public void test_createStatement_Code() {
		Code code1 = Code.getCodeByName("firstTestCode");
		Code code2 = Code.getCodeByName("secondTestCode");
		Statement testS1 = Statement.createStatement(baseIV, new Time(0, 0, 0), "boring statement!", new Code[]{code1});
		Statement testS2 = Statement.createStatement(baseIV, new Time(0, 0, 0), "even more boring statement!", new Code[]{code1, code2});
		assertThat("setting or fetching code failed", testS1.getCodes().contains(code1));
		assertThat("setting or fetching code failed", (testS2.getCodes().contains(code2)) && (testS2.getCodes().contains(code1)));
	}

	@Test
	public void test_findAllStatements_byProjectCode(){
		TestData.fillInTestData();
		
		Project project = Project.getProject("amazingNewMarketingProject");
		assertThat(project, notNullValue());
		
		List<Statement> list = Statement.findAllStatements(project, Code.getCodeByName("Important"));
		assertThat(list.size(), is(2));
	}
	
	@Test
	public void test_findAllStatements_byTimeBounds() {
		Statement s1 = Statement.createStatement(baseIV,new Time(1, 2, 0), "statement 1!");
		Statement s2 = Statement.createStatement(baseIV,new Time(1, 35, 13), "statement 2!");
		Statement s3 = Statement.createStatement(baseIV,new Time(2, 45, 44), "statement 3!");
		Statement s4 = Statement.createStatement(baseIV,new Time(13, 4, 0), "statement 4!");
		Statement s5 = Statement.createStatement(baseIV,new Time(25, 53, 1), "statement 5!");
		
		List<Statement> list = Statement.findAllStatements(baseIV, new Time(1,35,13), new Time(13,4,0));
		assertThat(list.size(), is(3));
		assertThat("should not contain s1",!list.contains(s1));
		assertThat("should contain s2",list.contains(s2));
		assertThat("should contain s3",list.contains(s3));
		assertThat("should contain s4",list.contains(s4));
		assertThat("should not contain s5",!list.contains(s5));
		
		list = Statement.findAllStatements(baseIV, new Time(1, 35, 13), new Time(1, 35, 13));
		assertThat("should contain s2",list.contains(s2));
	}

	@Test
	public void test_getId_and_getStatement() {
		Statement s1 = Statement.createStatement(baseIV,new Time(1, 2, 0), "test");
		Statement s2 = Statement.getStatement(s1.getId());
		assertThat(s1, is(s2));
	}
	
	@Test
	public void test_setDescription() {
		Statement s1 = Statement.createStatement(baseIV,new Time(1, 2, 0), "statement 1!");
		s1.setDescription("new description");
		
		s1.refresh();
		
		assertThat(s1.getDescription(), equalTo("new description"));
	}

	@Test
	public void test_setTime() {
		Statement s1 = Statement.createStatement(baseIV,new Time(1, 2, 0), "statement 1!");
		s1.setTime(new Time(2,30));
		
		Statement s1New = Statement.getStatement(s1.getId());
		
		assertThat(s1New.getTime().toMillis(), is( new Time(2,30).toMillis()));
	}

	@Test
	public void test_manage_Code() {
		Statement s1 = Statement.createStatement(baseIV,new Time(24, 44, 0), "statement");
		
		s1.addCode(Code.getCodeByName("JDK"));
		s1.addCode(Code.getCodeByName("JDK"));
		s1.addCode(Code.getCodeByName("JRE"));
		s1.refresh();
		
		assertThat(s1.getCodes().size(), is(2));
		
		s1.removeCode(Code.getCodeByName("JDK"));
		s1.refresh();
		
		assertThat(s1.getCodes().size(), is(1));
		assertThat("should not contain JDK", !s1.getCodes().contains(Code.getCodeByName("JDK")));
		
		s1.addCode(Code.getCodeByName("JUNIT"));
		s1.removeAllCodes();
		s1.refresh();
		
		assertThat(s1.getCodes().size(), is(0));
	}
	
	/* here are the new tests */
	Statement s;
	
	@Before
	public void setUp() {
		Project p = Project.createProject("MyProject");
		Interview i = Interview.createInterview(p, "MyInterview");
		Statement.createStatement(i, new Time("05:03"), "blubb");
		Statement.createStatement(i, new Time("03:00"), "test", Code.getCodeByName("A"), Code.getCodeByName("B"));
		s = Statement.createStatement(i, new Time(1, 5), "hello", Code.getCodeByName("A"));
	}

	@Test
	public void testFindAllStatementsProjectCode() {
		List<Statement> result = Statement.findAllStatements(Project.getProject("MyProject"), Code.getCodeByName("A"));
		assertThat(result.size(), is(2));
		List<String> expected = new ArrayList<String>();
		expected.add("test");
		expected.add("hello");
		List<String> actual = new ArrayList<String>();
		for (Statement s : result) {
			actual.add(s.getDescription());
		}
		assertThat("", actual.containsAll(expected));
		assertThat("", expected.containsAll(actual));
	}

	@Test
	public void testGetStatement() {
		assertThat(Statement.getStatement(s.getId()).getDescription(), is("hello"));
	}

	@Test
	public void testGetInterview() {
		assertThat(Statement.getStatement(s.getId()).getInterview().getName(), equalTo("MyInterview"));
	}

	@Test
	public void testGetSetTime() {
		assertThat(s.getTime().toString(), equalTo("01:05"));
		s.setTime(new Time("00:03"));
		assertThat(s.getTime().toString(), equalTo("00:03"));
	}

	@Test
	public void testGetCodes() {
		List<Code> result = Statement.findAllStatements(
				Interview.getInterview(Project.getProject("MyProject"), "MyInterview"), new Time("03:00"), new Time("03:00")).get(0).getCodes();
		List<Code> expected = new ArrayList<Code>();
		expected.add(Code.getCodeByName("A"));
		expected.add(Code.getCodeByName("B"));
		assertThat("", result.containsAll(expected) && expected.containsAll(result));
	}

	@Test
	public void testAddCode() {
		Statement tmp = Statement.findAllStatements(
				Interview.getInterview(Project.getProject("MyProject"), "MyInterview"), new Time("03:00"), new Time("03:00")).get(0);
		tmp.addCode(Code.getCodeByName("X"));
		List<Code> result = Statement.findAllStatements(
				Interview.getInterview(Project.getProject("MyProject"), "MyInterview"), new Time("03:00"), new Time("03:00")).get(0).getCodes();
		assertThat("", result.contains(Code.getCodeByName("X")));
	}

	@Test
	public void testRemoveCode() {
		Statement.findAllStatements(
				Interview.getInterview(Project.getProject("MyProject"), "MyInterview"), new Time("03:00"), new Time("03:00"))
				.get(0).removeCode(Code.getCodeByName("A"));
		List<Code> result = Statement.findAllStatements(
				Interview.getInterview(Project.getProject("MyProject"), "MyInterview"), new Time("03:00"), new Time("03:00")).get(0).getCodes();
		assertThat("", !result.contains(Code.getCodeByName("A")));
		s.refresh();
		assertThat("", s.getCodes().contains(Code.getCodeByName("A")));
	}

	@Test
	public void testRemoveAllCodes() {
		Statement.findAllStatements(
				Interview.getInterview(Project.getProject("MyProject"), "MyInterview"), new Time("03:00"), new Time("03:00"))
				.get(0).removeAllCodes();
		List<Code> result = Statement.findAllStatements(
				Interview.getInterview(Project.getProject("MyProject"), "MyInterview"), new Time("03:00"), new Time("03:00")).get(0).getCodes();
		assertThat(result.size(), is(0));
		assertThat(Code.getCodeByName("B").getOccurrence().size(), is(0));
		assertThat(Code.getCodeByName("A").getOccurrence().size(), is(1));
	}
}
