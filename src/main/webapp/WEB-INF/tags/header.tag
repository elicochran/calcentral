<!--[if lt IE 7]><p class=chromeframe>Your browser is <em>ancient!</em> <a href="http://browsehappy.com/">Upgrade to a different browser</a> or <a href="http://www.google.com/chromeframe/?redirect=true">install Google Chrome Frame</a> to experience this site.</p><![endif]-->
<header>
	<h1>Calcentral</h1>
	<nav class="cc-launcher cc-right"><!-- --></nav>

	<script id="cc-launcher-template" type="text/x-handlebars-template">
		<ul>
			<li class="cc-right">
				<a href="http://bmail.berkeley.edu" class="ir cc-left cc-icon cc-icon-mail" title="bMail">bMail</a>
				<a href="http://bcal.berkeley.edu" class="ir cc-left cc-icon cc-icon-calendar" title="bCal">bCal</a>
				<a href="http://bdrive.berkeley.edu" class="ir cc-left cc-icon cc-icon-drive" title="bDrive">bDrive</a>
				{{#if loggedIn}}
					<a href="/secure/dashboard" class="cc-launcher-info" aria-haspopup="true">{{preferredName}} <span class="cc-launcher-icon-dropdown">&#x25BC;<span></a>
					<div class="cc-launcher-dropdown">
						<ul>
							<li>
								<a href="/secure/preferences">Preferences</a>
							</li>
							<li>
								<a href="/secure/profile">Profile</a>
							</li>
							<li>
								<a href="/secure/logout">Sign out</a>
							</li>
						</ul>
					</div>
				{{else}}
					<a href="/secure/dashboard" class="cc-launcher-info">Sign in</a>
				{{/if}}
			</li>
		</ul>
	</script>

</header>
