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
package com.dattack.dbtools.drules.exceptions;

/**
 * @author cvarela
 * @since 0.1
 */
public class DrulesMaxEventsException extends DrulesNestableRuntimeException {

    private static final long serialVersionUID = -7045950166789009946L;

    public DrulesMaxEventsException(final int maxEvents) {
        super(String.format("The maximum number of events has been reached (max: %d)", maxEvents));
    }
}
