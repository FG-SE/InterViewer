package utils;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.util.List;

import org.json.JSONStringer;
import org.junit.Test;

import testenvironment.TestData;
import models.BaseModelTest;

public class SqlUtilsTest extends BaseModelTest {

	@Test
	public void test() {
		TestData.fillInTestData();

		List<String> keys = SqlUtils.directQueryKeys("select * from user");

		assertThat("should contain id", keys.get(0).contains("id"));

		List<List<String>> values = SqlUtils
				.directQueryValues("select * from user");

		assertThat(values.size(), is(4));
		assertThat(values.get(1).size(), is(6));

		for (List<String> row : values) {
			for (String s : row) {
				assertThat(s, notNullValue());
			}
		}

	}

	@Test
	public void test_json() {
		TestData.fillInTestData();

		JSONStringer json = SqlUtils
				.getDirectQueryMustacheJSON("select * from user");

		assertThat(json, notNullValue());
	}

}
