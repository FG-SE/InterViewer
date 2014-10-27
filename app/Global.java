import java.util.List;

import models.User;

import com.avaje.ebean.Ebean;

import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.libs.Yaml;

/**
 * Set gobal settings
 */
public class Global extends GlobalSettings{

	/**
	 * Prefill the database if empty
	 * @see play.GlobalSettings#onStart(play.Application)
	 */
	@Override
	public void onStart(Application app){
		if(User.findAll().isEmpty()){
			Ebean.save((List<?>) Yaml.load("admin-data.yml"));
			Logger.warn("no database found - filled with admin-data");
		}
	}
}
