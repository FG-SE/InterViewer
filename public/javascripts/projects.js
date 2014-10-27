/**
 * @fileOverview: JavaScript file which contains the logic for the projects
 *                view.
 * 
 * @author
 * @namespace
 */

var projects = {

	/**
	 * Initialize path variables. They are needed to send ajax requests. They
	 * can not set in this file.
	 */
	pathGetUserNames : "",
	pathPostAddProject : "",
	pathPostEditProject : "",
	pathPostDeleteProject : "",
	pathPostGetInfo : "",

	/**
	 * Called on startup: Set event listeners and do initializations
	 * 
	 * @returns {undefined}
	 */
	init : function(getUserNames, postAddProject, postEditProject,
			postDeleteProject, postGetInfo) {

		var _this = this;

		// path variables (they can not interpreted in this file)
		this.pathGetUserNames = getUserNames;
		this.pathPostAddProject = postAddProject;
		this.pathPostEditProject = postEditProject;
		this.pathPostDeleteProject = postDeleteProject;
		this.pathPostGetInfo = postGetInfo;

		// event listener on button to add projects
		$("#projectAdd").click(function() {
			// send request to server to get user names
			$.getJSON(_this.pathGetUserNames, function(data) {

				_this.renderAddModal(data);
			});
		});
	},

	// *************************************************************************
	// INTERACTION WITH DOM
	// *************************************************************************

	/**
	 * Helper function to render row template for a new project an show it
	 * 
	 * @param {Json}
	 *            projectData the project data
	 * @returns {undefined}
	 */
	addRow : function(projectData) {
		var projectAddModal = Mustache.to_html($('#rowTemplate').html(),
				projectData);
		$("#projectTable" + " tr:last").after(projectAddModal);
		$("#" + projectData["Id"]).fadeIn(1000);
	},

	// 
	/**
	 * Helper function to render row template to replace the current (maybe out
	 * of date) row
	 * 
	 * @param {Json}
	 *            projectData the project data
	 * @returns {undefined}
	 */
	replaceRow : function(projectData) {
		var projectEditModal = Mustache.to_html($('#rowTemplate').html(),
				projectData);
		$("#" + projectData["Id"]).fadeOut(1000).delay(1300).replaceWith(
				projectEditModal);
		$("#" + projectData["Id"]).fadeIn(1300);
	},

	/**
	 * removes the row in the table
	 * 
	 * @param {String}
	 *            projectId the Id of the project to remove
	 * @returns {undefined}
	 */
	removeRow : function(projectId) {
		$('#' + projectId).fadeOut(1000).delay(1000).remove();
	},

	// *************************************************************************
	// SLIDE THE USERS BETWEEN THE TWO SELECTS
	// *************************************************************************

	/**
	 * Remove the selected user from the select which contains the members of
	 * the project
	 * 
	 * @param {String}
	 *            selectForeignId the id of the select with the users that are
	 *            NOT member of the project
	 * @param {String}
	 *            selectMemberId the id of the select with the users that are
	 *            member of the project
	 * 
	 * @returns {undefined}
	 */
	modalRemoveSelectedUsers : function(selectForeignId, selectMemberId) {
		removeUsers = this.getDataFromSelect($(selectMemberId + " option"),
				false);
		ExistingUsers = this.getDataFromSelect($(selectForeignId + " option"),
				true);

		$.each(removeUsers, function() {
			if (this.Selected == true) {
				var name = this.Name;
				$.each(ExistingUsers, function(nr, user) {
					if (this.Name === name)
						name = null
				});
				if (name != null)
					$(selectForeignId).append(
							'<option value="' + this.Value + '">' + this.Name
									+ '</option>');
				$(selectMemberId + ' option[value=' + this.Value + ']')
						.remove();
			}
		});
	},

	/**
	 * Add the selected user to the select which contains the members of the
	 * project
	 * 
	 * @param {String}
	 *            selectForeignId the id of the select with the users that are
	 *            NOT member of the project
	 * @param {String}
	 *            selectMemberId the id of the select with the users that are
	 *            member of the project
	 * 
	 * @returns {undefined}
	 */
	modalAddSelectedUsers : function(selectForeignId, selectMemberId) {
		newUsers = this
				.getDataFromSelect($(selectForeignId + " option"), false);
		OldUsers = this.getDataFromSelect($(selectMemberId + " option"), true);

		$.each(newUsers, function() {
			if (this.Selected == true) {
				var name = this.Name;
				$.each(OldUsers, function(nr, user) {
					if (this.Name === name)
						name = null
				});
				if (name != null)
					$(selectMemberId).append(
							'<option value="' + this.Value + '">' + this.Name
									+ '</option>');
				$(selectForeignId + ' option[value=' + this.Value + ']')
						.remove();
			}
		});
	},

	// 

	/**
	 * Collect user data from a html select
	 * 
	 * @param $select
	 *            jquery object of the select
	 * @param allUsersSelected
	 *            optional parameter true - set selected state of all options
	 *            always true false - read selected attribute of the options
	 * 
	 * @returns {Json}
	 */
	getDataFromSelect : function($select, allUsersSelected) {
		allUsersSelected = allUsersSelected || false;
		var data = $select.map(function() {
			var object = new Object();
			object["Name"] = $(this).text()
			object["Value"] = $(this).val();
			object["Selected"] = allUsersSelected || $(this).is(":selected");
			return object;
		}).get();

		return data;
	},

	// *************************************************************************
	// RENDER FUNCTIONS
	// *************************************************************************

	// 

	/**
	 * Insert in the given template the json data and show the modal
	 * 
	 * @param {String}
	 *            templateId the Id of a template
	 * @param {String}
	 *            containerId the Id of the container to insert into
	 * @param {Json}
	 *            data the data to insert into the template
	 * 
	 * @returns {undefined}
	 */
	renderModal : function(templateId, containerId, data) {
		var htmlModal = Mustache.to_html($('#' + templateId).html(), data);
		$("#" + containerId).html(htmlModal);
		$("#" + containerId).find(".modal").modal("show");
	},

	/**
	 * Render the modal for project edit and set event listener
	 * 
	 * @param {Json}
	 *            data the data of the project
	 * 
	 * @returns {undefined}
	 */
	renderEditModal : function(data) {

		var _this = this;

		// render template with json data in 'data'
		_this.renderModal("projectEditTemplate", "projectEditModalContainer",
				data);

		// property tooltip
		$("#projectEditTemplateModal").find("#editPropertyHelp").tooltip({
			placement : 'top'
		});

		// place event listener on save button
		// send edited data to server
		$("#projectEditTemplateModal")
				.find("#saveProject")
				.click(
						function() {

							// find user-edited data in shown modal
							var Id = $("#projectEditTemplateModal").find(
									"#editProjectId").val();
							var Name = $("#projectEditTemplateModal").find(
									"#editProjectName").val();
							var Users = _this
									.getDataFromSelect(
											$("select#editProjectUsersSelected option"),
											true);
							var Properties = _this
									.getProperties($("select#editProjectProperties option"));
							$.each(_this.getDataFromSelect(
									$("select#editProjectUsers option"), true),
									function() {
										this.Selected = false;
										Users.push(this);
									});

							// create Json object
							var project = new Object();
							project["Name"] = Name;
							project["Id"] = Id;
							project["Users"] = Users;
							project["Properties"] = Properties;

							// Send new project data to the server to save
							_this.sendEditProject(project);

							// hide edit modal
							$("#projectEditTemplateModal").modal("hide");
						});

		_this.setPropertyAddEvent($("#projectEditTemplateModal"),
				$("select#editProjectProperties"));
		_this.setPropertyRemoveEvent($("#editProjectProperties"),
				$("#projectEditTemplateModal"));

		// if user presses "enter" in property input, trigger the button
		$("#projectEditTemplateModal").find("input").keyup(
				function(e) {
					if (e.keyCode == 13) {
						$("#projectEditTemplateModal").find("#saveProperty")
								.trigger("click");
					}
				});
	},

	/**
	 * Render the "project add" modal and set event listener
	 * 
	 * @param {Json}
	 *            data the data of the project
	 * 
	 * @returns {undefined}
	 */
	renderAddModal : function(data) {

		var _this = this;

		// render project add modal
		_this.renderModal("projectAddTemplate", "projectAddModalContainer",
				data);

		// property tooltip
		$("#projectAddTemplateModal").find("#addPropertyHelp").tooltip({
			placement : 'top'
		});

		// Pick out the data from the form and send to server
		$("#projectAddTemplateModal")
				.find("#saveProject")
				.click(
						function() {

							// pick out project data
							var users = $(
									"select#addProjectUsersSelected option")
									.map(function() {
										return $(this).val();
									}).get();
							var properties = _this
									.getProperties($("select#addProjectProperties option"));
							var projectName = $("#projectAddTemplateModal")
									.find("#newProjectName").val();

							// create JSON with the values
							var project = new Object();
							project["Name"] = projectName;
							project["Users"] = users;
							project["Properties"] = properties;

							// send JSON data to the server
							_this.sendAddProject(project);

							// close add project modal
							$("#projectAddTemplateModal").modal("hide");
						});

		// handle user input in porperty text field
		_this.setPropertyAddEvent($("#projectAddTemplateModal"),
				$("#addProjectProperties"));
		_this.setPropertyRemoveEvent($("#addProjectProperties"),
				$("#projectAddTemplateModal"));

		// if user presses "enter" in property input, trigger the button to do
		// same operations
		$("#projectAddTemplateModal").find("input").keyup(
				function(e) {
					if (e.keyCode == 13) {
						$("#projectAddTemplateModal").find("#saveProperty")
								.trigger("click");
					}
				});
	},

	/**
	 * Get the value and the selected status of all option of a select
	 * 
	 * @returns {undefined}
	 */
	getProperties : function($select) {
		return $select.map(function() {
			var json = new Object();
			json["Value"] = $(this).val();
			return json;
		}).get();
	},

	/**
	 * Set a click event on the property remove button to hide the option
	 * 
	 * @param {jQuery-object}
	 *            $select the select
	 * @param {jQuery-object}
	 *            $select the current modal
	 * @returns {undefined}
	 */
	setPropertyRemoveEvent : function($select, $modal) {
		$modal.find("#deleteProperty").click(function() {
			$select.find("option:selected").each(function() {
				$(this).remove();
			});
		});
	},

	/**
	 * Set a click listener on the property add button to add property to the
	 * select
	 * 
	 * @param {jQuery-object}
	 *            $projectEditModal the edit modal
	 * @param {jQuery-object}
	 *            $select the select
	 */
	setPropertyAddEvent : function($projectEditModal, $select) {
		// Get property name and put it in the property table
		$projectEditModal.find("#saveProperty").click(
				function() {
					var $input = $(this).parent().parent().find("input");
					var val = $input.val().trim();

					// check whether the text field is empty
					if (val.length > 0) {
						var duplicate = false;
						// true if a hidden element was found
						var hiddenProperty = false;

						// search for a occurence of the same property in the
						// list
						$select.find('option').each(
								function() {

									if ($(this).val() == val)
										duplicate = true;

									// fade the property in if it was removed
									// from the list before
									if (!$(this).is(":visible")
											&& $(this).val() == val) {
										$(this).show();
										$input.val('').focus();
										duplicate = true;
										hiddenProperty = true;
										return false;
									}
								});

						if (!duplicate && !hiddenProperty) {
							// if the property is not found in the list, add it
							// to the list
							$select.append(
									"<option value='" + val + "'>" + val
											+ "</option>").fadeIn(1000);
							$input.val('').focus();
						} else if (duplicate && !hiddenProperty) {
							// display error to sign the already existing
							// property
							$input.parent().parent().addClass("has-error")
									.select();
							$input.parent().parent().on('change', function() {
								$(this).removeClass("has-error");
								$(this).off("change");
							});
						}
					}
				});
	},

	// *************************************************************************
	// SERVER REQUEST FUNCTIONS
	// *************************************************************************

	/**
	 * Send given project data to server to create a new project and add a new
	 * to the table
	 * 
	 * @param {Json}
	 *            project the project data
	 * @returns {undefined}
	 */
	sendAddProject : function(project) {

		var _this = this;

		// send data to server
		$
				.ajax({
					type : "POST",
					url : this.pathPostAddProject,
					contentType : "application/json; charset=utf-8",
					dataType : "json",
					data : JSON.stringify(project),
					statusCode : {
						200 : function(data) {
							// integrate the new project instantly in the list
							_this.addRow(data);
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
	 * Send server request to edit the interview and update the project row
	 * 
	 * @param {Json}
	 *            project the project data
	 * @returns {undefined}
	 */
	sendEditProject : function(project) {

		var _this = this;

		$
				.ajax({
					type : "POST",
					url : this.pathPostEditProject,
					data : JSON.stringify(project),
					contentType : "application/json; charset=utf-8",
					dataType : "json",
					statusCode : {
						200 : function(data) {
							_this.replaceRow(data);
						},
						400 : function(data) {
							console
									.error("Es ist ein Fehler beim Speichern der Projekt-Daten aufgetreten!"
											+ data);
							generalAlert("Die Aktion konnte nicht ausgef&uuml;hrt werden: "
									+ data.responseText);
						}
					}
				});
	},

	/**
	 * Send server request delete the project with the given project Id from
	 * server. Remove the project from the table.
	 * 
	 * @param {String}
	 *            projectId the id of the project
	 * @returns {undefined}
	 */
	sendDeleteProject : function(projectId) {

		var _this = this;

		showConfirmModal(
				function() {

					var markers = {
						"Id" : projectId
					};
					$
							.ajax({
								type : "POST",
								url : _this.pathPostDeleteProject,
								contentType : "application/json; charset=utf-8",
								dataType : "json",
								data : JSON.stringify(markers),
								statusCode : {
									200 : function(data) {
										_this.removeRow(projectId);
									},
									400 : function(data) {
										console.error(data.responseText);
										generalAlert("Die Aktion konnte nicht ausgef&uuml;hrt werden: "
												+ data.responseText);
									}
								}
							});

				},
				unescape("M%F6chten Sie wirklich das Projekt mitsamt aller Interviews l%F6schen%3F"));
	},

	/**
	 * Retrieves and shows data from the project, with the given project Id, in
	 * an info modal
	 * 
	 * @param {String}
	 *            projectId the id of the project
	 * @returns {undefined}
	 */
	sendShowInfo : function(projectId) {
		var _this = this;

		// request data from server
		var markers = {
			"Id" : projectId
		};
		$
				.ajax({
					type : "POST",
					url : this.pathPostGetInfo,
					contentType : "application/json; charset=utf-8",
					dataType : "json",
					data : JSON.stringify(markers),
					statusCode : {
						200 : function(data) {

							// Render Modal with the data and show
							_this.renderModal("projectInfoTemplate",
									"projectInfoModalContainer", data);
						},
						400 : function(data) {
							console
									.error("Es ist ein Fehler beim Laden der Projekt-Daten vom Server aufgetreten!"
											+ data.responseText);
							generalAlert("Die Aktion konnte nicht ausgef&uuml;rt werden: "
									+ data.responseText);
						}
					}
				});
	},

	// 
	/**
	 * Send server request to get project data
	 * 
	 * @param {String}
	 *            projectId the project data
	 * @returns {undefined}
	 */
	getEditProject : function(projectId) {

		var _this = this;

		var markers = {
			"Id" : projectId
		};
		$
				.ajax({
					type : "POST",
					url : this.pathPostGetInfo,
					contentType : "application/json; charset=utf-8",
					dataType : "json",
					data : JSON.stringify(markers),
					statusCode : {
						200 : function(data) {
							// adds the received project informations to the
							// modal, show it and place the event listeners
							_this.renderEditModal(data);
						},
						400 : function(data) {
							console
									.error("Es ist ein Fehler beim Laden der Projekt-Daten vom Server aufgetreten!"
											+ data);
							generalAlert("Die Aktion konnte nicht ausgef&uuml;hrt werden: "
									+ data.responseText);
						}
					}
				});
	},
}