/*
 Copyright (c) 2012 GFT Appverse, S.L., Sociedad Unipersonal.

 This Source Code Form is subject to the terms of the Appverse Public License 
 Version 2.0 (“APL v2.0�?). If a copy of the APL was not distributed with this 
 file, You can obtain one at http://www.appverse.mobi/licenses/apl_v2.0.pdf. [^]

 Redistribution and use in source and binary forms, with or without modification, 
 are permitted provided that the conditions of the AppVerse Public License v2.0 
 are met.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. EXCEPT IN CASE OF WILLFUL MISCONDUCT OR GROSS NEGLIGENCE, IN NO EVENT
 SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT(INCLUDING NEGLIGENCE OR OTHERWISE) 
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 POSSIBILITY OF SUCH DAMAGE.
 */
package org.appverse.web.framework.backend.frontfacade.mvc.swagger.autoconfigure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;


import org.appverse.web.framework.backend.frontfacade.mvc.swagger.provider.EurekaSwaggerResourcesProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import org.thymeleaf.spring4.util.SpringVersionUtils;
import org.thymeleaf.util.StringUtils;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spi.service.contexts.SecurityContextBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * This class holds default swagger configuration using Swagger Sprinfox API.
 * You can enable / disable this autoconfiguration by using the property
 * "appverse.frontfacade.swagger.enabled" which is false by default.
 * 
 * It creates by default a default group showing all your API (the patterns you have included).
 * It provides support both for basic auth and OAuth2 using a login endpoint.
 * Properties:
 * - appverse.frontfacade.swagger.enabled: allows enable / disable Appverse Swagger feature
 * - appverse.frontfacade.swagger.oauth2.scopes: scopes offered to the swagger user to authenticate with oauth2
 * - appverse.frontfacade.swagger.oauth2.clientId: the cliendId oauth2 parameter used to authenticate using swagger with oauht2 enabled 
 * 
 * Example of more complex setup:
 * https://github.com/springfox/springfox/blob/master/springfox-spring-config/src/main/java/springfox/springconfig/Swagger2SpringBoot.java
 */
@EnableSwagger2
@Configuration
@ConditionalOnProperty(value="appverse.frontfacade.swagger.enabled", matchIfMissing=false)
public class SwaggerDefaultSetup implements EnvironmentAware {
	
	public static final String SECURITY_SCHEMA_OAUTH2 = "oauth2schema";
	public static final String SCOPES_SEPARATOR = ","; 
	
    @Value("${appverse.frontfacade.rest.api.basepath:/api}")
    private String apiPath;
	@Value("${appverse.frontfacade.swagger.host:}")
	private String swaggerHost;
	@Value("${appverse.frontfacade.swagger.tags:}")
	private String tags;
	private RelaxedPropertyResolver propertyResolver;
	//oauth2
    @Value("${appverse.frontfacade.swagger.oauth2.support.enabled:false}")
    private boolean swaggerOauth2SupportEnabled;
	@Value("${appverse.frontfacade.swagger.oauth2.clientId:}")
	private String swaggerClientId;
	@Value("${appverse.frontfacade.swagger.oauth2.loginEndpoint:swaggeroauth2login}")
	private String swaggerOAuth2LoginEndpoint;



	@Override
	public void setEnvironment(Environment environment) {
		this.propertyResolver = new RelaxedPropertyResolver(environment, "appverse.frontfacade.swagger.");
	}
	
	@Bean
	@ConditionalOnProperty(value="appverse.frontfacade.swagger.oauth2.support.enabled", matchIfMissing=false)
	public SecurityConfiguration securityConfiguration(){
		SecurityConfiguration config = new SecurityConfiguration(swaggerClientId, "NOT_USED", "oauth2-resource", swaggerClientId, "apiValue", ApiKeyVehicle.HEADER, "apiKey", SCOPES_SEPARATOR);
		return config;
	}


