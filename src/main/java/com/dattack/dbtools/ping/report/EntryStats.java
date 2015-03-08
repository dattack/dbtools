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
package com.dattack.dbtools.ping.report;

/**
 * @author cvarela
 * @since 0.1
 */
public class EntryStats {

    private final long x;
    private final long y;
    private final int group;

    public EntryStats(final long x, final long y, final int group) {
        this.x = x;
        this.y = y;
        this.group = group;
    }

    public long getX() {
        return x;
    }

    public long getY() {
        return y;
    }

    public int getGroup() {
        return group;
    }
}
