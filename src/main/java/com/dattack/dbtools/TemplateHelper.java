/*
 * Copyright (c) 2016, The Dattack team (http://www.dattack.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dattack.dbtools;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 * @author cvarela
 * @since 0.1
 */
public final class TemplateHelper {

    /**
     * The Configuration class is meant to be used in a singleton pattern (see {@link Configuration}).
     */
    private static volatile Configuration cfg;

    private TemplateHelper() {
        // static class
    }

    public static Template createTemplate(final String sourceCode) throws IOException, ConfigurationException {
        return new Template(null, sourceCode, getConfiguration());
    }

    public static Template loadTemplate(final String sourceFile) throws IOException, ConfigurationException {
        return getConfiguration().getTemplate(sourceFile);
    }

    private static Configuration getConfiguration() throws IOException, ConfigurationException {

        Configuration result = cfg;
        if (result == null) {
            synchronized (TemplateHelper.class) {
                result = cfg;
                if (result == null) {
                    result = new Configuration(Configuration.VERSION_2_3_23);
                    String templatesDir = GlobalConfiguration.getProperty(GlobalConfiguration.TEMPLATES_DIRECTORY_KEY);
                    if (StringUtils.isNotBlank(templatesDir)) {
                        result.setDirectoryForTemplateLoading(new File(templatesDir));
                    } else {
                        result.setDirectoryForTemplateLoading(new File("."));
                    }
                    result.setDefaultEncoding("UTF-8");
                    result.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
                    cfg = result;
                }
            }
        }
        return result;
    }
}