	@Bean
	@ConditionalOnProperty(value="appverse.frontfacade.swagger.docket.enable", matchIfMissing=true)
	public Docket apiDocumentationV2Security() {
		Docket docket =  new Docket(DocumentationType.SWAGGER_2);
		if (!StringUtils.isEmpty(swaggerHost)){
			docket.host(swaggerHost);
		}
		docket.groupName("default-group").apiInfo(apiInfo()).select().paths(defaultGroup()).build();
		if (swaggerOauth2SupportEnabled) {
			// This causes duplicated contextpath in Swagger UI 
			// .pathMapping(apiPath)
			docket.securitySchemes(Arrays.asList(securitySchema()))
					.securityContexts(Arrays.asList(securityContext()));
		}

		return docket;
	}
	@Bean
	@Primary
	@ConditionalOnProperty(value="appverse.frontfacade.swagger.eureka.enable", matchIfMissing=false)
	public SwaggerResourcesProvider customResourcesProvider(){
		return new EurekaSwaggerResourcesProvider();
	}
		
	private OAuth securitySchema() {		
		// TODO: LoginEndpoint needs to be parametrizable.
		// The following works, we click the on swagger oauth2 switch but the problem is that as the 
		// user is already authenticated (zuul redirects) then is automatically propagated. 
		// Later there is a token mismatch.
		// Possible solutions:
		// 	1. Leave swagger-ui.html open so is not oauth2 protected, and thus Zuul will not redirect to the auth server. Then with swagger the user
		//     will authenticate when clicking the swicth. -> This allows to test with swagger changing users easily, disadvantage: swagger-ui.html is opened, not protected
		// (eventhough nobody will be able to do anything without authenticating)
		//	2. Disable swagger oauth2 and see if Zuul proxy propagates well the headers and then everything works -> disadvantage: not easy to change users 
		// to test with swagger, you need to logout completely...
		LoginEndpoint loginEndpoint = new LoginEndpoint(swaggerOAuth2LoginEndpoint);
		GrantType grantType = new ImplicitGrant(loginEndpoint, "access_token");
		return new OAuth(SECURITY_SCHEMA_OAUTH2, Arrays.asList(getOauth2Scopes()), Arrays.asList(grantType));
	}

	private SecurityContext securityContext() {
		SecurityContextBuilder builder = SecurityContext.builder();
		if (swaggerOauth2SupportEnabled){
			List<SecurityReference> defaultOAuthSecurityReference = Arrays.asList(new SecurityReference(SECURITY_SCHEMA_OAUTH2, getOauth2Scopes()));
			if (defaultOAuthSecurityReference != null){
				builder.securityReferences(defaultOAuthSecurityReference);
			}
		}
		return builder.forPaths(defaultGroup()).build();
	}
	
	private AuthorizationScope[] getOauth2Scopes() {
		String[] scopes = getSwaggerScopes();
		AuthorizationScope[] authorizationScopes = null;
		if (scopes != null) {
			authorizationScopes = new AuthorizationScope[scopes.length];
			int cnt=0;
			for (String scope:scopes){				
				AuthorizationScope authScope = new AuthorizationScope(scope, "");
				authorizationScopes[cnt] = authScope;
				cnt++;
			}
		}
		return authorizationScopes;
	}	
	
	@SuppressWarnings("unchecked")
	private Predicate<String> defaultGroup() {
		String[] includePatterns = getIncludePatterns();
		List<Predicate<String>> predicateList = new ArrayList<Predicate<String>>();	    	
		if (includePatterns != null) {
			for (String pattern:includePatterns){
				predicateList.add(PathSelectors.regex(pattern));
			}	    		
			return Predicates.or(predicateList);
		}
		else{
			return Predicates.or(PathSelectors.regex("/*"));
		}
	}

	private ApiInfo apiInfo() {
		Contact contact = new Contact(propertyResolver.getProperty("contact.name"),propertyResolver.getProperty("contact.url"),propertyResolver.getProperty("contact.email"));

		return new ApiInfoBuilder()
				.title(propertyResolver.getProperty("title"))
				.description(propertyResolver.getProperty("description"))
				.termsOfServiceUrl(propertyResolver.getProperty("termsOfServiceUrl"))
				.contact(contact)
				.license(propertyResolver.getProperty("license"))
				.licenseUrl(propertyResolver.getProperty("licenseUrl"))
				.version("version")
				.build();
	}
	
	private String[] getSwaggerScopes(){
		String includePatterns = propertyResolver.getProperty("oauth2.scopes");
		if (includePatterns == null){
			return null;
		}
		return includePatterns.split(",");		 
	}

	private String[] getIncludePatterns(){
		String includePatterns = propertyResolver.getProperty("defaultGroupIncludePatterns");
		if (includePatterns == null){
			return null;
		}
		return includePatterns.split(",");		 
	}

}