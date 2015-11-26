/*
 Copyright (c) 2012 GFT Appverse, S.L., Sociedad Unipersonal.

 This Source Code Form is subject to the terms of the Appverse Public License 
 Version 2.0 (â€œAPL v2.0â€�). If a copy of the APL was not distributed with this 
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
package org.appverse.web.framework.backend.security.oauth2.authserver.autoconfigure;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for OAuth2 to protect your API
 */
@Configuration
@ConditionalOnProperty(value="appverse.frontfacade.oauth2.apiprotection.enabled", matchIfMissing=false)
@ComponentScan("org.appverse.web.framework.backend.security.oauth2.authserver.configuration")
public class OAuth2ServerAutoConfiguration {
	
	/**
	 * Tomcat request dumper.
	 * By default disabled. Coditional on running in Tomcat.
	 * appverse-web-modules-frontfacade-mvc adds this filter also, however you might implement
	 * an auth provider without using the appverse-web-modules-frontfacade-mvc starter.
	 * This is the reason why the filter setup is added here also.
	 */
	@Bean
	@ConditionalOnProperty(value="appverse.frontfacade.rest.debug.requestdumper.enabled", matchIfMissing=false)
	@ConditionalOnClass(org.apache.catalina.filters.RequestDumperFilter.class)
	public FilterRegistrationBean someFilterRegistration() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(requestDumperFilter());
		return registration;
	}

	@Bean(name = "requestDumperFilter")
	@ConditionalOnProperty(value="appverse.frontfacade.rest.debug.requestdumper.enabled", matchIfMissing=false)
	@ConditionalOnClass(org.apache.catalina.filters.RequestDumperFilter.class)
	public org.apache.catalina.filters.RequestDumperFilter requestDumperFilter() {
		return new org.apache.catalina.filters.RequestDumperFilter();
	}

}