package models;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import org.junit.*;

import models.PropertyType;

public class PropertyTypeTest extends BaseModelTest {
	
	PropertyType type;

	@Before
	public void initialize(){
		this.type = new PropertyType("JUnit");
	}
	
	@Test
	public void test_equalsObject() {
		assertThat(this.type, equalTo(this.type));
		assertThat(this.type, equalTo(new PropertyType("JUnit")));
		assertThat(this.type, not(equalTo(new Object())));
		assertThat(this.type, not(equalTo(new PropertyType("Date"))));
	}

	@Test
	public void test_propertyType() {
		assertThat(this.type.getName(), equalTo("JUnit"));
	}

}
