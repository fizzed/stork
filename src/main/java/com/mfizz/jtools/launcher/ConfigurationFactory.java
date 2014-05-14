/*
 * Copyright 2014 mfizz.
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
package com.mfizz.jtools.launcher;

/*
 * #%L
 * mfz-jtools-launcher
 * %%
 * Copyright (C) 2012 - 2014 mfizz
 * %%
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
 * #L%
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 *
 * @author joelauer
 */
public class ConfigurationFactory {
    
    private ObjectMapper mapper;
    
    public ConfigurationFactory() {
        this.mapper = createObjectMapper();
    }
    
    static public ObjectMapper createObjectMapper() {
        // create json deserializer
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        return mapper;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
    
    public JsonNode createDefaultNode() {
        return mapper.valueToTree(new Configuration());
    }
    
    public JsonNode createConfigNode(File configFile) throws IOException {
        return mapper.readTree(configFile);
    }
    
    public JsonNode mergeNodes(JsonNode node, JsonNode updateNode) {
        return JacksonUtil.merge(node, updateNode);
    }
    
    public Configuration create(JsonNode node) throws JsonProcessingException {
        return mapper.treeToValue(node, Configuration.class);
    }
    
    public Configuration create(File configFile) throws Exception {
        // tree of defaults
        JsonNode defaultNode = createDefaultNode();
        
        // tree of configuration
        JsonNode configNode = createConfigNode(configFile);
        
        // merge defaults + config
        JsonNode mergedNode = mergeNodes(defaultNode, configNode);
        
        Configuration config = create(mergedNode);
        config.setFile(configFile);
        
        // create validator
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        
        Set<ConstraintViolation<Configuration>> violations = validator.validate(config);
        if (violations.size() > 0) {
            for (ConstraintViolation<Configuration> violation : violations) {
                throw new Exception("Configuration file invalid: property [" + violation.getPropertyPath() + "] error [" + violation.getMessage() + "]");
            }
        }
        
        return config;
    }
    
}
