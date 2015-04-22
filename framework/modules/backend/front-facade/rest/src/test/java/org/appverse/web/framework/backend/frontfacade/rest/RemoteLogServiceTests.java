package org.appverse.web.framework.backend.frontfacade.rest;

import static org.junit.Assert.assertEquals;

import org.appverse.web.framework.backend.frontfacade.rest.remotelog.model.presentation.RemoteLogRequestVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {RemoteLogServiceTestsConfigurationApplication.class})
@WebIntegrationTest(randomPort= true, value="security.basic.enabled=false")
public class RemoteLogServiceTests {
	
	@Autowired
	private AnnotationConfigEmbeddedWebApplicationContext context;
	
	RestTemplate restTemplate = new TestRestTemplate();
	
	
	// http://blog.codeleak.pl/2014/09/using-configurationproperties-in-spring.html
	
	@Test
	public void remoteLogServiceTest() {
		int port = context.getEmbeddedServletContainer().getPort();
		RemoteLogRequestVO logRequestVO = new RemoteLogRequestVO();
		logRequestVO.setMessage("Test mesage!");
		logRequestVO.setLogLevel("DEBUG");
		 
		ResponseEntity<String> entity = restTemplate.postForEntity("http://localhost:" + port + "/remotelog/log", logRequestVO, String.class);
		assertEquals(HttpStatus.OK, entity.getStatusCode());
	}

}
