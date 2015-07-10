/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.appverse.web.framework.backend.frontfacade.rest.autoconfigure;

import org.appverse.web.framework.backend.frontfacade.rest.authentication.basic.configuration.AppverseBasicAuthenticationConfigurerAdapter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Front Facade module
 */
@Configuration
@ConditionalOnClass(FrontFacadeRestAutoConfiguration.class)
@ComponentScan("org.appverse.web.framework.backend.frontfacade.rest")
public class FrontFacadeRestAutoConfiguration {
	
	@Configuration
	@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
	@ConditionalOnProperty(value="appverse.frontfacade.rest.basicAuthentication.enabled", matchIfMissing=true)
	protected static class AppverseWebBasicAuthConfiguration extends AppverseBasicAuthenticationConfigurerAdapter {		
	}	
}
