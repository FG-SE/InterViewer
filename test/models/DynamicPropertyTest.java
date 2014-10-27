package models;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import models.Project;

import org.junit.*;

public class DynamicPropertyTest extends BaseModelTest {
	
	private Project dummyProject;
	private PropertyType junitType;
	
	@Before
	public void initialize(){
		dummyProject = Project.createProject("DummyProject");
		junitType = dummyProject.getPropertyTypeByName("JUnit");
	}
	
	@Test
	public void test_DynamicProperty() {
		DynamicProperty dynamicProperty = new DynamicProperty(junitType, "competent knowledge of junit");
		assertThat(dynamicProperty, is(not(nullValue())));
		assertThat(dynamicProperty.getValue(), equalTo("competent knowledge of junit"));
	}
	
	@Test
	public void test_default_value(){
		DynamicProperty dynamicProperty = new DynamicProperty(junitType);
		assertThat(dynamicProperty.getValue(), equalTo(""));
	}
}
