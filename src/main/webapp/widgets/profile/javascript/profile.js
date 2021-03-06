var calcentral = calcentral || {};
calcentral.Widgets = calcentral.Widgets || {};
calcentral.Widgets.profile = function(tuid) {

	/*global console*/


	/////////////////////////////
	// Configuration variables //
	/////////////////////////////

	'use strict';

	var $rootel = $('#' + tuid);
	var $profileList = $('.cc-widget-profile-list', $rootel);
	var $preferredNameInput;
	var $linkInput;


	///////////////
	// Rendering //
	///////////////

	var renderProfile = function(success, data) {
		calcentral.Api.Util.renderTemplate({
			'container': $profileList,
			'data': data,
			'template': $('#cc-widget-profile-list-template', $rootel)
		});
		$preferredNameInput = $('#cc-widget-profile-preferredName', $rootel).on('blur', function() {
			saveProfile({
				'preferredName': $(this).val()
			});
		});
		$linkInput = $('#cc-widget-profile-link', $rootel).on('blur', function() {
			saveProfile({
				'link': $(this).val()
			});
		});
		$('#cc-widget-profile-imagelink', $rootel).on('blur', function() {
			saveProfile({
				'profileImageLink': $(this).val()
			});
		});
	};

	///////////////////
	// Ajax Requests //
	///////////////////

	var saveProfile = function(data) {
		var newUserData = {
			'uid': calcentral.Data.User.user.uid
		};
		$.extend(newUserData, data);
		console.log('Profile widget - Saving profile: ', newUserData);
		calcentral.Api.User.saveUser(newUserData, function(success) {
			console.log('Profile widget - Profile saved: ' + success);
		});
	};

	////////////////////
	// Initialisation //
	////////////////////

	/**
	 * Initialise the profile widget
	 */
	var doInit = function() {
		// We always want an updated version of the the current user on the profile page.
		calcentral.Api.User.getCurrentUser({
			'refresh': false
		}, renderProfile);
	};

	// Start the request
	doInit();
};
