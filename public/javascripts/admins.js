/**
 * @fileOverview: JavaScript file which contains the logic of the admins view.
 * 
 * @author Sven
 * @namespace
 */
var admins = {

	/**
	 * TODO document properties
	 */
	count : -40000,
	/**
	 * TODO document properties
	 */
	resort : true,
	/**
	 * FIXME ???
	 */
	callback : function() {
	},

	/**
	 * add enter and escape shortcuts to an arbitrary html-tag to quickly access
	 * save an cancel of the input.
	 * 
	 * @param {number|string}
	 *            id the id of the row that has to be saved or canceled
	 * @param {string}
	 *            name the html-id of the tag that gets shortcut handlers
	 * @returns {undefined}
	 */
	addHandler : function(id, name) {
		$('#' + name).keyup(function(event) { // enter key pressed
			if (event.which == 13) {
				// console.log("Enter-key - save ID:"+id);
				$('#saveform' + id).click();
			}
			if (event.which == 27) { // escape key pressed
				// console.log("Escape-key - break ID:"+id);
				breakEdit(id);
			}
		});
	},

	/**
	 * adds li-element for new User
	 * 
	 * @returns {undefined}
	 */
	addNewUserLi : function() {
		$('#usertablebody').prepend(
				'<tr id="newUserLi' + admins.count + '"></tr>');
		$('#newUserLi' + admins.count).append('<td></td>');
		$('#newUserLi' + admins.count).append(
				'<td><input id="newUserLi' + admins.count
						+ 'alias" type="text" value="Alias" autofocus></td>');
		$('#newUserLi' + admins.count).append(
				'<td><input id="newUserLi' + admins.count
						+ 'email" type="email" value="Email"></td>');
		$('#newUserLi' + admins.count).append(
				'<td><input id="newUserLi' + admins.count
						+ 'password" type="password" value="Password"></td>');
		$('#newUserLi' + admins.count).append(
				'<td><input id="newUserLi' + admins.count
						+ 'admin" type="checkbox"></td>');
		$('#newUserLi' + admins.count)
				.append(
						'<td id="newUserLi'
								+ admins.count
								+ 'function">'
								+ '<button id="saveform'
								+ admins.count
								+ '" type="button" title="Neuen Nutzer erstellen" class="btn btn-default btn-xs addNewUser" data-newuserid="'
								+ admins.count
								+ '">'
								+ '<span class="glyphicon glyphicon-ok"></span>'
								+ '</button>'
								+ '&nbsp;'
								+ '<button type="button" title="Nutzer verwerfen" class="btn btn-default btn-xs dismissNewUser" data-newuserid="'
								+ admins.count
								+ '">'
								+ '<span class="glyphicon glyphicon-remove"></span>'
								+ '</button></td>');
		this.addHandler(admins.count, 'newUserLi' + admins.count + 'alias');
		this.addHandler(admins.count, 'newUserLi' + admins.count + 'email');
		this.addHandler(admins.count, 'newUserLi' + admins.count + 'password');
		this.addHandler(admins.count, 'newUserLi' + admins.count + 'admin');
		admins.count++;
	},

	/**
	 * FIXME difference to addNewUserLi. Code duplication? TODO better
	 * documentation adds the li-element for a newly created user
	 * 
	 * @params {string} responseText
	 * @params {number} id the user id
	 * 
	 * @returns {undefined}
	 */
	addUserLi : function(responseText, id) {
		response = responseText.split("?");
		generalSuccess("Status: " + response[0]);
		$('#newUserLi' + id).remove();
		id = response[1];
		$('#usertablebody').append('<tr id="user' + id + '"></tr>');
		$('#user' + id).append('<td>' + id + '</td>');
		$('#user' + id).append('<td id="' + id + 'alias"></td>');
		$('#user' + id).append('<td id="' + id + 'email"></td>');
		$('#user' + id).append('<td id="' + id + 'password"></td>');
		$('#user' + id)
				.append(
						'<td id="'
								+ id
								+ 'admin"><input id="'
								+ id
								+ 'admininput" type="checkbox" disabled="disabled" checked="checked"></td>');
		$('#user' + id).append('<td id="' + id + 'function"></td>');
		functionstring = '<button type="button" title="Nutzer-Eigenschaften bearbeiten" class="btn btn-default btn-xs editUser" data-userid="'
				+ id
				+ '">'
				+ '<span class="glyphicon glyphicon-edit"></span>'
				+ '</button>'
				+ '&nbsp;'
				+ '<button type="button" title="Nutzer l&ouml;schen" class="btn btn-default btn-xs deleteUser" data-userid="'
				+ id
				+ '">'
				+ '<span class="glyphicon glyphicon-trash"></span>'
				+ '</button>';
		passw = "**********";
		this.replace(id, alias, email, passw, false, admin, functionstring);
	},

	/**
	 * remove table row by userid. the whole tr-element
	 * 
	 * @params {number} id the user id
	 * 
	 * @returns {undefined}
	 */
	removeUser : function(id) {
		$('#user' + id).remove();
	},

	/**
	 * changes uneditable User properties to editable inputs and adds shortcut
	 * handler
	 * 
	 * @params {number} id the user id
	 * 
	 * @returns {undefined}
	 */
	editUser : function(id) {
		alias = $('#' + id + 'alias').text();
		email = $('#' + id + 'email').text();
		passw = $('#' + id + 'password').text();
		admin = $('#' + id + 'admininput').is(':checked');
		aliasstring = '<input id="' + id + 'aliasform" type="text" value="'
				+ alias + '" autofocus>';
		emailstring = '<input id="' + id + 'emailform" type="email" value="'
				+ email + '">';
		passwstring = '<input id="' + id
				+ 'passwordform" type="password" placeholder="password">';
		functionstring = '<button id="saveform'
				+ id
				+ '" type="button" title="Nutzer-Eigenschaften speichern" class="btn btn-default btn-xs saveEditedUser" data-editeduserid="'
				+ id
				+ '" data-editeduseremail="'
				+ email
				+ '">'
				+ '<span class="glyphicon glyphicon-ok"></span>'
				+ '</button>'
				+ '&nbsp;'
				+ '<button type="button" title="Nutzer-Eigenschaften verwerfen" class="btn btn-default btn-xs dismissEditedUser" data-editeduserid="'
				+ id + '">'
				+ '<span class="glyphicon glyphicon-remove"></span>'
				+ '</button>';
		this.replace(id, aliasstring, emailstring, passwstring, true, admin,
				functionstring);
		this.addHandler(id, id + 'aliasform');
		this.addHandler(id, id + 'emailform');
		this.addHandler(id, id + 'passwordform');
		this.addHandler(id, id + 'admininput');
	},

	/**
	 * break User edit and reload User with old informations
	 * 
	 * @params {number} id the user id
	 * 
	 * @returns {undefined}
	 */
	breakEdit : function(id) {
		if ($('#newUserLi' + id).length > 0) { // id is in new User
			$('#newUserLi' + id).remove();
		} else { // id is in editable User
			var markers = {
				"id" : id
			};
			admins.reloadUserinfo(markers, id);
		}
	},

	/**
	 * shows the wait gif
	 * 
	 * @params {number} id the user id
	 * @params {???} idstring FIXME delete this
	 * 
	 * @returns {undefined}
	 */
	setWait : function(idstring, id) {
		$('#' + id + 'wait').css({
			"visibility" : "visible"
		});
	},

	/**
	 * hides the wait gif
	 * 
	 * @params {number} id the user id
	 * @returns {undefined}
	 */
	removeWait : function(id) {
		$('#' + id + 'wait').css({
			"visibility" : "hidden"
		});
	},

	/**
	 * tablesorter need updates after adding or modifing rows
	 * 
	 * @returns {undefined}
	 */
	updateTable : function() {
		var t = $('table');
		t.trigger('update');
		$("#usertable")
				.trigger("updateAll", [ admins.resort, admins.callback ]);
	},

	/**
	 * replaces a regular (not in edit mode / new user mode) user Informations
	 * with the given data
	 * 
	 * @param {number}
	 *            id
	 * @param {string}
	 *            alias
	 * @param {string}
	 *            email
	 * @param {string}
	 *            password
	 * @param {???}
	 *            adminenabled
	 * @param {???}
	 *            adminchecked
	 * @param {string}
	 *            functionstring
	 * 
	 * @returns {undefined}
	 */
	replace : function(id, alias, email, password, adminenabled, adminchecked,
			functionstring) {
		$('#' + id + 'alias').html(alias);
		$('#' + id + 'email').html(email);
		$('#' + id + 'password').html(password);
		$('#' + id + 'function')
				.html(
						functionstring
								+ '<img style="visibility:hidden; text-align:right;" id="'
								+ id
								+ 'wait" height="25px" src="@(routes.Assets.at("images/warte.gif"))');

		if (adminenabled)
			$('#' + id + 'admininput').removeAttr("disabled");
		else
			$('#' + id + 'admininput').attr("disabled", "disabled");
		if (adminchecked || adminchecked === "true") {
			$('#' + id + 'admininput').prop("checked", true);
		} else
			$('#' + id + 'admininput').removeAttr("checked");

		this.updateTable();
	},

	/**
	 * updates User li-element to edited data.
	 * 
	 * @param {number}
	 *            id user id
	 * @param {object}
	 *            markers data
	 * 
	 * @returns {undefined}
	 */
	updateEdits : function(markers, id) {
		passw = "**********";
		functionstring = '<button type="button" title="Nutzer-Eigenschaften bearbeiten" class="btn btn-default btn-xs editUser" data-userid="'
				+ id
				+ '">'
				+ '<span class="glyphicon glyphicon-edit"></span>'
				+ '</button>'
				+ '&nbsp;'
				+ '<button type="button" title="Nutzer l&ouml;schen" class="btn btn-default btn-xs deleteUser" data-userid="'
				+ id
				+ '">'
				+ '<span class="glyphicon glyphicon-trash"></span>'
				+ '</button>';
		this.replace(id, markers.alias, markers.email, passw, false,
				markers.admin, functionstring);
	},

	/**
	 * Called on startup: Set event listeners and do initializations
	 * 
	 * @returns {undefined}
	 */
	init : function() {

		$("table#usertable").tablesorter({
			sortList : [ [ 0, 0 ] ],
			headers : {
				3 : {
					sorter : false
				},
				4 : {
					sorter : false
				},
				5 : {
					sorter : false
				}
			}
		});

		$("#exportSqlResponse").prop('disabled', true);

		// handler for newUserLi Button
		$("body").on("click", "button.newUser", function() {
			admins.addNewUserLi();
		});
		// handler for saveNewUser Button
		$("body").on("click", "button.addNewUser", function() {
			id = $(this).data("newuserid");

			alias = $('#newUserLi' + id + 'alias').val();
			email = $('#newUserLi' + id + 'email').val();
			passw = $('#newUserLi' + id + 'password').val();
			admin = $('#newUserLi' + id + 'admin').is(':checked');
			var markers = {
				"alias" : alias,
				"email" : email,
				"password" : passw,
				"admin" : admin
			};

			admins.addNewUser(markers, id);
		});
		// handler for dismissNewUser Button
		$("body").on("click", "button.dismissNewUser", function() {
			id = $(this).data("newuserid");
			admins.breakEdit(id);
		});
		// handler for edit User Button
		$("body").on("click", "button.editUser", function() {
			id = $(this).data("userid");
			admins.editUser(id);
		});
		// handler for save Edits Button
		$("body").on("click", "button.saveEditedUser", function() {
			id = $(this).data("editeduserid");
			oldemail = $(this).data("editeduseremail");

			alias = $('#' + id + 'aliasform').val();
			email = $('#' + id + 'emailform').val();
			passw = $('#' + id + 'passwordform').val();
			admin = $('#' + id + 'admininput').is(':checked');

			var markers = {
				"alias" : alias,
				"email" : email,
				"password" : passw,
				"oldemail" : oldemail,
				"admin" : admin
			};

			admins.saveEdits(markers, id);
		});
		// handler for dismiss Edits Button
		$("body").on("click", "button.dismissEditedUser", function() {
			id = $(this).data("editeduserid");
			admins.breakEdit(id);
		});
		// handler for delete User Button
		$("body").on(
				"click",
				"button.deleteUser",
				function() {
					id = $(this).data("userid");

					showConfirmModal(function() {
						admins.setWait('#' + id + 'function', id);
						var markers = {
							"id" : id
						};
						admins.deleteUser(markers, id);
					}, unescape("Wirklich den Benutzer "
							+ $('#' + id + 'alias').text() + "/"
							+ $('#' + id + 'email').text() + " l%F6schen?"),
							function() {
							});
				});

		$("body").on("click", "button.performSqlQuery", function() {
			queryString = $("#sqlInputField").val();
			var markers = {
				"queryString" : queryString
			};
			admins.performSqlQuery(markers);
		});

		$("#sqlInputField").on("change keyup", function() {
			$("#exportSqlResponse").prop('disabled', true);
		});
	},

	// SERVER REQUEST FUNCTIONS

	// statuscodes: 200 / 400 / 500 used
	// http://www.playframework.com/documentation/2.0.1/api/java/constant-values.html#play.mvc.Http.Status.NOT_MODIFIED

	/**
	 * ajax-serverrequest to save edited User
	 * 
	 * @param {number}
	 *            id user id
	 * @param {object}
	 *            markers data
	 * @returns {undefined}
	 */
	saveEdits : function(markers, id) {
		$
				.ajax(
						{
							type : "POST",
							url : $('#ajaxroutes').data("saveedits"),
							contentType : "application/json; charset=utf-8",
							dataType : "json",
							data : JSON.stringify(markers),
							statusCode : {
								// success
								200 : function(data) {
									admins.updateEdits(markers, id);
									generalSuccess(data.responseText);
								},
								// badrequest
								400 : function(data) {
									generalAlert(data.responseText);
									admins.removeWait(id);
								},

								// internal server error
								500 : function(data) {
									generalAlert("internal server error! Ein serverseitiger Fehler ist aufgetreten.");
								}
							},
							beforeSend : function() {
								admins.setWait('#' + id + 'function', id);
							}
						}).done(function(data) {
				});
	},

	/**
	 * ajax-serverrequest to create new user
	 * 
	 * @param {number}
	 *            id user id
	 * @param {object}
	 *            markers data
	 * @returns {undefined}
	 */
	addNewUser : function(markers, id) {
		$
				.ajax({
					type : "POST",
					url : $('#ajaxroutes').data("addnewuser"),
					contentType : "application/json; charset=utf-8",
					dataType : "json",
					data : JSON.stringify(markers),
					statusCode : {
						// success
						200 : function(data) {
							admins.addUserLi(data.responseText, id);
						},
						// badrequest
						400 : function(data) {
							generalAlert(data.responseText);
						},

						// internal server error
						500 : function(data) {
							generalAlert("internal server error! Ein serverseitiger Fehler ist aufgetreten.");
						}
					},
					beforeSend : function() {
						admins.setWait('#newUser' + id + 'function', id);
					}
				});
	},

	/**
	 * ajax-serverrequest to reload a User
	 * 
	 * @param {number}
	 *            id user id
	 * @param {object}
	 *            markers data
	 * @returns {undefined}
	 */
	reloadUserinfo : function(markers, id) {
		$
				.ajax(
						{
							type : "POST",
							url : $('#ajaxroutes').data("reloaduserinfo"),
							contentType : "application/json; charset=utf-8",
							dataType : "json",
							data : JSON.stringify(markers),
							statusCode : {
								// success
								200 : function(data) {
									admins
											.replace(
													id,
													data.alias,
													data.email,
													"**********",
													false,
													data.admin,
													'<button type="button" title="Nutzer-Eigenschaften bearbeiten" class="btn btn-default btn-xs editUser" data-userid="'
															+ id
															+ '">'
															+ '<span class="glyphicon glyphicon-edit"></span>'
															+ '</button>'
															+ '&nbsp;'
															+ '<button type="button" title="Nutzer l&ouml;schen" class="btn btn-default btn-xs deleteUser" data-userid="'
															+ id
															+ '">'
															+ '<span class="glyphicon glyphicon-trash"></span>'
															+ '</button>');
								},
								// badrequest
								400 : function(data) {
									generalAlert("reload unsaved changes for User["
											+ id + "]:\n" + data.responseText);
								},

								// internal server error
								500 : function(data) {
									generalAlert("internal server error! Ein serverseitiger Fehler ist aufgetreten.");
								}
							},
							beforeSend : function() {
								admins.setWait('#' + id + 'function', id);
							}
						}).done(function(data) {
				});
	},

	/**
	 * ajax-serverrequest to delete specific user
	 * 
	 * @param {number}
	 *            id user id
	 * @param {object}
	 *            markers data
	 * @returns {undefined}
	 */
	deleteUser : function(markers, id) {
		$
				.ajax(
						{
							type : "POST",
							url : $('#ajaxroutes').data("deleteuser"),
							contentType : "application/json; charset=utf-8",
							dataType : "json",
							data : JSON.stringify(markers),
							statusCode : {
								// success
								200 : function(data) {
									admins.removeUser(id);
									generalSuccess(data.responseText);
								},
								// badrequest
								400 : function(data) {
									admins.removeWait(id);
									generalAlert(data.responseText);
								},

								// internal server error
								500 : function(data) {
									generalAlert("internal server error! Ein serverseitiger Fehler ist aufgetreten.");
								}
							},
							beforeSend : function() {
								admins.setWait('#' + id + 'function', id);
							}
						}).done(function(data) {
				});
	},

	/**
	 * Server request to perform SQL querys
	 * 
	 * @param {object}
	 *            markers data
	 * @returns {undefined}
	 */
	performSqlQuery : function(markers) {
		$("#sqlResponseTableDiv").html("bitte warten ...");

		$
				.ajax({
					type : "POST",
					url : $('#ajaxroutes').data("performsqlquery"),
					contentType : "application/json; charset=utf-8",
					dataType : "json",
					data : JSON.stringify(markers),
					statusCode : {
						200 : function(result) {
							// alert($("#sqlInputField").val())
							// alert(JSON.stringify(result))
							var sqlResponseTable = Mustache
									.to_html($('#sqlResponseTableTemplate')
											.html(), result);
							$("#sqlResponseTableDiv").html(sqlResponseTable);
							$("#exportSqlResponse").prop('disabled', false);
						},
						// badrequest
						400 : function(data) {
							$("#sqlResponseTableDiv").html("");
							$("#exportSqlResponse").prop('disabled', true);
							generalAlert(data.responseText);
						},

						// internal server error
						500 : function(data) {
							generalAlert("internal server error! Ein serverseitiger Fehler ist aufgetreten.");
						}
					}
				});
	}
};
