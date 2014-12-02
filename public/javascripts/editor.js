/**
 * @fileOverview: JavaScript file which contains the logic for the editor.
 * 
 * @author
 * @namespace
 */

/*
 * 32 = space 
 * 78 = n 
 * 67 = c 
 * 13 = return
 * 83 = s
 * 68 = d
 */
var KEY_PLAY_PAUSE = 32;
var KEY_NEW_STATEMENT = 78;
var KEY_REWIND = 77;
var KEY_SLOWER = 83;
var KEY_FASTER = 68;

/*
 * You can switch shortcut key here.
 */
function shortcutKey(event) {
	return event.ctrlKey;
	// return event.shiftKey;
}

var editor = {

	/**
	 * Called on startup: Set event listeners and do initializations
	 * 
	 * @returns {undefined}
	 */
	init : function() {

		$("table").on(
				"click",
				"tr.uneditableRow",
				function(event) {
					// Do not run this method if a button was clicked
					if (event.target.tagName.toLowerCase() != "button"
							&& event.target.tagName.toLowerCase() != "span") {
						var statementSeconds = $(this).find(".statementTime")
								.data("seconds");
						audio.setTime(statementSeconds);
					}
				});

		$("#statementAdd").click(function() {
			editor.addStatement();
		});

		// Adds an evenhandler to each editButton, this method is used,
		// because all buttons which were added dynamically should use this
		// event handler as well
		$("body").on("click", "button.statementEdit", function() {
			var row = $(this).closest("tr");
			editor.setRowEditable(row);
		});

		// set event handler on all cancel buttons to set the current row
		// uneditable
		$("body").on("click", "button.cancelEdit", function() {
			var row = $(this).closest("tr");
			editor.setRowUneditable(row);
		});

		// add event handler to send remove request to the server and remove the
		// row
		$("body").on("click", "button.statementRemove", function() {
			var row = $(this).closest("tr");
			editor.removeStatement(row);
		});

		// add event handler to save a new statement and the corresponding codes
		// and time
		$("body").on("click", "button.statementSave", function() {
			var row = $(this).closest("tr");
			var codeDropdown = row.find(".allCodes");
			var codeInput = row.find(".codeInput");
			editor.addCode(codeDropdown, codeInput);
			editor.saveStatement(row);
		});

		// handle shortcuts
		$(document).bind('keydown', function(e) {
			if (e.which == KEY_PLAY_PAUSE && shortcutKey(e)) {
				e.preventDefault();
				audio.toggleAudio();
				return false;
			}
		});
		$(document)
				.bind(
						'keydown',
						function(e) {
							if (e.which == KEY_NEW_STATEMENT && shortcutKey(e)) {
								e.preventDefault();
								if (e.target.tagName.toLowerCase() != "input"
										&& e.target.tagName.toLowerCase() != "textarea") {
									editor.addStatement();
								}
								return false;
							}
						});
		$(document).bind('keydown', function(e) {
			  if(e.which == KEY_REWIND && shortcutKey(e)) {
				    e.preventDefault();
				    audio.returnSec();
				    return false;
			  }
		});
		$(document).bind('keydown', function(e) {
			  if(e.which == KEY_SLOWER && shortcutKey(e)) {
				    e.preventDefault();
				    audio.slower();
				    return false;
			  }
		});
		$(document).bind('keydown', function(e) {
			  if(e.which == KEY_FASTER && shortcutKey(e)) {
				    e.preventDefault();
				    audio.faster();
				    return false;
			  }
		});

		// save statement, time and codes on pressing 'Enter'
		$("table").on("keydown", "tr.editableRow", function(button) {
			// Enter
			if (button.keyCode == 13) {
				var row = $(this);
				var codeDropdown = row.find(".allCodes");
				var codeInput = row.find(".codeInput");
				editor.addCode(codeDropdown, codeInput);
				editor.saveStatement(row);
			}
		});

		// Handle timepicker button events to manipulate the time of the
		// statement
		$("body").on(
				"click",
				"button.timepicker-up",
				function() {
					var timepickerInputField = $(this).closest(".input-group")
							.find(".timepicker-value");
					editor.addSecond(timepickerInputField);
				});

		$("body").on(
				"click",
				"button.timepicker-down",
				function() {
					var timepickerInputField = $(this).closest(".input-group")
							.find(".timepicker-value");
					editor.substractSecond(timepickerInputField);
				});

		// Initialization of the removeCode buttons
		$("body").on("click", "span.removeCode", function() {
			var codeDropdown = $(this).closest(".input-group").find(".allCodes");
			var codeInput = $(this).closest(".input-group").find(".codeInput");
			$(this).closest("li").remove();
			editor.refreshCodeInput(codeDropdown, codeInput);
		});

		// Initialization of the addCode button
		$("body").on(
				"click",
				"button.addCode",
				function() {
					var codeDropdown = $(this).closest(".input-group").find(
							".allCodes");
					var codeInput = $(this).closest(".input-group").find(
							".codeInput");
					editor.addCode(codeDropdown, codeInput);
				});

		// Initialization of the addKnownCode button
		$("body").on(
				"click",
				"span.addKnownCode",
				function() {
					var codeDropdown = $(this).closest(".input-group").find(".allCodes");
					var codeText = $(this).closest("li").text().trim();
					var codeInput = $(this).closest(".input-group").find(".codeInput");
					codeInput.val(codeText);
					editor.addCode(codeDropdown, codeInput);
				});

		// Editing this interview
		$("body").on(
				"click",
				"button.interviewEdit",
				function() {
					var interviewId = $("#ajaxroutes").data("interviewid");

					// Read all current values, to put them into the form
					$.getJSON("/getInterviewInfo/" + interviewId, function(
							result) {

						// create the modal with the form with the current
						// values
						var interviewEditHTML = Mustache.to_html($(
								'#interviewEditTemplate').html(), result);
						$("#modalContainer").html(interviewEditHTML);
						$("#interviewEdit").modal("show");
						$("#interviewEdit").find("#saveInterview").click(
								function() {

									var formData = new FormData(document
											.getElementById("interviewForm"));
									$(this).button("loading");
									editor.editInterview(formData);
								});
					});
				});

	},

	/**
	 * Calculates a time-string if a new Statement is created.
	 * 
	 * @param {number}
	 *            currentTime the current time in seconds
	 * @returns {string} time as string
	 */
	calculateStatementTime : function(currentTime) {
		if (currentTime > 4) {
			return audio.convertSecToDate(audio.getCurrentTime() - 4);
		}
		return audio.convertSecToDate(0);
	},

	/**
	 * Adds a new Statement and reports to server
	 * 
	 * @returns {undefined}
	 */
	addStatement : function() {
		audio.pauseAudio();

		data = {
			interviewId : $('#ajaxroutes').data("interviewid"),
			statementTime : editor.calculateStatementTime(audio
					.getCurrentTime()),
			codes : "",
			description : ""
		},

		$.ajax({
			type : "POST",
			url : $('#ajaxroutes').data("addstatement"),
			data : JSON.stringify(data),
			contentType : "application/json; charset=utf-8",
			dataType : "json",
			statusCode : {
				200 : function(data) {
					var editableRow = Mustache.to_html(
							$('#editableRowTemplate').html(), data);
					$("#statementsTable tbody").prepend(editableRow);
					//the input field has the autofocus attribute, but somehow it doesnt work, so do it manually
					$("#input-for-" + data.statementId).focus();
				}
			}
		});
	},

	/**
	 * Requests server to delete a Statement. Removes row from the table
	 * 
	 * @param {object}
	 *            row the accessed row
	 * @returns {undefined}
	 */
	removeStatement : function(row) {
		$.ajax({
			type : "POST",
			url : $('#ajaxroutes').data("removestatement")
					+ row.data("statementid"),
			statusCode : {
				200 : function(data) {
					row.fadeOut();
				},
				400 : function(data) {
					generalAlert(result.responseText);
				}
			}
		});
	},

	/**
	 * Requests server to save a Statement. Return to uneditable-state.
	 * 
	 * @param {object}
	 *            row the accessed row
	 * @returns {undefined}
	 */
	saveStatement : function(row) {
		data = {
			statementId : row.data("statementid"),
			statementTime : row.find(".statementTime").val(),
			description : row.find(".description").val(),
			// Use this row if you use the comma-separated-view
			// codes : row.find(".codes").val(),
			codes : editor.getAllCodes(row)
		};

		$.ajax({
			type : "POST",
			url : $('#ajaxroutes').data("savestatement"),
			data : JSON.stringify(data),
			contentType : "application/json; charset=utf-8",
			dataType : "json",
			statusCode : {
				200 : function(data) {
					editor.setRowUneditable(row);
				}
			}
		});
	},

	/**
	 * Sets a row editable.
	 * 
	 * @param {object}
	 *            row the accessed row
	 * @returns {undefined}
	 */
	setRowEditable : function(row) {
		$.ajax({
			url : "/editor/getStatement/" + row.data("statementid"),
			type : "POST",
			dataType: "json",
			success: function(data) {
				var editableRow = Mustache.to_html(
					$('#editableRowTemplate').html(), data);
				editor.replaceRow(row, editableRow);
			}
		});
	},

	/**
	 * Sets a row uneditable.
	 * 
	 * @param {object}
	 *            row the accessed row
	 * @returns {undefined}
	 */
	setRowUneditable : function(row) {
		$.ajax({
			url : "/editor/getStatement/" + row.data("statementid"),
			type : "POST",
			dataType: "json",
			success: function(data) {
				var uneditableRow = Mustache.to_html($('#uneditableRowTemplate')
					.html(), data);
				editor.replaceRow(row, uneditableRow);
			}
		});
	},

	/**
	 * Replace some old row with a new row
	 * 
	 * @param {object}
	 *            oldRow the row to be deleted
	 * @param {object}
	 *            newRow the new row to be filled in
	 * @returns {undefined}
	 */
	replaceRow : function(oldRow, newRow) {
		oldRow.after(newRow);
		oldRow.remove();
	},

	/**
	 * Adds one second in the timepicker
	 * 
	 * @param {object}
	 *            timepicker the timepicker object
	 * @returns {undefined}
	 */
	addSecond : function(timepicker) {
		var newSeconds = timepicker.data("seconds") + 1
		if (editor.validateSeconds(newSeconds)) {
			timepicker.val(audio.convertSecToDate(newSeconds));
			timepicker.data("seconds", newSeconds);
		}

	},

	/**
	 * substracts one second in the timepicker
	 * 
	 * @param {object}
	 *            timepicker the timepicker object
	 * @returns {undefined}
	 */
	substractSecond : function(timepicker) {
		var newSeconds = timepicker.data("seconds") - 1
		if (editor.validateSeconds(newSeconds)) {
			timepicker.val(audio.convertSecToDate(newSeconds));
			timepicker.data("seconds", newSeconds);
		}
	},

	/**
	 * Validates the given seconds. This function returns true, if the given
	 * seconds are valid, otherwise false
	 * 
	 * @returns {boolean}
	 */
	validateSeconds : function(seconds) {
		var maxSeconds = audio.upperTime;
		if (seconds >= 0 && seconds <= maxSeconds) {
			return true;
		}
		return false;
	},

	/**
	 * This function returns true if the new code in the codeInput field is
	 * valid, otherwise false.
	 * 
	 * @param {html
	 *            text input}
	 * @returns {boolean}
	 */
	validateCode : function(codeInput) {

		Array.prototype.contains = function(element) {
			return this.indexOf(element) > -1;
		};

		var codeValue = codeInput.val();
		var row = codeInput.closest("tr");

		if (codeValue == "" || editor.getAllCodes(row).contains(codeValue)) {
			return false;
		}
		return true;
	},

	/**
	 * Adds a new code element to the dropdown menu of the codes view
	 * 
	 * @param {string}
	 *            value of the code
	 * @returns {undefined}
	 */
	addCode : function(codeDropdown, codeInput) {
		if (editor.validateCode(codeInput)) {
			codeDropdown
					.append("<li>"
							+ "<a href='javascript:;'>"
							+ "<div class='codeValue col-md-1'>"
							+ codeInput.val()
							+ "</div>"
							+ "<span class='label label-default removeCode pull-right'>"
							+ "<span class='glyphicon glyphicon-trash'></span>"
							+ "</span>" + "</a>" + "</li>");
							
			this.refreshCodeInput(codeDropdown, codeInput);
		}
	},
	
	/**
	 * Adjusts the placeholder so that it fits the current codes and resets the value of the code input.
	 */
	refreshCodeInput : function(codeDropdown, codeInput) {
		var allCodes = codeDropdown.text().replace(/\s+/g, " ").trim();
		codeInput.attr("placeholder", allCodes);
		codeInput.val("");
	},

	/**
	 * Returns all code values from a dropdown menu
	 * 
	 * @param {row}
	 *            row which was selected
	 * @returns {array}
	 */
	getAllCodes : function(row) {
		var result = [];
		row.find(".allCodes .codeValue").each(function() {
			result.push($(this).html());
		});
		return result;
	},

	/**
	 * Handles the server request while editing this interview
	 * 
	 * @param {formData}
	 *            data of the interview edit modal
	 * @returns {undefined}
	 */
	editInterview : function(formData) {
		$.ajax({
			url : "/editInterview",
			type : "POST",
			data : formData,
			processData : false, // tell jQuery not to process the data
			contentType : false, // tell jQuery not to set contentType
			statusCode : {
				200 : function(result) {
					$("#interviewEdit").modal("hide");
					generalSuccess("Das Interview wurde gespeichert.");
				},
				400 : function(result) {
					$("#interviewEdit").modal("hide");
					generalAlert(result.responseText);
				},
				500 : function(result) {
					$("#interviewEdit").modal("hide");
					generalAlert("Interner Serverfehler");
				},
			}
		});
	}
};