package models;

import org.junit.*;

import testenvironment.TestData;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import models.Code;

public class CodeTest extends BaseModelTest{
	
	@Before
	public void createBaseInterview() {
		Interview.createInterview(Project.createProject("TestProject"), "TestInterview");
	}

    
	@Test
	public void test_getCodeByName() {
		String name = "MyCode";
		Code testCode = Code.getCodeByName(name);
		assertThat("Constructor failed", testCode.getName().equals(name));
		testCode.delete();
	}
	
	@Test
	public void test_Uniqueness() {
		Code firstTestCode = Code.getCodeByName("UniqueCode");
		Code secondTestCode = Code.getCodeByName("UniqueCode");
		assertThat(secondTestCode, is(firstTestCode));
		firstTestCode.delete();
	}
	
	@Test
	public void testOccurrence() {
		TestData.fillInTestData();
		
		assertThat(Code.getCodeByName("Important").getOccurrence().size(),is(2));
	}

	@Test
	public void test_getName() {
		Code testCode = Code.getCodeByName("MyCode");
		assertThat(testCode.getName(),is("MyCode"));
		testCode.delete();
	}

	@Test
	public void test_findAllCodes() {
		TestData.fillInTestData();
		Project project = Project.createProject("XY");
		Interview interview = Interview.createInterview(project, "YZ");
		Statement statement = Statement.createStatement(interview, new Time(0,0), "ZA");
		statement.addCode(Code.getCodeByName("Optional"));
		statement.addCode(Code.getCodeByName("A"));
		statement.addCode(Code.getCodeByName("B"));
		statement.addCode(Code.getCodeByName("C"));
		statement.addCode(Code.getCodeByName("D"));
		statement.addCode(Code.getCodeByName("E"));
		project.refresh();
		assertThat(Code.findAllCodes(Project.getProject("amazingNewMarketingProject")).size(), is(3));
		assertThat(Code.findAllCodes(project).size(), is(6));
		

	}

	@Test
	public void test_getCode_using_id() {
		TestData.fillInTestData();
		UUID id  = Code.findAllCodes(Project.getProject("amazingNewMarketingProject")).get(1).getId();
		assertThat(Code.getCode(id), notNullValue());

	}

}
