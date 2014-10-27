package models;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.io.IOException;

import models.AudioFile;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.avaje.ebean.Ebean;

public class AudioFileTest extends BaseModelTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private File audio1;
	private File audio2;
	private File audio3;
	private File targetFolder;
	private File anotherFolder;
	private File anotherFolder2;

	@Before
	public void initialize() throws IOException {

		// setup folders
		targetFolder = folder.newFolder("audioFilesForTesting");
		anotherFolder = folder.newFolder("anotherFolder");
		anotherFolder2 = folder.newFolder("anotherFolder2");

		// some test files. audio2 and audio3 will have the same name
		audio1 = File.createTempFile("audio", ".mp3");
		audio2 = new File(anotherFolder.getAbsolutePath() + "/double.mp3");
		audio2.createNewFile();
		audio3 = new File(anotherFolder2.getAbsolutePath() + "/double.mp3");
		audio3.createNewFile();

	}

	@Test
	public void test_AudioFile_creation() throws IOException {
		// build object
		AudioFile audioFile = new AudioFile(targetFolder, audio1);

		// check if everything gone right
		assertThat(targetFolder.list().length, is(1));
		assertThat(targetFolder.listFiles()[0].getAbsolutePath(),
				is(equalTo(audioFile.getPath())));
	}

	@Test
	public void test_getFile() throws IOException {
		AudioFile audioFile = new AudioFile(targetFolder, audio1);
		File response = audioFile.getFile();
		assertThat(response, is(equalTo(new File(audioFile.getPath()))));
	}

	@Test
	public void test_DefaultFolder() throws IOException {
		AudioFile audioFile = new AudioFile(audio1);

		// This will normally be managed by the Interview-class
		Ebean.save(audioFile);

		assertThat("file is missing", audioFile.getFile().exists());
		assertThat("file not in default directory but in "
				+ audioFile.getFile().getAbsolutePath(), audioFile.getFile()
				.getAbsolutePath().contains("audioFiles"));

		audioFile.delete();
	}

	@Test
	public void test_two_files_with_same_name() throws IOException {
		AudioFile audioFile1 = new AudioFile(targetFolder, audio2);
		AudioFile audioFile2 = new AudioFile(targetFolder, audio3);
		assertThat(
				"both files were copied to the same path",
				!audioFile1.getFile().getAbsolutePath()
						.equals(audioFile2.getFile().getAbsolutePath()));
	}

	@Test
	public void test_delete() throws IOException {
		AudioFile audioFile = new AudioFile(audio1);
		File file = audioFile.getFile();
		assertThat("file wasn't created", file.exists());
		
		//Normally this will be managed by the Interview
		audioFile.save();
		
		audioFile.delete();
		assertThat("file wasn't created", !file.exists());
	}

	@Test
	public void test_getPath() throws IOException {
		AudioFile audioFile = new AudioFile(targetFolder, audio1);
		
		assertThat(audioFile.getFile().getAbsolutePath(), is(audioFile.getPath()));
	}

	@Test
	public void test_getName() throws IOException {
		AudioFile audioFile = new AudioFile(targetFolder, audio2);
		assertThat(audioFile.getName(), is("double.mp3"));
	}

	@Test
	public void test_setName() throws IOException {
		AudioFile audioFile = new AudioFile(targetFolder, audio1);
		audioFile.setName("Beethoven");
		assertThat(audioFile.getName(),is(not(audioFile.getFile().getName())));
		assertThat(audioFile.getName(),is("Beethoven"));
	}

}
