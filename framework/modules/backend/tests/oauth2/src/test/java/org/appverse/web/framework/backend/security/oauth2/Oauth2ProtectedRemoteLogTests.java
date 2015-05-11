package org.appverse.web.framework.backend.security.oauth2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.List;









import org.appverse.web.framework.backend.frontfacade.rest.remotelog.model.presentation.RemoteLogRequestVO;
/*
import org.appverse.web.framework.backend.frontfacade.rest.remotelog.model.presentation.RemoteLogRequestVO;
import org.appverse.web.framework.backend.security.authentication.userpassword.model.AuthorizationData;
import org.appverse.web.framework.backend.security.xs.SecurityHelper;
*/
// import org.appverse.web.framework.backend.security.xs.xsrf.XSRFCheckFilterTests.AuthenticationManagerCustomizer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.test.OAuth2ContextConfiguration;
import org.springframework.security.oauth2.client.token.grant.implicit.ImplicitResourceDetails;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;


/*
 * Necessary to have httpclient for this tests (with scope tests) so that the http returns are handle correctly.
 * Otherwise you will experience exactly the problem described here:
 * http://stackoverflow.com/questions/27341604/exception-when-using-testresttemplate 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port=0")
public class Oauth2ProtectedRemoteLogTests extends AbstractIntegrationTests {
	
	@Autowired
    private FilterChainProxy springSecurityFilterChain;
	
	@Autowired
	private EmbeddedWebApplicationContext server;	
		
	@Autowired
	private TokenStore tokenStore;

	@Autowired
	private ClientDetailsService clientDetailsService;
	
	RestTemplate restTemplate = new TestRestTemplate();
	
	/*
	 * Enable this init method if you need to use a proxy to debug (fiddler, for instance)
	 * This is required as passing regular JVM arguments for proxy setup seems not to work with RestTemplate
	 * as it uses Apache HttpClient 
    @Before
    public void initProxy(){    	
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
	    Proxy proxy= new Proxy(Type.HTTP, new InetSocketAddress("localhost", 8888));
	    requestFactory.setProxy(proxy);
	    restTemplate = new RestTemplate(requestFactory);    	
    }
	 */
    
	protected String getPassword() {
		return "secret";
	}

	protected String getUsername() {
		return "dave";
	}

	@Test
	public void contextLoads() {
		assertTrue("Wrong token store type: " + tokenStore, tokenStore instanceof JdbcTokenStore);
		assertTrue("Wrong client details type: " + clientDetailsService, JdbcClientDetailsService.class.isAssignableFrom(AopUtils.getTargetClass(clientDetailsService)));
	}
	
	/* Crash with MVC
	 * 
	 * http://forum.spring.io/forum/spring-projects/security/oauth/109730-oauth-2-without-spring-mvc
	 * http://stackoverflow.com/questions/21907777/spring-security-oauth-basic-access-authentication-needed-when-sending-token-re
	 * http://stackoverflow.com/questions/24920373/how-to-override-the-spring-boot-default-oauthtokenendpoint
	 * http://stackoverflow.com/questions/29948154/cant-get-spring-security-oauth2-login-to-trigger
	 */
	
	@Test
	@OAuth2ContextConfiguration(resource = NonAutoApproveImplicit.class, initialize = false)
	public void testPostForNonAutomaticApprovalToken() throws Exception {

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", getBasicAuthentication());
		context.getAccessTokenRequest().setHeaders(headers);
		try {
			assertNotNull(context.getAccessToken());
			fail("Expected UserRedirectRequiredException");
		}
		catch (UserRedirectRequiredException e) {
			// ignore
		}
		// add user approval parameter for the second request
		context.getAccessTokenRequest().add(OAuth2Utils.USER_OAUTH_APPROVAL, "true");
		context.getAccessTokenRequest().add("scope.read", "true");
		assertNotNull(context.getAccessToken());
	}

	static class NonAutoApproveImplicit extends ImplicitResourceDetails {
		public NonAutoApproveImplicit(Object target) {
			super();
			setClientId("my-trusted-client");
			setId(getClientId());
			setPreEstablishedRedirectUri("http://anywhere");
		}
	}
	
	@Test
	public void testProtectedResourceIsProtected() throws Exception {
		ResponseEntity<String> response = http.getForString("/");
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertTrue("Wrong header: " + response.getHeaders(), response.getHeaders()
				.getFirst("WWW-Authenticate").startsWith("Bearer realm="));
	}
		
	@Test
	@OAuth2ContextConfiguration(resource = NonAutoApproveImplicit.class, initialize = false)
	public void testProtectedRemoteLogWithToken() throws Exception {

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", getBasicAuthentication());
		context.getAccessTokenRequest().setHeaders(headers);
		try {
			assertNotNull(context.getAccessToken());
			fail("Expected UserRedirectRequiredException");
		}
		catch (UserRedirectRequiredException e) {
			// ignore
		}
		// add user approval parameter for the second request
		context.getAccessTokenRequest().add(OAuth2Utils.USER_OAUTH_APPROVAL, "true");
		context.getAccessTokenRequest().add("scope.read", "true");
		assertNotNull(context.getAccessToken());
		
		// OAuth2RestTemplate template = new OAuth2RestTemplate(resource, new DefaultOAuth2ClientContext(context.getAccessToken()));
        RemoteLogRequestVO remoteLogRequest = new RemoteLogRequestVO();
        remoteLogRequest.setLogLevel("DEBUG");
        remoteLogRequest.setMessage("This is my log message!");
        
        int port = server.getEmbeddedServletContainer().getPort();
        
        ResponseEntity<String> result2 = http.getRestTemplate().postForEntity("http://localhost:" + port + "/jersey/remotelog/log", remoteLogRequest, String.class);
        assertEquals(HttpStatus.OK, result2.getStatusCode());
	}
	
	@Test
	public void testRemoteLogIsProtected() throws Exception {
        RemoteLogRequestVO remoteLogRequest = new RemoteLogRequestVO();
        remoteLogRequest.setLogLevel("DEBUG");
        remoteLogRequest.setMessage("This is my log message!");
        
        int port = server.getEmbeddedServletContainer().getPort();
		
		ResponseEntity<String> response = http.getRestTemplate().postForEntity("http://localhost:" + port + "/jersey/remotelog/log", remoteLogRequest, String.class);
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertTrue("Wrong header: " + response.getHeaders(), response.getHeaders()
				.getFirst("WWW-Authenticate").startsWith("Bearer realm="));
	}
	
}
