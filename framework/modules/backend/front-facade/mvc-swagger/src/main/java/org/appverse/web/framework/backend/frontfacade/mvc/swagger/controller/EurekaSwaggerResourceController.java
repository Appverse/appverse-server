package org.appverse.web.framework.backend.frontfacade.mvc.swagger.controller;/*
 Copyright (c) 2012 GFT Appverse, S.L., Sociedad Unipersonal.

 This Source Code Form is subject to the terms of the Appverse Public License 
 Version 2.0 (“APL v2.0”). If a copy of the APL was not distributed with this 
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import springfox.documentation.swagger.web.SwaggerResource;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@ConditionalOnProperty(value="appverse.frontfacade.swagger.eureka.enabled", matchIfMissing=false)
public class EurekaSwaggerResourceController {
    @Value("${appverse.frontfacade.swagger.eureka.default.group:default-group}")
    private String baseSwaggerDefaultGroup;
    @Value("${appverse.frontfacade.swagger.eureka.default.url:/v2/api-docs?group=default-group}")
    private String baseSwaggerDefaultUrl;
    @Value("${appverse.frontfacade.swagger.eureka.default.version:2.0}")
    private String BASE_SWAGGER_VERSION;


    @Value("${appverse.frontfacade.swagger.eureka.exclusions}")
    private Optional<List<String>> eurekaSkipServices;

    @Autowired(required = false)
    private DiscoveryClient discoveryClient;

    @RequestMapping(value = "/swagger-resources", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SwaggerResource> obtainServices(HttpServletRequest request){
        List<SwaggerResource> resources = new ArrayList<SwaggerResource>();
        if (discoveryClient != null) {
            List<String> services = discoveryClient.getServices();
            UriComponents current = ServletUriComponentsBuilder.fromRequest(request).build();
            for (String service : services) {
                if (!eurekaSkipServices.isPresent() || !eurekaSkipServices.get().contains(service)) {
                    List<ServiceInstance> instances = discoveryClient.getInstances(service);
                    if (instances.size() != 0) {
                        String urlLocation = baseSwaggerDefaultUrl;
                        ServiceInstance instance = instances.get(0);
                        String managementPath = "";
                        if (instance.getMetadata().containsKey("managementPath")) {
                            managementPath = instance.getMetadata().get("managementPath");
                        }
                        String hostUrl;
                        if (("https".equals(current.getScheme()) && 443 == current.getPort()) || ("http".equals(current.getScheme()) && 80 == current.getPort())) {
                            //default ports
                            hostUrl = String.format("%s://%s", current.getScheme(), current.getHost());
                        } else {
                            //custom ports
                            hostUrl = String.format("%s://%s:%d", current.getScheme(), current.getHost(), current.getPort());
                        }
                        urlLocation = hostUrl + managementPath + urlLocation;
                        SwaggerResource resource = new SwaggerResource();
                        resource.setName(service);
                        resource.setLocation(urlLocation);
                        resource.setSwaggerVersion(BASE_SWAGGER_VERSION);
                        resources.add(resource);
                    }
                }
            }
        }else{
            //return default swagger group
            SwaggerResource resource = new SwaggerResource();
            resource.setName(baseSwaggerDefaultGroup);
            resource.setLocation(baseSwaggerDefaultUrl);
            resource.setSwaggerVersion(BASE_SWAGGER_VERSION);
            resources.add(resource);
        }

        return resources;

    }


}
