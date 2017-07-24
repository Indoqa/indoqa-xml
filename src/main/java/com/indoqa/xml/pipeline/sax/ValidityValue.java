/*
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
package com.indoqa.xml.pipeline.sax;

public class ValidityValue<V> {

    final private V value;

    final private long lastModified;

    public ValidityValue(final V value, long lastModified) {
        this.value = value;
        this.lastModified = lastModified;
    }

    public V getValue() {
        return value;
    }

    public long getLastModified() {
        return lastModified;
    }
}
