/**
 * @fileOverview: JavaScript file which contains the logic for the
 *                interviews-view.
 * 
 * @author
 * @namespace
 */
var interviews = {

	/**
	 * Called on startup: Set event listeners and do initializations
	 * 
	 * @returns {undefined}
	 */
	init : function() {
		/**
		 * Adding an interview, handle user input
		 */
		$("#interviewAddButton").click(
				function() {
					propertytypes['headline'] = "Interview anlegen";
					var interviewAddHTML = Mustache.to_html($(
							'#interviewAddTemplate').html(), propertytypes);
					$("#modalContainer").html(interviewAddHTML);
					$("#interviewAdd").modal("show");

					$("#interviewAdd").find("#saveInterview").click(
							function() {
								var formData = new FormData(document
										.getElementById("interviewForm"));
								$(this).button("loading");
								interviews.addInterview(formData);
							});
				});

		/**
		 * Removing an interview, handle user input
		 */
		$("body")
				.on(
						"click",
						"button.interviewRemove",
						function() {
							var _this = this;

							showConfirmModal(
									function() {
										interviews.removeInterview($(_this)
												.data("interviewid"));

										// to get all the elements of this row
										// and fade them out
										$(_this).parentsUntil("tbody").fadeOut(
												200);
									},
									unescape("M%F6chten Sie wirklich das Interview inklusive aller Aussagen l%F6schen%3F"),
									function() {
									});
						});

		/**
		 * Editing an interview, handle user input
		 */
		$("body").on(
				"click",
				"button.interviewEdit",
				function() {
					var interviewId = $(this).data("interviewid");

					// Read all current values, to put them into the form
					$.getJSON("/getInterviewInfo/" + interviewId, function(
							result) {

						// create the modal with the form with the current
						// values
						result['headline'] = "Interview bearbeiten";
						var interviewAddHTML = Mustache.to_html($(
								'#interviewAddTemplate').html(), result);
						$("#modalContainer").html(interviewAddHTML);
						$("#interviewAdd").modal("show");
						$("#interviewAdd").find("#saveInterview").click(
								function() {

									var formData = new FormData(document
											.getElementById("interviewForm"));
									$(this).button("loading");
									interviews.editInterview(formData);
								});
					});
				});

		/**
		 * Showing interview info
		 */
		$("body").on(
				"click",
				"button.interviewInfo",
				function() {
					$.getJSON("/getInterviewInfo/"
							+ $(this).data('interviewid'), function(result) {

						var interviewInfoHTML = Mustache.to_html($(
								'#interviewInfoTemplate').html(), result);
						$("#modalContainer").html(interviewInfoHTML);
						$("#interviewInfo").modal("show");
					});
				});
	},

	/**
	 * Server request to edit the interview with the id
	 * @param {object}
	 *            formData information to be send to the server
	 * @returns {undefined}
	 */
	editInterview : function(formData) {
		$.ajax({
			url : $('#ajaxroutes').data("editinterview"),
			type : "POST",
			data : formData,
			processData : false, // tell jQuery not to process the data
			contentType : false, // tell jQuery not to set contentType
			statusCode : {
				200 : function(result) {
					var jsonData = $.parseJSON(result);
					var interviewRowHTML = Mustache.to_html($(
							'#interviewRowTemplate').html(), jsonData);
					$("#" + jsonData.interviewId).replaceWith(interviewRowHTML);
					$("#interviewAdd").modal("hide");
				},
				400 : function(result) {
					$("#interviewAdd").modal("hide");
					generalAlert(result.responseText);
				},
				500 : function(result) {
					$("#interviewAdd").modal("hide");
					generalAlert("Interner Serverfehler");
				},
			}
		});
	},

	/**
	 * Server request to add an interview with the data of the form
	 * 
	 * @param {object}
	 *            formData information to be send to the server
	 * 
	 * @returns {undefined}
	 */
	addInterview : function(formData) {
		$.ajax({
			url : $('#ajaxroutes').data("addinterview"),
			type : "POST",
			data : formData,
			processData : false, // tell jQuery not to process the data
			contentType : false, // tell jQuery not to set contentType
			statusCode : {
				200 : function(result) {
					$("#interviewAdd").modal("hide");
					interviews.addInterviewRow(result);
				},
				400 : function(result) {
					$("#interviewAdd").modal("hide");
					generalAlert(result.responseText);
				},
				500 : function(result) {
					$("#interviewAdd").modal("hide");
					generalAlert("Interner Serverfehler");
				},
			}
		});
	},

	/**
	 * Server request to remove the interview.
	 * 
	 * @param {number}
	 *            id the id of the interview that will be deleted
	 * 
	 * @returns {undefined}
	 */
	removeInterview : function(Id) {
		data = {
			interviewId : Id
		};
		$
				.ajax({
					type : "POST",
					url : $('#ajaxroutes').data("removeinterview"),
					contentType : "application/json; charset=utf-8",
					dataType : "json",
					data : JSON.stringify(data),
					statusCode : {
						200 : function(data) {
							console.log("interview gelöscht!");
						},
						400 : function(data) {
							console.error(data.responseText);
							generalAlert("Die Aktion konnte nicht ausgef&uuml;hrt werden: "
									+ data.responseText);
						}
					}
				});
	},

	/**
	 * Adds an interview to the interview table
	 * 
	 * @param {jsonstring}
	 *            interview the interview data
	 * 
	 * @returns {undefined}
	 */
	addInterviewRow : function(interview) {
		var json = $.parseJSON(interview);
		var interviewRowHTML = Mustache.to_html($('#interviewRowTemplate')
				.html(), json);
		$("#interviewsTable > tbody").append(interviewRowHTML);
	},

};