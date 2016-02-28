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
package com.dattack.dbtools.drules.engine;

/**
 * Property names that can be interpolated on scripts and configuration files.
 *
 * @author cvarela
 * @since 0.1
 */
public final class PropertyNames {

    /** Property 'check.expr'. */
    public static final String CHECK_EXPR = "checkExpression";

    /** Property 'errorRows'. */
    public static final String ERROR_ROWS = "errorRows";

    /** Property 'executionId'. */
    public static final String EXECUTION_ID = "executionId";

    /** Property 'join.using'. */
    public static final String JOIN_CONDITION = "joinCondition";

    /** Property 'log'. */
    public static final String LOG = "log";

    /** Property 'onMissing.source'. */
    public static final String MISSING_SOURCE = "missingSource";
    
    /** Property 'status'. */
    public static final String STATUS = "status";
    
    /** Property 'successRows'. */
    public static final String SUCCESS_ROWS = "successRows";
    
    /** Property 'task.name'. */
    public static final String TASK_NAME = "taskName";
    
    /** Property 'warningRows'. */
    public static final String WARNING_ROWS = "warningRows";
    
    private PropertyNames() {
        // static class
    }
}
