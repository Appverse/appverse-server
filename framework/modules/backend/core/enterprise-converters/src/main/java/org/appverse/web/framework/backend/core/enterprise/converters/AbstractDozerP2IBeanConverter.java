/*
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
package org.appverse.web.framework.backend.core.enterprise.converters;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.appverse.web.framework.backend.core.AbstractBean;
import org.appverse.web.framework.backend.core.beans.AbstractIntegrationBean;
import org.appverse.web.framework.backend.core.beans.AbstractPresentationBean;
import org.dozer.Mapper;
import org.dozer.spring.DozerBeanMapperFactoryBean;
import org.springframework.core.convert.converter.Converter;

public class AbstractDozerP2IBeanConverter<PresentationBean extends AbstractPresentationBean, IntegrationBean extends AbstractIntegrationBean>
		implements IP2IBeanConverter<PresentationBean, IntegrationBean> {

	@Resource
	protected DozerBeanMapperFactoryBean dozerBeanMapperFactoryBean;
	private Class<PresentationBean> presentationBeanClass;
	private Class<IntegrationBean> IntegrationBeanClass;
	private String SCOPE_WITHOUT_DEPENDENCIES = "default-scope-without-dependencies";
	private String SCOPE_COMPLETE = "default-scope-complete";
	private String SCOPE_CUSTOM = "default-scope-custom";

	private Converter<PresentationBean, IntegrationBean> springConverter = (PresentationBean bean) -> { try { return this.convert(bean); } catch (ConverterException e) { throw new RuntimeException(e);}}; 
	private Converter<IntegrationBean, PresentationBean> springInverseConverter = (IntegrationBean bean) -> { try { return this.convert(bean); } catch (ConverterException e) { throw new RuntimeException(e);}};
	
	public AbstractDozerP2IBeanConverter() {
		super();
	}

	@Override
	public PresentationBean convert(IntegrationBean bean) throws ConverterException {
		return convert(bean, ConversionType.Complete);
	}

	@Override
	public PresentationBean convert(IntegrationBean integrationBean,
			ConversionType conversionType) throws ConverterException {
        return convert(integrationBean, getScope(conversionType));
	}

    @Override
    public PresentationBean convert(IntegrationBean integrationBean,
                                    String scope) throws ConverterException {
        try {
        	return ((Mapper) dozerBeanMapperFactoryBean.getObject()).map(
                integrationBean, presentationBeanClass, scope);
        } catch (Exception e) {
        	throw new ConverterException(e);
        }
    }

	@Override
	public void convert(final IntegrationBean integrationBean,
			PresentationBean presentationBean) throws ConverterException {
		convert(integrationBean, presentationBean, ConversionType.Complete);
	}

	@Override
	public void convert(final IntegrationBean integrationBean,
			PresentationBean presentationBean, ConversionType conversionType)
			throws ConverterException {
        convert(integrationBean, presentationBean, getScope(conversionType));
	}

    @Override
    public void convert(final IntegrationBean integrationBean,
                        PresentationBean presentationBean, String scope)
            throws ConverterException {
    	try {
    		((Mapper) dozerBeanMapperFactoryBean.getObject()).map(integrationBean,
                presentationBean, scope);
        } catch (Exception e) {
        	throw new ConverterException(e);
        }
    }

	@Override
	public IntegrationBean convert(PresentationBean bean) throws ConverterException {
		return convert(bean, ConversionType.Complete);
	}

	@Override
	public IntegrationBean convert(PresentationBean presentationBean,
			ConversionType conversionType) throws ConverterException {
        return convert(presentationBean, getScope(conversionType));
	}

    @Override
    public IntegrationBean convert(PresentationBean presentationBean,
                                   String scope) throws ConverterException {
        try {
        	return ((Mapper) dozerBeanMapperFactoryBean.getObject()).map(
        			presentationBean, IntegrationBeanClass, scope);
        } catch (Exception e) {
        	throw new ConverterException(e);
        }
    }

	@Override
	public void convert(final PresentationBean presentationBean,
			IntegrationBean IntegrationBean) throws ConverterException {
		convert(presentationBean, IntegrationBean, ConversionType.Complete);
	}

	@Override
	public void convert(final PresentationBean presentationBean,
			IntegrationBean integrationBean, ConversionType conversionType)
			throws ConverterException {
        convert(presentationBean, integrationBean, getScope(conversionType));
	}

    @Override
    public void convert(final PresentationBean presentationBean,
                        IntegrationBean integrationBean, String scope)
            throws ConverterException {
    	try {
    		((Mapper) dozerBeanMapperFactoryBean.getObject()).map(presentationBean,
                integrationBean, scope);
        } catch (Exception e) {
        	throw new ConverterException(e);
        }
    }

	@Override
	public List<PresentationBean> convertIntegrationList(
			List<IntegrationBean> integrationBeans) throws ConverterException {
		List<PresentationBean> presentationBeans = new ArrayList<PresentationBean>();
		for (IntegrationBean integrationBean : integrationBeans) {
			PresentationBean presentationBean = convert(integrationBean,
					ConversionType.Complete);
			presentationBeans.add(presentationBean);
		}
		return presentationBeans;
	}

	@Override
	public List<PresentationBean> convertIntegrationList(
			List<IntegrationBean> integrationBeans,
			ConversionType conversionType) throws ConverterException {
        return convertIntegrationList(integrationBeans, getScope(conversionType));
	}

    @Override
    public List<PresentationBean> convertIntegrationList(
            List<IntegrationBean> integrationBeans,
            String scope) throws ConverterException {
        List<PresentationBean> presentationBeans = new ArrayList<PresentationBean>();
        try {
	        for (IntegrationBean IntegrationBean : integrationBeans) {
	            PresentationBean presentationBean = ((Mapper) dozerBeanMapperFactoryBean
	                    .getObject()).map(IntegrationBean, presentationBeanClass,
	                    scope);
	            presentationBeans.add(presentationBean);
	        }
        } catch (Exception e) {
        	throw new ConverterException(e);
        }
        return presentationBeans;
    }

	@Override
	public void convertIntegrationList(List<IntegrationBean> integrationBeans,
			List<PresentationBean> presentationBeans) throws ConverterException {
		if (integrationBeans.size() != presentationBeans.size()) {
			throw new ConverterException("List sizes differ");
		}
		for (int i = 0; i < integrationBeans.size(); i++) {
			convert(integrationBeans.get(i), presentationBeans.get(i),
					ConversionType.Complete);
		}
	}

	@Override
	public void convertIntegrationList(List<IntegrationBean> integrationBeans,
			List<PresentationBean> presentationBeans,
			ConversionType conversionType) throws ConverterException {
        convertIntegrationList(integrationBeans, presentationBeans, getScope(conversionType));
	}

    @Override
    public void convertIntegrationList(List<IntegrationBean> integrationBeans,
                                       List<PresentationBean> presentationBeans,
                                       String scope) throws ConverterException {
        if (integrationBeans.size() != presentationBeans.size()) {
            throw new ConverterException("List sizes differ");
        }
        try {
	        for (int i = 0; i < integrationBeans.size(); i++) {
	            ((Mapper) dozerBeanMapperFactoryBean.getObject()).map(
	                    integrationBeans.get(i), presentationBeans.get(i),
	                    scope);
	        }
        } catch (Exception e) {
        	throw new ConverterException(e);
        }
    }

	@Override
	public List<IntegrationBean> convertPresentationList(
			List<PresentationBean> presentationBeans) throws ConverterException {
		List<IntegrationBean> integrationBeans = new ArrayList<IntegrationBean>();
		for (PresentationBean presentationBean : presentationBeans) {
			IntegrationBean integrationBean = convert(presentationBean,
					ConversionType.Complete);
			integrationBeans.add(integrationBean);
		}
		return integrationBeans;
	}

	@Override
	public List<IntegrationBean> convertPresentationList(
			List<PresentationBean> presentationBeans,
			ConversionType conversionType) throws ConverterException {
        return convertPresentationList(presentationBeans, getScope(conversionType));
	}

    @Override
    public List<IntegrationBean> convertPresentationList(
            List<PresentationBean> presentationBeans,
            String scope) throws ConverterException {
        List<IntegrationBean> IntegrationBeans = new ArrayList<IntegrationBean>();
        try {
	        for (PresentationBean presentationBean : presentationBeans) {
	            IntegrationBean IntegrationBean = ((Mapper) dozerBeanMapperFactoryBean
	                    .getObject()).map(presentationBean, IntegrationBeanClass,
	                    scope);
	            IntegrationBeans.add(IntegrationBean);
	        }
        } catch (Exception e) {
        	throw new ConverterException(e);
        }
        return IntegrationBeans;
    }

	@Override
	public void convertPresentationList(
			List<PresentationBean> presentationBeans,
			List<IntegrationBean> integrationBeans) throws ConverterException {
		if (presentationBeans.size() != integrationBeans.size()) {
			throw new ConverterException("List sizes differ");
		}
		for (int i = 0; i < presentationBeans.size(); i++) {
			convert(presentationBeans.get(i), integrationBeans.get(i),
					ConversionType.Complete);
		}
	}

	@Override
	public void convertPresentationList(
			List<PresentationBean> presentationBeans,
			List<IntegrationBean> integrationBeans,
			ConversionType conversionType) throws ConverterException {
       convertPresentationList(presentationBeans, integrationBeans, getScope(conversionType));
	}

    @Override
    public void convertPresentationList(
            List<PresentationBean> presentationBeans,
            List<IntegrationBean> integrationBeans,
            String scope) throws ConverterException {
        if (presentationBeans.size() != integrationBeans.size()) {
            throw new ConverterException("List sizes differ");
        }
        try {
	        for (int i = 0; i < presentationBeans.size(); i++) {
	            ((Mapper) dozerBeanMapperFactoryBean.getObject()).map(
	                    presentationBeans.get(i), integrationBeans.get(i),
	                    scope);
	        }
        } catch (Exception e) {
        	throw new ConverterException(e);
        }
    }

	@Override
	public String getScope(ConversionType conversionType) {
		String scope = null;
		if (conversionType == ConversionType.WithoutDependencies) {
			scope = SCOPE_WITHOUT_DEPENDENCIES;
		} else if (conversionType == ConversionType.Custom) {
			scope = SCOPE_CUSTOM;
		} else {
			scope = SCOPE_COMPLETE;
		}
		return scope;
	}

	public void setBeanClasses(Class<PresentationBean> presentationBeanClass,
			Class<IntegrationBean> IntegrationBeanClass) {
		this.presentationBeanClass = presentationBeanClass;
		this.IntegrationBeanClass = IntegrationBeanClass;
	}

	@Override
	public void setScopes(String... scopes) {
		if (scopes.length > 0) {
			this.SCOPE_COMPLETE = scopes[0];
		}
		if (scopes.length > 1) {
			this.SCOPE_WITHOUT_DEPENDENCIES = scopes[1];
		}
		if (scopes.length > 2) {
			this.SCOPE_CUSTOM = scopes[2];
		}
	}

	
	@Override
	public Converter<PresentationBean, IntegrationBean> getSpringConverter() {
		return springConverter;
	}
	
	@Override
	public Converter<IntegrationBean, PresentationBean> getSpringInverseConverter() {
		return springInverseConverter;
	}
	
	
}
