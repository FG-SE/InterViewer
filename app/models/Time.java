package models;

import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.validation.Constraints;
import play.db.ebean.Model;

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
	 * @param minutes Can be larger than 60 if interview is longer than an hour
	 * @param seconds
	 */
	public Time(int minutes, int seconds) {
		this.minutes = minutes;
		this.seconds = seconds;
		this.millis = 0;
		this.legalize();
	}

	/**
	 * Creates a Time from a specified. minutes, seconds, and millisecond are
	 * expected to be positive
	 *
	 * @param minutes Can be larger than 60 if interview is longer than an hour
	 * @param seconds
	 * @param milliseconds
	 */
	public Time(int minutes, int seconds, int milliseconds) {
		this.minutes = minutes;
		this.seconds = seconds;
		this.millis = milliseconds;
		this.legalize();
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
		final StringBuilder builder = new StringBuilder();
		if (this.getMinutes() < 10) {
			builder.append("0");
		}
		builder.append(Integer.toString(this.getMinutes()));
		builder.append(":");
		if (this.getSeconds() < 10) {
			builder.append("0");
		}
		builder.append(Integer.toString(this.getSeconds()));
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
		return this.getMillis()+1000*(this.getSeconds()+60*this.getMinutes());
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
		this.seconds += this.getMillis() / 1000;
		this.minutes += this.getSeconds() / 60;
		this.millis %= 1000;
		this.seconds %= 60;

		if (this.getMillis() < 0) {
			this.millis += 1000;
			this.seconds -= 1;
		}

		if (this.getSeconds() < 0) {
			this.seconds += 60;
			this.minutes -= 1;
		}

		if (this.getMinutes() < 0) {
			throw new RuntimeException("illegal time");
		}
	}

	// *************************************************************************
	// GETTERS AND SETTERS
	// *************************************************************************

	public UUID getId() {
		return this.id;
	}

	public int getMinutes() {
		return this.minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
		this.legalize();
		this.save();
	}

	public int getSeconds() {
		return this.seconds;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
		this.legalize();
		this.save();
	}

	public int getMillis() {
		return this.millis;
	}

	public void setMillis(int millis) {
		this.millis = millis;
		this.legalize();
		this.save();
	}


}
