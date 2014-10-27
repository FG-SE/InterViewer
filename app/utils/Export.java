package utils;

import models.Interview;
import models.Project;
import models.PropertyType;
import models.Statement;
import models.Code;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.json.JSONException;
import org.json.JSONWriter;

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Export {
	private static final String illegalCharacters = "[^A-Za-z0-9,;:._-]";
	/**
	 * Writes all properties of one Project to a File (using JSON). It goes through all objects that are accumulated by the project, too.
	 * Finally a ZIP archive is generated, containing the JSON file and all audio files.
	 * 
	 * @param source The project that should get exported
	 * @return returns a File object referring to the generated ZIP file
	 * @throws IOException
	 */
	public static File exportProject(Project source) throws IOException{
		File tmpFolder = File.createTempFile("export_", "");
		tmpFolder.delete(); //file was created but we need a folder
		tmpFolder.mkdir();
		
		String name = source.getName().replaceAll(illegalCharacters, "?");
		File jsonFile = new File(tmpFolder.getAbsolutePath()+"/"+name+".json");
		File zipFile = new File(tmpFolder.getAbsolutePath()+"/"+name+".zip");
		
		List<File> exportFiles = new ArrayList<File>();
		exportFiles.add(jsonFile);
		
		Writer w = new FileWriter(jsonFile);
		JSONWriter json = new JSONWriter(w);
		try {
			json.object()
				.key("name").value(source.getName());
			
			json.key("interviews").array();
			for (Interview i : source.getInterviews()) {
				json.object();
				json.key("name").value(i.getName());
				
				//DynamicProperties
				for (PropertyType type : source.getPropertyTypes()) {
					json.key(type.getName()).value(i.getProperty(type));
				}
				
				//AudioFile
				if (i.getAudio()!=null) {
					File audioFile = fetchFile(i.getAudio().getFile(), tmpFolder, i.getAudio().getName());
					exportFiles.add(audioFile);
					json.key("audio").value(audioFile.getName());
				}
				
				//Statements
				json.key("statements").array();
				for (Statement s : i.getStatements()) {
					json.object();
					json.key("time").value(s.getTime().toString());
					json.key("description").value(s.getDescription());
					json.key("codes").array();
					for (Code c : s.getCodes()) {
						json.value(c.getName());
					}
					json.endArray();
					json.endObject();
				}
				json.endArray();
				
				json.endObject();
			}
			json.endArray();
			
			json.endObject();
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException("Not able to create JSON for project "+source.getName()+".");
		}
		w.close();
		
		return zip(exportFiles, zipFile);
	}
	
	private static File fetchFile(File origin, File dest, String name) throws IOException{
		//find new filename
		String newName = name;
		int i = 0;
		while (new File(dest.getAbsolutePath()+"/"+newName).exists()) {
			i++;
			newName = FilenameUtils.getBaseName(origin.getName())+"_"+Integer.toString(i)+"."+FilenameUtils.getExtension(origin.getName());
		}
		
		Files.copy(origin.toPath(), Paths.get(dest.getAbsolutePath()+"/"+newName), StandardCopyOption.COPY_ATTRIBUTES);
		return new File(dest.getAbsolutePath()+"/"+newName);
	}
	
	private static File zip(List<File> files, File dest) throws IOException {
		//zipping all files to zip archive dest
		FileOutputStream out = new FileOutputStream(dest);
		ZipOutputStream zipStream = new ZipOutputStream(out);
		byte[] buffer = new byte[1024];
		
		for (File f : files) {
			FileInputStream in = new FileInputStream(f);
			ZipEntry entry = new ZipEntry(f.getName());
			zipStream.putNextEntry(entry);
			
			int len;
			while((len=in.read(buffer)) > 0) {
				zipStream.write(buffer, 0, len);
			}
			
			zipStream.closeEntry();
			in.close();
		}
		
		zipStream.close();
		out.close();
		return dest;
	}
}
