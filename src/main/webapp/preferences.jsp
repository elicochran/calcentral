<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>

<tags:head/>

<body class="cc-page-preferences">
<tags:header/>
<tags:topnavigation/>
<div class="cc-container-main" role="main">
	<!-- Page specific HTML -->
		<div class="cc-container-widgets">
			<jsp:include page="widgets/oAuthToggle/oAuthToggle.html" />
		</div>
	<!-- END Page specific HTML -->
	<br class="clearfix" />
</div>
<tags:footer/>
</body>
</html>