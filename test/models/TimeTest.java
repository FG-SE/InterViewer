package models;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import testenvironment.TestData;

public class TimeTest extends BaseModelTest {

	@Before
	public void setUp() throws Exception {
		TestData.fillInTestData();
	}

	@After
	public void tearDownClass() throws Exception {
		TestData.cleanUpTestData();
	}

	@Test
	public void test_getTime() {
		List<Time> list = Time.findAllTimes();
		assertThat(Time.getTime(list.get(3).getId()), notNullValue());
		assertThat(Time.getTime(list.get(3).getId()), is(list.get(3)));
	}

	@Test
	public void test_save_and_findAllTimes() {
		Time time = new Time(1, 2, 3);

		time.save();
		List<Time> list = Time.findAllTimes();
		assertThat(list, notNullValue());
		// TestData will give 6 Time-Objects
		assertThat(list.size(), is(6 + 1));

		time.delete();
		list = Time.findAllTimes();
		assertThat(list, notNullValue());
		// TestData will give 6 Time-Objects
		assertThat(list.size(), is(6));
	}
	
	public void test_toMillis(){
		Time time = new Time(1,2,3);
		
		assertThat(time.toMillis(),is(60*1000*1+1000*2+3));
	}

	@Test
	public void test_TimeIntInt() {
		Time time = new Time(23, 45);
		assertThat(time.getMinutes(), is(23));
		assertThat(time.getSeconds(), is(45));
		assertThat(time.getMillis(), is(0));
	}

	@Test
	public void test_TimeIntIntInt() {
		Time time = new Time(22, 32, 32);
		assertThat(time.getMinutes(), is(22));
		assertThat(time.getSeconds(), is(32));
		assertThat(time.getMillis(), is(32));

		time = new Time(44, 36, 1050);
		assertThat(time.getMinutes(), is(44));
		assertThat(time.getSeconds(), is(37));
		assertThat(time.getMillis(), is(50));
		
		time = new Time(44, -36, -2);
		assertThat(time.getMinutes(), is(43));
		assertThat(time.getSeconds(), is(23));
		assertThat(time.getMillis(), is(998));
		// Nice Implementation Benedikt. Thats funny :-D
	}

	@Test
	public void test_toString() {
		Time time = new Time(1,1,14);
		assertThat(time.toString(),is("01:01"));
		
		time = new Time(23,10);
		assertThat(time.toString(),is("23:10"));
	}
	
	@Test
	public void test_TimeString() {
		Time t = new Time("23:10");
		assertThat(t.getMinutes(), is(23));
		assertThat(t.getSeconds(), is(10));
		assertThat(t.getMillis(), is(0));
		t = new Time("0:12");
		assertThat(t.toString(), equalTo("00:12"));
	}

}
