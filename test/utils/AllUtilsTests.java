package utils;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
//Please insert your new test-class below
@SuiteClasses({
	Export.class,
	SqlUtils.class
})

public class AllUtilsTests {
}
