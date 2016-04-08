$(function() {
	var springfox = {
			"baseUrl": function() {
				/* Appverse Server 
				 * We override this URLs setup so the default page is swagger-ui.html without having to 
				 * specify it explicitly */
				// var urlMatches = /(.*)\/swagger-ui.html.*/.exec(window.location.href);				
				var urlMatches = /(.*)\/.*/.exec(window.location.href);
				console.log('urlMatches: ', urlMatches);
				return urlMatches[1];
			},
			"securityConfig": function(cb) {
				console.log('urlMatches2: ', this.baseUrl());
				$.getJSON(this.baseUrl() + "/configuration/security", function(data) {
					cb(data);
				});
			},
			"uiConfig": function(cb) {
				console.log('urlMatches3: ', this.baseUrl());
				$.getJSON(this.baseUrl() + "/configuration/ui", function(data) {
					cb(data);
				});
			}
	};
	/* Appverse Server END */
	window.springfox = springfox;
	window.oAuthRedirectUrl = springfox.baseUrl() + '/webjars/springfox-swagger-ui/o2c.html';

	window.springfox.uiConfig(function(data) {
		window.swaggerUi = new SwaggerUi({
			dom_id: "swagger-ui-container",
			/* Appverse Server: Added XSRF token management */
			authorizations: {
				someName: function() {
					console.log('Config XSRF');
					// This function will get called /before/ each request
					// ... UNLESS you have a 'security' tag in the swagger.json file, in which case you must add 'someName' to the list of auths.
					var xsrfToken = getCookie("XSRF-TOKEN");  //Read xsrfToken cookie
					this.headers['X-XSRF-TOKEN'] = xsrfToken;
					return true; // there is a bug, fixed but not in develop_2.0 where returning true will only process _one_ interceptor, you can leave this if it's your only interceptor and when the fix is in, it'll work as expected
				}
			},      
			/* Appverse Server: Added XSRF token management END */
			validatorUrl: data.validatorUrl,
			supportedSubmitMethods: ['get', 'post', 'put', 'delete', 'patch'],
			onComplete: function(swaggerApi, swaggerUi) {

				initializeSpringfox();

				if (window.SwaggerTranslator) {
					window.SwaggerTranslator.translate();
				}

				$('pre code').each(function(i, e) {
					hljs.highlightBlock(e)
				});

			},
			onFailure: function(data) {
				log("Unable to Load SwaggerUI");
			},
			docExpansion: data.docExpansion || 'none',
			jsonEditor: data.jsonEditor || false,
			apisSorter: data.apisSorter || 'alpha',
			defaultModelRendering: data.defaultModelRendering || 'schema',
			showRequestHeaders: data.showRequestHeaders || true
		});

		initializeBaseUrl();

		function addApiKeyAuthorization() {
			var key = encodeURIComponent($('#input_apiKey')[0].value);
			if (key && key.trim() != "") {
				var apiKeyAuth = new SwaggerClient.ApiKeyAuthorization(window.apiKeyName, key, window.apiKeyVehicle);
				window.swaggerUi.api.clientAuthorizations.add(window.apiKeyName, apiKeyAuth);
				log("added key " + key);
			}
		}

		$('#input_apiKey').change(addApiKeyAuthorization);

		function log() {
			if ('console' in window) {
				console.log.apply(console, arguments);
			}
		}

		/* Appverse Server: Needed for XSRF token management */ 
		function getCookie(cookie) {
			return document.cookie.split(';').reduce(function(prev, c) {
				var arr = c.split('=');
				return (arr[0].trim() === cookie) ? arr[1] : prev;
			}, undefined);
		}            
		/* Appverse Server: Needed for XSRF token management END */

		function oAuthIsDefined(security) {
			return security.clientId
			&& security.clientSecret
			&& security.appName
			&& security.realm;
		}

		function initializeSpringfox() {
			var security = {};
			window.springfox.securityConfig(function(data) {
				security = data;
				window.apiKeyVehicle = security.apiKeyVehicle || 'query';
				window.apiKeyName = security.apiKeyName || 'api_key';
				if (security.apiKey) {
					$('#input_apiKey').val(security.apiKey);
					addApiKeyAuthorization();
				}
				if (typeof initOAuth == "function" && oAuthIsDefined(security)) {
					initOAuth(security);
				}
			});
		}
	});

	$('#select_baseUrl').change(function() {
		window.swaggerUi.headerView.trigger('update-swagger-ui', {
			url: $('#select_baseUrl').val()
		});
	});

	function maybePrefix(location, withRelativePath) {
		var pat = /^https?:\/\//i;
		if (pat.test(location)) {
			return location;
		}
		return withRelativePath + location;
	}

	function initializeBaseUrl() {
		var relativeLocation = springfox.baseUrl();

		$('#input_baseUrl').hide();

		$.getJSON(relativeLocation + "/swagger-resources", function(data) {

			var $urlDropdown = $('#select_baseUrl');
			$urlDropdown.empty();
			$.each(data, function(i, resource) {
				var option = $('<option></option>')
				.attr("value", maybePrefix(resource.location, relativeLocation))
				.text(resource.name + " (" + resource.location + ")");
				$urlDropdown.append(option);
			});
			$urlDropdown.change();
		});

	}

});


