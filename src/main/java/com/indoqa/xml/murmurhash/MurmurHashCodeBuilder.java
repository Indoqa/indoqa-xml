/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.indoqa.xml.murmurhash;

import static java.lang.System.arraycopy;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A hash code builder based on MurmurHash.
 */
public class MurmurHashCodeBuilder {

    private byte[] bytes;

    public MurmurHashCodeBuilder() {
        this.bytes = new byte[0];
    }

    public MurmurHashCodeBuilder append(byte[] byteArray) {
        if (byteArray == null) {
            return this;
        }

        byte[] joinedArray = new byte[this.bytes.length + byteArray.length];
        arraycopy(this.bytes, 0, joinedArray, 0, this.bytes.length);
        arraycopy(byteArray, 0, joinedArray, this.bytes.length, byteArray.length);
        this.bytes = joinedArray;
        return this;
    }

    public MurmurHashCodeBuilder append(int value) {
        return this.append(new byte[] {(byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value});
    }

    public MurmurHashCodeBuilder append(long v) {
        return this.append(
            new byte[] {(byte) (v >>> 56), (byte) (v >>> 48), (byte) (v >>> 40), (byte) (v >>> 32), (byte) (v >>> 24),
                (byte) (v >>> 16), (byte) (v >>> 8), (byte) (v >>> 0)});
    }

    public MurmurHashCodeBuilder append(String value) {
        if (value == null) {
            return this;
        }

        return this.append(value.getBytes(UTF_8));
    }

    public int toHashCode() {
        return MurmurHash.hash(this.bytes, 1);
    }

    protected byte[] getBytes() {
        return this.bytes;
    }
}
