package org.appverse.web.framework.backend.frontfacade.mvc.swagger.provider;/*
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
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Component Based on InMemorySwaggerResourcesProvider from  springfox
 */
@ConditionalOnProperty(value="appverse.frontfacade.swagger.eureka.enable2", matchIfMissing=false)
@Component
public class EurekaSwaggerResourcesProvider implements SwaggerResourcesProvider {
    @Value("${appverse.frontfacade.swagger.eureka.default.group:default-group}")
    private String baseSwaggerDefaultGroup;
    @Value("${appverse.frontfacade.swagger.eureka.default.url:/v2/api-docs?group=default-group}")
    private String baseSwaggerDefaultUrl;
    @Value("${appverse.frontfacade.swagger.eureka.default.version:2.0}")
    private String baseSwaggerDefaultVersion;
    @Value("${appverse.frontfacade.swagger.eureka.host:}")
    private String swaggerHost;


    @Value("#{'${appverse.frontfacade.swagger.eureka.exclusions:}'.split(',')}")
    private List<String> eurekaSkipServices;

    @Autowired(required = false)
    private DiscoveryClient discoveryClient;


    private static  String obtainUrlLocation(ServiceInstance instance, UriComponents current, String path, String swaggerHost){
        String managementPath = "";
        if (instance.getMetadata().containsKey("managementPath")) {
            managementPath = instance.getMetadata().get("managementPath");
        }
        String hostUrl;
        if (swaggerHost!=null && swaggerHost.length()>0){
            hostUrl=swaggerHost;
        }else {
            //tries to findout the host
            if (("https".equals(current.getScheme()) && 443 == current.getPort()) || ("http".equals(current.getScheme()) && 80 == current.getPort()) || -1 == current.getPort()) {
                //default ports
                hostUrl = String.format("%s://%s", current.getScheme(), current.getHost());
            } else {
                //custom ports
                hostUrl = String.format("%s://%s:%d", current.getScheme(), current.getHost(), current.getPort());
            }
        }
        return hostUrl + managementPath + path;
    }

    private static SwaggerResource createResource(String service, String urlLocation, String version){
        SwaggerResource resource = new SwaggerResource();
        resource.setName(service);
        resource.setLocation(urlLocation);
        resource.setSwaggerVersion(version);
        return resource;
    }

    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<SwaggerResource>();
        if (discoveryClient != null) {
            //eureka discovery cliend found
            List<String> services = discoveryClient.getServices();
            if (services != null && !services.isEmpty()) {
                //there are some services
                UriComponents current = null;
                if (swaggerHost==null || swaggerHost.length()==0) {
                    //obtain current uri from request
                    current = ServletUriComponentsBuilder.fromCurrentRequest().build();
                }
                for (String service : services) {
                    if (eurekaSkipServices!=null && eurekaSkipServices.size()!=0){
                        if (eurekaSkipServices.contains(service)){
                            continue;
                        }

                    }
                    List<ServiceInstance> instances = discoveryClient.getInstances(service);
                    if (instances.size() != 0) {
                        ServiceInstance instance = instances.get(0);
                        String urlLocation = obtainUrlLocation(instance, current, baseSwaggerDefaultUrl, swaggerHost);
                        resources.add(createResource(service, urlLocation, baseSwaggerDefaultVersion));
                    }

                }
            }
        }else{
            //return default swagger group
            resources.add(createResource(baseSwaggerDefaultGroup,baseSwaggerDefaultGroup,baseSwaggerDefaultVersion));
        }
        Collections.sort(resources);
        return resources;

    }


}



