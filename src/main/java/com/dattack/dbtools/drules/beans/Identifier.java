/*
 * Copyright (c) 2015, The Dattack team (http://www.dattack.com)
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
package com.dattack.dbtools.drules.beans;

import java.io.Serializable;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;

/**
 * @author cvarela
 * @since 0.1
 */
@XmlJavaTypeAdapter(IdentifierAdapter.class)
public final class Identifier implements Serializable {

    public static class IdentifierBuilder {

        private String value;

        public Identifier build() {
            return new Identifier(this);
        }

        public IdentifierBuilder withValue(final String text) {
            if (value == null) {
                this.value = text;
            } else {
                this.value = String.format("%s.%s", this.value, text);
            }
            return this;
        }
    }

    private static final long serialVersionUID = 4467866944887901631L;

    private String value;

    public Identifier() {
        // public constructor
    }

    private Identifier(final IdentifierBuilder builder) {
        this.value = normalize(builder.value);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Identifier other = (Identifier) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    public String getValue() {
        return value;
    }

    public Identifier append(final String other) {
        return new IdentifierBuilder().withValue(getValue()).withValue(other).build();
    }

    public Identifier append(final Identifier other) {
        return new IdentifierBuilder().withValue(getValue()).withValue(other.getValue()).build();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    private String normalize(final String text) {
        return StringUtils.trim(StringUtils.lowerCase(text));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Identifier[value=").append(value).append("]");
        return builder.toString();
    }
}
