package models;

import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;
import play.data.validation.*;

/**
 * This Ebean will be saved by Statement-Objects. It contains the
 * time-stamp-information for an interview.
 * 
 * @author Thiemo
 * @author Benedikt
 */
@SuppressWarnings("serial")
@Entity
public class Time extends Model {

	@Id
	private UUID id;
	
	@Constraints.Required
	private int minutes;
	
	@Constraints.Required
	private int seconds;
	
	@Constraints.Required
	private int millis;

	// *************************************************************************
	// METHODS TO ADRESS DATABASE
	// *************************************************************************

	/**
	 * the database Finder-object for the time-relation.
	 */
	public static Model.Finder<UUID, Time> find = new Model.Finder<UUID, Time>(
			UUID.class, Time.class);

	/**
	 * Get a Time Object from the database by id.
	 * 
	 * @param timeId
	 */
	public static Time getTime(UUID timeId) {
		return find.where().eq("Id", timeId).findUnique();
	}

	/**
	 * Gives all time Objects. Except of tests it is unused so far.
	 */
	public static List<Time> findAllTimes() {
		return find.all();
	}

	// *************************************************************************
	// CONSTRUCTORS
	// *************************************************************************

	/**
	 * Creates a Time from a specified. Minutes and Seconds are expected to be
	 * positive.
	 * 
	 * @param minutes
	 * @param seconds
	 */
	public Time(int minutes, int seconds) {
		this.minutes = minutes;
		this.seconds = seconds;
		this.millis = 0;
		legalize();
	}

	/**
	 * Creates a Time from a specified. minutes, seconds, and millisecond are
	 * expected to be positive
	 * 
	 * @param minutes
	 * @param seconds
	 * @param milliseconds
	 */
	public Time(int minutes, int seconds, int milliseconds) {
		this.minutes = minutes;
		this.seconds = seconds;
		this.millis = milliseconds;
		legalize();
	}
	
	/**
	 * Creates a Time instance by parsing from String in format "MM:SS"
	 * 
	 * @param string
	 */
	public Time(String string){
		
		this(Integer.parseInt(string.split(":")[0]), Integer.parseInt(string.split(":")[1]));
	}

	// *************************************************************************
	// TOSTRING
	// *************************************************************************

	/**
	 * The representative String contains minutes and seconds.
	 * 
	 * @return e.g. "3:02"
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (getMinutes() < 10) {
			builder.append("0");
		}
		builder.append(Integer.toString(getMinutes()));
		builder.append(":");
		if (getSeconds() < 10) {
			builder.append("0");
		}
		builder.append(Integer.toString(getSeconds()));
		return builder.toString();
	}
	
	// *************************************************************************
	// TOMILLIS
	// *************************************************************************

	/**
	 * Gives the Time in milliseconds.
	 * @return integer value.
	 */
	public int toMillis(){
		return getMillis()+1000*(getSeconds()+60*getMinutes());
	}

	// *************************************************************************
	// LEGALIZE
	// *************************************************************************

	/**
	 * This method is called to bring the Time-Object in a valid state. That
	 * means it brings minutes seconds and milliseconds in a range that makes
	 * sense if it's displayed.
	 */
	private void legalize() {
		seconds += getMillis() / 1000;
		minutes += getSeconds() / 60;
		millis %= 1000;
		seconds %= 60;

		if (getMillis() < 0) {
			millis += 1000;
			seconds -= 1;
		}

		if (getSeconds() < 0) {
			seconds += 60;
			minutes -= 1;
		}

		if (getMinutes() < 0) {
			throw new RuntimeException("illegal time");
		}
	}

	// *************************************************************************
	// GETTERS AND SETTERS
	// *************************************************************************

	public UUID getId() {
		return id;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
		legalize();
		save();
	}

	public int getSeconds() {
		return seconds;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
		legalize();
		save();
	}

	public int getMillis() {
		return millis;
	}

	public void setMillis(int millis) {
		this.millis = millis;
		legalize();
		save();
	}
	
	
}
