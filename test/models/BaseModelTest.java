package models;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.avaje.ebean.Ebean;

import play.db.ebean.Model;
import play.test.FakeApplication;
import play.test.Helpers;
import testenvironment.TestData;

/*
 * As alternate to the old EbeanTest base class.
 * 
 * This is a superclass for each JUnit test using EBeans. Setup of fake application is done here.
 * This class refreshes database (before each test), too.
 * 
 * from http://blog.matthieuguillermin.fr/2012/03/unit-testing-tricks-for-play-2-0-and-ebean/
 * 
 * 
 * Note: 	JUnit won't work inside Eclipse because an EBean enhancer is missing... 
 * 			You need to download it (I used the one from: http://www.java2s.com/Code/JarDownload/ebean/ebean-2.8.1-agent.jar.zip)
 * 			and add the following to VM arguments of JUnit run configuration:
 * 				-javaagent:/path/to/ebean-2.8.1-agent.jar
 * 			
 * 			There should be several plugins for Eclipse to do this, too.
 * 			I think there is one by avaje.com that would work (not tested).
 */
public class BaseModelTest {
	public static FakeApplication app;
	public static String createDdl = "";
	public static String dropDdl = "";
	
	
	public static void refreshAll(Model... models){
		for(Model m:models){
			m.refresh();
		}
	}

	@BeforeClass
	public static void startApp() throws IOException {
		app = Helpers.fakeApplication(Helpers.inMemoryDatabase(), Helpers.fakeGlobal());
		Helpers.start(app);

		// Reading the evolution file
		String evolutionContent = FileUtils.readFileToString(
				app.getWrappedApplication().getFile("conf/evolutions/default/1.sql"));

		// Splitting the String to get Create & Drop DDL
		String[] splittedEvolutionContent = evolutionContent.split("# --- !Ups");
		String[] upsDowns = splittedEvolutionContent[1].split("# --- !Downs");
		createDdl = upsDowns[0];
		dropDdl = upsDowns[1];

	}

	@AfterClass
	public static void stopApp() {
		try{
			TestData.cleanUpTestData();
		} catch(Exception e) {}
		Helpers.stop(app);
	}

	@Before
	public void createCleanDb() {
		Ebean.execute(Ebean.createCallableSql(dropDdl));
		Ebean.execute(Ebean.createCallableSql(createDdl));
	}

}