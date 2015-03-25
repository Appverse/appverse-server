/*
 * Copyright 2012-2013 the original author or authors.
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
package org.appverse.web.framework.backend.core.persistence.test;

import org.appverse.web.framework.backend.core.persistence.test.model.integration.TestDTO;
import org.appverse.web.framework.backend.core.persistence.test.services.integration.TestRepository;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Integration tests for {@link TestRepository}.
 *
 * @author Miguel Fernandez
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@SpringApplicationConfiguration(classes = TestConfiguration.class)
public class TestHibernateRepositoryIntegrationTests {

	@Autowired
	TestRepository repository;

	/**
	 * Tests calling a repository method with pagination
	 */
	@Test
	public void findsFirstPageOfTestObjects() {
		Page<TestDTO> cities = this.repository.findAll(new PageRequest(0, 10));
		assertThat(cities.getTotalElements(), is(8L));
	}
	
	/**
	 * Tests unwrapping and calling the Hibernate native API to retrieve elements
	 */
	@Test
    public void findTestObjectcUsingNativeHibernateApi() {
        Session session = repository.unwrap(org.hibernate.Session.class);
        Criteria criteria = session.createCriteria(TestDTO.class);
        assertThat(criteria.list().size(), is(8));
    }
}
