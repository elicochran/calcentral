<div class="cc-container-main-left"><!-- --></div>
<script id="cc-container-main-left-template" type="text/x-handlebars-template">
	<section class="cc-entity">
		{{#with profile}}
		<div class="cc-entity-name"><strong class="cc-entity-name-nolink">{{preferredName}}</strong></div>
		{{/with}}
	</section>
	<nav class="cc-lefthandnavigation">
		<ul>
			{{#each pages}}
				<li><a href="{{url}}"{{#compare url ../pathname}} class="cc-lefthandnavigation-item-selected"{{/compare}}>{{title}}</a></li>
			{{/each}}
		</ul>
	</nav>
</script>
