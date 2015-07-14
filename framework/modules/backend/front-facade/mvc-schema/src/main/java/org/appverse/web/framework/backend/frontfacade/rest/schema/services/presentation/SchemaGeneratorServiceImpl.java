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
package org.appverse.web.framework.backend.frontfacade.rest.authentication.simple.services.presentation;

import org.appverse.web.framework.backend.frontfacade.rest.beans.CredentialsVO;
import org.appverse.web.framework.backend.security.authentication.userpassword.managers.UserAndPasswordAuthenticationManager;
import org.appverse.web.framework.backend.security.authentication.userpassword.model.AuthorizationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@ConditionalOnProperty(value="appverse.frontfacade.rest.schema.enabled", matchIfMissing=false)
@RequestMapping(value = "${appverse.frontfacade.rest.api.basepath:/api}")
public class SchemaGeneratorServiceImpl {
    @Autowired
    private PersistentEntityToJsonSchemaConverter entityConverter;

    @RequestMapping(value = "/schema-generator/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)

    public List<String> listEntities(){
        List<String> data = new ArrayList<String>();
        Set<GenericConverter.ConvertiblePair> list =  entityConverter.getConvertibleTypes();
        for (GenericConverter.ConvertiblePair element: list){
            if (!Modifier.isAbstract(element.getSourceType().getModifiers())){
                data.add(element.getSourceType().getCanonicalName());
            }
        }
        return data;
    }
    @RequestMapping(value = "/schema-generator/entity/{entity}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String listDetail (@PathVariable("entity") String entity) throws Exception{
        String data = "";
        if (entity==null || "".equals(entity)){
            throw new PresentationException("invalid content");
        }
        Class<?> clazz = Class.forName(entity);
        if (clazz.getAnnotationsByType(Entity.class)==null ) {
            throw new PresentationException("invalid class");
        }
        try {
            Constructor<?> ctor = clazz.getConstructor();
            JsonSchema schema = entityConverter.convert(clazz, TypeDescriptor.valueOf(String.class), TypeDescriptor.valueOf(JsonSchema.class));

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationConfig.Feature.WRITE_ENUMS_USING_TO_STRING, true);
            mapper.configure(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES, false);
            data = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);

        }catch (NoSuchMethodException nsme){
            throw new PresentationException("invalid class no empty constructor",nsme);
        }
        return data;
    }

}