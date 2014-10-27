package models;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.Logger;
import play.db.ebean.Model;

/**
 * AudioFile-objects are used to persist and retrieve where the audioFiles of an
 * interview are stored. The Interviews will save these objects to the DB using
 * the cascade save mechanism.
 * 
 * @author Christian
 * @author Sven
 * @author Thiemo
 */
@SuppressWarnings("serial")
@Entity
public class AudioFile extends Model {

	private static final String defaultPath = "audioFiles/";

	@Id
	private String path;

	private String objectName;
	
	/**
	 * This constructor will create a new AudioFile-Object. It will move the
	 * given audio to the target folder and remember the path. The default
	 * non-unique name is the old filename as it was uploaded by the user. At
	 * the sever it might be saved with another name.
	 * 
	 * Delete operations have to be directly invoked on audio to delete the
	 * associated files from server.
	 * 
	 * @param targetFolder
	 * @param audio
	 * @throws IOException
	 */
	public AudioFile(File targetFolder, File audio) throws IOException {
		targetFolder.mkdirs();
		File targetFile = moveFile(audio, targetFolder);
		this.path = targetFile.getAbsolutePath();
		this.setName(audio.getName());
		Logger.info("Uploaded audio file:" + audio.getName() + " server filename:" + this.getFilename());
	}

	/**
	 * This constructor will create a new AudioFile-Object. It will move the
	 * given audio to the default target folder.
	 * 
	 * @param audio
	 * @throws IOException
	 */
	public AudioFile(File audio) throws IOException {
		this(createDefaultPathIfNotExists(), audio);
	}

	/**
	 * This method creates the default path.
	 * 
	 * @return default path as a File-Object.
	 */
	private static File createDefaultPathIfNotExists() {
		File dir = new File(defaultPath);
		dir.mkdir();
		return dir;
	}

	/**
	 * The moveFile method manages file copying and renaming
	 * 
	 * @param audio
	 * @param targetFolder
	 * @return the moved file with a new filename.
	 * @throws IOException
	 */
	private File moveFile(File audio, File targetFolder) throws IOException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HHmm_");
		Date date = new Date();

		File targetFile;
		do {
			targetFile = new File(targetFolder.getAbsolutePath() + "/"
					+ dateFormat.format(date)
					+ ((int) (Math.random() * 900000 + 100000))
					+ audio.getName());
		} while (targetFile.exists());
		Files.move(Paths.get(audio.getAbsolutePath()),
				Paths.get(targetFile.getAbsolutePath()));
		return targetFile;
	}

	public String getPath() {
		return this.path;
	}

	/**
	 * Constructs a File-Object from the path stored in the DB.
	 */
	public File getFile() {
		return new File(this.path);
	}

	/**
	 * Returns the name of the file on the server. Use this method to get the
	 * filename in the player
	 * 
	 * @return filename on the server
	 */
	public String getFilename() {
		return new File(this.path).getName();
	}
	
	protected void deleteFile(){
		getFile().delete();
		Logger.info("Deleted audio file:" + this.getName() + " server filename:" + this.getFilename());
		File dp = new File(defaultPath);
		if (dp.isDirectory() && dp.list().length == 0) {
			dp.delete();
		}
	}

	/**
	 * The default non-unique name is the old filename as it was uploaded by the
	 * user. At the sever it might be saved with another name.
	 */
	public String getName() {
		return objectName;
	}

	public void setName(String name) {
		objectName = name;
	}

	/**
	 * This method manages the deletion from the database as common. It will
	 * also delete the file from the file-system and delete its parent folder if
	 * its empty.
	 */
	@Override
	public void delete() {
		deleteFile();
		super.delete();
	}

}
