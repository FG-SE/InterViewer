package models;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
//Please insert your new test-class below
@SuiteClasses({
	ProjectTest.class,
	PropertyTypeTest.class,
	DynamicPropertyTest.class,
	InterviewTest.class,
	CodeTest.class,
	StatementTest.class,
	AudioFileTest.class,
	TimeTest.class,
	UserTest.class
})

public class AllModelTests {
}
