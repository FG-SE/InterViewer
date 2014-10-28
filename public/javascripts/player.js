/**
 * @fileOverview: JavaScript file which contains the logic of the audio player
 *                all functions to play/pause the file and to handle the zoom
 *                feature are defined here
 * 
 * @author Florian
 * @namespace
 */
var audio = {
	/** current zoom level of the player. 0 = whole file */
	zoomLevel : 0,

	/** easy setting the maximum zoom level */
	maximumZoomLevel : 3,

	/** variables to save the currently shown time window in the progress bar */
	lowerTime : 0,
	upperTime : 0,

	/**
	 * Check if the player has to jump to the beginning after the ended event
	 * was triggered
	 */
	timeUpdated : false,

	/**
	 * toggle the audios state between play and pause and change the buttons
	 * 
	 * @returns {undefined}
	 */
	toggleAudio : function() {
		// check if HTML5 audio is supported
		if (window.HTMLAudioElement) {
			try {
				var oAudio = document.getElementById('interViewerAudio');

				// if audio is already paused then continue playing audio
				if (oAudio.paused) {
					oAudio.play();
					$("#playBtn").removeClass("glyphicon-play").addClass(
							"glyphicon-pause");
				}
				// else pause audio
				else {
					oAudio.pause();
					$("#playBtn").removeClass("glyphicon-pause").addClass(
							"glyphicon-play");
				}
			} catch (e) {
				generalAlert("Es ist ein Fehler beim Abspielen der Audio-Datei aufgetreten.");
				if (window.console && console.error("Error: " + e))
					;
			}
		}
	},

	/**
	 * pauses the audio and change the buttons
	 * 
	 * @returns {undefined}
	 */
	pauseAudio : function() {
		if (window.HTMLAudioElement) {
			try {
				var oAudio = document.getElementById('interViewerAudio');
				if (!oAudio.paused) {
					oAudio.pause();
					$("#playBtn").removeClass("glyphicon-pause").addClass(
							"glyphicon-play");
				} else
					console.log("Tried to pause audio but it was paused yet.");
			} catch (e) {
				generalAlert("Es ist ein Fehler beim Pausieren der Audio-Datei aufgetreten.");
				if (window.console && console.error("Error: " + e))
					;
			}
		}
	},

	/**
	 * set audios state to playing
	 * 
	 * @returns {undefined}
	 */
	playAudio : function() {
		if (window.HTMLAudioElement) {
			try {
				var oAudio = document.getElementById('interViewerAudio');
				if (oAudio.paused) {
					oAudio.play();
					$("#playBtn").removeClass("glyphicon-play").addClass(
							"glyphicon-pause");
				} else
					console
							.log("Tried to continue playing audio but it is playing yet.");
			} catch (e) {
				generalAlert("Es ist ein Fehler beim Abspielen der Audio-Datei aufgetreten.");
				if (window.console && console.error("Error: " + e))
					;
			}
		}
	},

	/**
	 * Set current Time -5 seconds. Called by clicking on "<<" button
	 * 
	 * @returns {undefined}
	 */
	returnSec : function() {
		// check if HTML5 audio is supported
		if (window.HTMLAudioElement) {
			try {
				var oAudio = document.getElementById("interViewerAudio");
				oAudio.currentTime = oAudio.currentTime - 5.0;
			} catch (e) {
				generalAlert("Es ist ein Fehler beim Abspielen der Audio-Datei aufgetreten.");
				if (window.console && console.error("Error: " + e))
					;
			}
		}
	},

	/**
	 * Sets player time do the given seconds
	 * 
	 * @param {number}
	 *            seconds new player time in seconds
	 * @returns {undefined}
	 */
	setTime : function(seconds) {
		// check if HTML5 audio is supported
		if (window.HTMLAudioElement) {
			try {
				var oAudio = document.getElementById("interViewerAudio");
				oAudio.currentTime = seconds;

				color = "linear-gradient(rgb(66, 139, 202) 0px, rgb(48, 113, 169) 100%);";
				$('.progress-bar')
						.css(
								{
									'background-image' : 'linear-gradient(#A4C639 0px, #665D1E)',
									'background-color' : '#A4C639'
								});
				setTimeout(
						function() {
							$('.progress-bar')
									.css(
											{
												'background-image' : 'linear-gradient(rgb(66, 139, 202) 0px, rgb(48, 113, 169) 100%)',
												'background-color' : 'rgb(66, 139, 202)'
											});
						}, 800);

			} catch (e) {
				generalAlert("Der Audio-Player konnte nicht zu der Zeitstelle springen.");
				if (window.console && console.error("Error: " + e))
					;
			}
		}
	},

	/**
	 * Convert seconds into time string (mm:ss)
	 * 
	 * @param {number}
	 *            time in seconds
	 * @returns {string} e.g. 21:13
	 */
	convertSecToDate : function(seconds) {

		// calculate the number of hours
		var timeHours = Math.floor(seconds / 3600);

		// format the number of hours
		if (timeHours <= 0)
			timeHours = "";
		else if (timeHours < 10)
			timeHours = "0" + timeHours + ":";
		else
			timeHours += ":";

		// subtract timeHours * 3600 from seconds
		seconds = seconds % 3600;

		// calculate number of minutes and seconds
		var timeMinutes = Math.floor(seconds / 60);
		var timeSeconds = Math.floor(seconds % 60);

		// format as string
		return timeHours
				+ ((timeMinutes < 10) ? ("0" + timeMinutes) : timeMinutes)
				+ ":"
				+ ((timeSeconds < 10) ? ("0" + timeSeconds) : timeSeconds);
	},

	/**
	 * Set the listener on the play button to toggle the audio
	 * 
	 * @returns {undefined}
	 */
	setPlayListener : function() {
		var _this = this;
		$("#audioPlayButton").click(function() {
			_this.toggleAudio();
		});
	},

	/**
	 * Called on startup: Set event listeners and initialize audio
	 * 
	 * @returns {undefined}
	 */
	init : function() {
		// check if HTML5 audio is supported
		if (window.HTMLAudioElement) {
			try {
				// contains the HTML audio element
				var oAudio = document.getElementById("interViewerAudio");

				// variable to access the defined variables(see top) and
				// functions
				// it is NOT possible to access these in the event listener by
				// using "this"(it return the object on which the listener is
				// placed
				var _this = this;

				_this.setPlayListener();

				// Set event listener on HTML5-audio 'timeupdate'
				$("#interViewerAudio")
						.on(
								'timeupdate',
								/**
								 * perform update operations
								 * 
								 * @returns {undefined}
								 */
								function() {
									// Calculate progress of progressBar
									var percentage = (100 / (_this.upperTime - _this.lowerTime))
											* (oAudio.currentTime - _this.lowerTime);

									var oldLowerTime = _this.lowerTime;

									// Scroll progressBar if zoomed in
									if (percentage > 100 && _this.zoomLevel > 0) {
										var duration = _this.upperTime
												- _this.lowerTime;
										_this.setprogressBar(percentage - 100);
										_this.lowerTime = _this.upperTime;

										// check if the new time frame reaches
										// the end of the audio
										if (_this.upperTime + duration < oAudio.duration)
											_this.upperTime += duration;
										else {
											_this.upperTime = oAudio.duration;
										}

										// show new time frame
										$("#lowerTime")
												.html(
														_this
																.convertSecToDate(_this.lowerTime));
										$("#upperTime")
												.html(
														_this
																.convertSecToDate(_this.upperTime));
										document
												.getElementById("showCurrentTime").innerHTML = _this
												.convertSecToDate(oAudio.currentTime);

										// check if an calculation error occured
										if (Math.abs(_this.upperTime
												- _this.lowerTime) < 1) {
											_this.lowerTime = oldLowerTime;
											_this.updateTime(_this.lowerTime,
													_this.upperTime);
										}
									} else if (percentage < 0
											&& _this.zoomLevel > 0) {
										// scroll backwards

										var duration = _this.upperTime
												- _this.lowerTime;
										_this.upperTime = _this.lowerTime;
										_this.lowerTime -= duration;

										// prevent negative times
										if (_this.lowerTime < 0) {
											var negative = Math
													.abs(_this.lowerTime);
											_this.lowerTime = 0;
											_this.upperTime += _this.lowerTime;
										}

										// show new time frame
										_this.updateTime(_this.lowerTime,
												_this.upperTime);
									}
									// Set current time
									else {
										_this.setprogressBar(percentage);
										document
												.getElementById("showCurrentTime").innerHTML = _this
												.convertSecToDate(oAudio.currentTime);
									}
									timeUpdated = true;
								});

				$("#interViewerAudio")
						.on(
								'ended',
								/**
								 * Change Play/Pause-Button when the audio file
								 * ends
								 * 
								 * @returns {undefined}
								 */
								function() {

									// switch play/pause Button to pause state
									if ($("#playBtn").hasClass(
											"glyphicon-pause")) {
										$("#playBtn").removeClass(
												"glyphicon-pause").addClass(
												"glyphicon-play");
									}
									$("#progressBar").width("100%");
									timeUpdated = false;

									$("#audioPlayButton").off("click");

									// new click event to check if the audio has
									// to restart from beginning or if the user
									// manipulated the time
									$("#audioPlayButton")
											.on(
													'click',
													function() {

														// user manipulated the
														// time
														if (timeUpdated) {
															_this.playAudio();
														} else {
															// start from
															// beginning

															if (_this.upperTime
																	- _this.lowerTime) {
																_this.upperTime = _this.upperTime
																		- _this.lowerTime;
																_this.lowerTime = 0;

																// remove this
																// event handler
																$(this)
																		.off(
																				'click');
																_this
																		.updateTime(
																				_this.lowerTime,
																				_this.upperTime);
																_this
																		.playAudio();
																timeUpdated = false;
															}

														}

														// set default event
														// listener on
														// play/pause button
														_this.setPlayListener();

													});
								});

				$(".progress").click(
						/**
						 * Event listener to get relative position of the mouse
						 * on the progressBar
						 * 
						 * @param {object}
						 *            e the event
						 * @returns {undefined}
						 */
						function(e) {
							if (!$(".controls").attr("disabled")) {
								var currentDuration = _this.upperTime
										- _this.lowerTime;

								// Calculate relative position of cursor on the
								// progressBar
								var offset = $(this).position().left;
								var relX = e.pageX - offset;

								// Set progressBar width
								var barWidth = $(".progress").width();

								// Calculate progress of the progressBar.
								// Depends on the width of the progressBar
								var progress = (relX / barWidth);

								// Update time in HTML5-audio
								// If zoomed in, currentDuration is not
								// audio.currentTime
								oAudio.currentTime = _this.lowerTime
										+ currentDuration * progress;
							}
						});

				$('#interViewerAudio')
						.on(
								'error',
								/**
								 * Handle Errors on audio element
								 * 
								 * @param {object}
								 *            e the event
								 * @returns {undefined}
								 */
								function(e) {
									switch (e.target.error.code) {
									case e.target.error.MEDIA_ERR_NETWORK:
										generalAlert("Es ist ein Netzwerk-Fehler beim Laden der Audio-Datei aufgetreten.");
										break;
									case e.target.error.MEDIA_ERR_DECODE:
										generalAlert("Die Audio-Datei konnte nicht wiedergegeben werden. M&ouml;glicherweise unterst&uuml;tzt Ihr Browser das Mp3-Dateiformat nicht.");
										break;
									case e.target.error.MEDIA_ERR_SRC_NOT_SUPPORTED:
										generalAlert("Die Audio-Quelle wird nicht unterst&uuml;tzt.");
										break;
									default:
										generalAlert('Die Audio-Datei kann nicht wiedergegeben werden.');
										break;
									}
								});

				$("#interViewerAudio")
						.on(
								'canplay',
								/**
								 * Initialize shown duration after the browser
								 * can play the audio file (otherwise IE and
								 * Chrome (,...) fail)
								 * 
								 * @returns {undefined}
								 */
								function() {
									if ($(".controls").attr("disabled")) {
										// Global variables to compute the real
										// duration
										_this.upperTime = oAudio.duration;
										_this.lowerTime = 0;

										// Set duration
										document.getElementById("upperTime").innerHTML = _this
												.convertSecToDate(oAudio.duration);
									}

									// unblock control buttons
									$(".controls").removeAttr("disabled");
								});
			} catch (e) {
				generalAlert("Bei der Initialisierung des Audio-Players ist ein Fehler aufgetreten.");
				if (window.console && console.error("Error: " + e))
					;
			}
		}
	},

	/**
	 * calculates the new value of the progress bar
	 * 
	 * @param {number}
	 *            lowerTime i.e. depending on current zoom level
	 * @param {number}
	 *            lowerTime i.e. depending on current zoom level
	 * @returns {undefined}
	 */
	updateProgressBar : function(lowerTime, upperTime) {
		// check if HTML5 audio is supported
		if (window.HTMLAudioElement) {
			try {
				var oAudio = document.getElementById("interViewerAudio");

				// Calculate progress of progressBar
				var percentage = ((oAudio.currentTime - this.lowerTime) / (this.upperTime - this.lowerTime)) * 100;

				// set new value
				this.setprogressBar(percentage);
			} catch (e) {
				generalAlert("Es ist ein Fehler beim Abspielen der Audio-Datei aufgetreten.");
				if (window.console && console.error("Error: " + e))
					;
			}
		}
	},

	/**
	 * Accesses the progress bar in DOM and sets its values
	 * 
	 * @param {number}
	 *            percentage between 0 and 100
	 * @returns {undefined}
	 */
	setprogressBar : function(percentage) {
		var oAudio = document.getElementById("interViewerAudio");

		$("#progressBar").width(percentage + "%");
		$("#showCurrentTime").html(this.convertSecToDate(oAudio.currentTime));
	},

	/**
	 * Convert given time frame in seconds to date string and insert it into DOM
	 * 
	 * @param {number}
	 *            lowerTime i.e. depending on current zoom level
	 * @param {number}
	 *            lowerTime i.e. depending on current zoom level
	 * @returns {undefined}
	 */
	updateTime : function(lowerTime, upperTime) {
		// Do fade-effect
		$("#lowerTime").fadeToggle(400);
		$("#upperTime").fadeToggle(400);

		$("#lowerTime").html(this.convertSecToDate(this.lowerTime));
		$("#upperTime").html(this.convertSecToDate(this.upperTime));

		this.updateProgressBar(this.lowerTime, this.upperTime);

		// Do fade-effect
		$("#lowerTime").fadeToggle(400);
		$("#upperTime").fadeToggle(400);
	},

	/**
	 * returns the current time of the audio
	 * 
	 * @returns {undefined|???} the current time if possible
	 */
	getCurrentTime : function() {
		if (window.HTMLAudioElement) {
			try {
				var oAudio = document.getElementById("interViewerAudio");

				return oAudio.currentTime;

			} catch (e) {
				generalAlert("Es ist ein Fehler beim Abspielen der Audio-Datei aufgetreten.");
				if (window.console && console.error("Error: " + e))
					;
			}
		}
	},

	/**
	 * zooms one level in the current time frame of the audio
	 * 
	 * @returns {undefined}
	 */
	zoomIn : function() {
		// check if HTML5 audio is supported
		if (window.HTMLAudioElement) {
			try {
				var oAudio = document.getElementById("interViewerAudio");

				// Control maximum zoom level
				if (this.zoomLevel < this.maximumZoomLevel) {

					var currentDuration = this.upperTime - this.lowerTime;

					// current time position < 1/4 of currentDuration
					if (oAudio.currentTime - (currentDuration / 4) < this.lowerTime) {
						this.upperTime = this.lowerTime + currentDuration / 2;
					}

					// currentTime position > 3/4 of currentDuration
					else if (oAudio.currentTime + (currentDuration / 4) > this.upperTime) {
						this.lowerTime = this.lowerTime + currentDuration / 2;
					}

					// if currentTime is in ~middle of file
					// then decrease time window to currentDuration/2 on both
					// sides
					else {
						this.lowerTime = oAudio.currentTime
								- (currentDuration / 4);
						this.upperTime = oAudio.currentTime
								+ (currentDuration / 4);
					}

					// one zoom level divides the shown duration in halves
					currentDuration /= 2;

					// Update shown start time, end time and progressBar
					this.updateTime(this.lowerTime, this.upperTime);

					// Inkrement zoom level counter
					this.zoomLevel++;
				} else {

					// show warning if the user reached the maximum zoom level
					$("#zoomLevelWarning span").html(
							"Das maximale zoom level ist erreicht.");
					$("#zoomLevelWarning").animate({
						opacity : 1
					}, 500);

					// fade out the warning
					setTimeout(function() {
						$("#zoomLevelWarning").animate({
							opacity : 0
						}, 1000);
					}, 1500);
				}
			} catch (e) {
				generalAlert("Der Zoom in konnte nicht korrekt durchgeführt werden.");
				if (window.console && console.error("Error: " + e))
					;
			}
		}
	},

	/**
	 * zooms one level out of the current time frame of the audio
	 * 
	 * @returns {undefined}
	 */
	zoomOut : function() {
		// check if HTML5 audio is supported
		if (window.HTMLAudioElement) {
			try {
				var oAudio = document.getElementById("interViewerAudio");

				// if the full length of the audio file is shown
				if (this.zoomLevel <= 0) {
					$("#zoomLevelWarning span").html(
							"Das minimale zoom level ist erreicht.");
					$("#zoomLevelWarning").animate({
						opacity : 1
					}, 500);

					// fade out
					setTimeout(function() {
						$("#zoomLevelWarning").animate({
							opacity : 0
						}, 1000);
					}, 1500);

					return;
				}

				// update interal zoom level variable
				this.zoomLevel--;

				// set the full length of the audio file if reached the normal
				// view
				if (this.zoomLevel == 0) {
					this.lowerTime = 0;
					this.upperTime = oAudio.duration;
				} else {
					var currentDuration = this.upperTime - this.lowerTime;

					// if upperTime window + half of currentDuration is greater
					// than the length of the audio file
					// then set the upperTime to max and the lowerTime to
					// lowerTime - half of currentDuration + upper overlap time
					// new time windows is double of old
					if ((this.upperTime + currentDuration / 2) > oAudio.duration) {
						this.lowerTime -= ((this.upperTime + currentDuration / 2) - oAudio.duration)
								+ currentDuration / 2;
						this.upperTime = oAudio.duration;
					}

					// if this.lowerTime window - half of currentDuration is
					// lower than 0
					// then set the lowerTime to 0 and the upperTime to
					// upperTime + half of currentDuration + lower overlap time
					// new time windows is double of old
					else if ((this.lowerTime - currentDuration / 2) < 0) {
						this.lowerTime = 0;
						this.upperTime += -(this.lowerTime - currentDuration / 2)
								+ currentDuration / 2;
					}

					// if currentTime is in ~middle of file
					// then increase time window to currentDuration/2 on both
					// sides
					else {
						this.lowerTime -= currentDuration / 2;
						this.upperTime += currentDuration / 2;
					}
				}

				// update shown times
				this.updateTime(this.lowerTime, this.upperTime);

			} catch (e) {
				generalAlert("Der Zoom out konnte nicht korrekt durchgeführt werden.");
				if (window.console && console.error("Error: " + e))
					;
			}
		}
	}
};