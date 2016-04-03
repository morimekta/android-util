/*
 * Copyright (c) 2016, Stein Eldar Johnsen
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.morimekta.util.io;

import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Stein Eldar Johnsen
 * @since 28.12.15.
 */
public class Utf8StreamReaderTest {
    // TODO: test unicode U+2A6B2.
    // It is the highest unicode codepoint that also has a valid printable
    // character on most platforms.
    // https://en.wikipedia.org/wiki/List_of_CJK_Unified_Ideographs_Extension_B_(Part_7_of_7)

    @Test
    public void testRead_ASCII() throws IOException {
        byte[] data = new byte[]{'a', 'b', 'b', 'a', '\t', 'x'};

        Utf8StreamReader reader = new Utf8StreamReader(new ByteArrayInputStream(data));

        char[] out = new char[6];
        assertEquals(6, reader.read(out));
        assertEquals("abba\tx", String.valueOf(out));
    }

    @Test
    public void testRead_UTF8_longString() throws IOException {
        String original = "ü$Ѹ~OӐW| \\rBֆc}}ӂဂG3>㚉EGᖪǙ\\t;\\tၧb}H(πи-ˁ&H59XOqr/,?DרB㡧-Үyg9i/?l+ႬЁjZr=#DC+;|ԥ'f9VB5|8]cOEሹrĐaP.ѾҢ/^nȨޢ\\\"u";
        byte[] data = original.getBytes(UTF_8);
        char[] out = new char[original.length()];

        Utf8StreamReader reader = new Utf8StreamReader(new ByteArrayInputStream(data));

        assertEquals(out.length, reader.read(out));
        assertEquals(original, String.valueOf(out));
    }

    @Test
    public void testRead_UTF8_singleRead() throws IOException {
        String original = "ü$Ѹ~";
        byte[] data = original.getBytes(UTF_8);

        ByteArrayInputStream bais = new ByteArrayInputStream(data);

        assertEquals('ü', (char) new Utf8StreamReader(bais).read());
        assertEquals('$', (char) new Utf8StreamReader(bais).read());
        assertEquals('Ѹ', (char) new Utf8StreamReader(bais).read());
        assertEquals('~', (char) new Utf8StreamReader(bais).read());
    }

    @Test
    @Ignore("We're missing good data to test with surrogate pair unicode")
    public void testReadSurrogatePair() throws IOException {
        byte[] src = new byte[]{};
        ByteArrayInputStream bais = new ByteArrayInputStream(src);

        Utf8StreamReader reader = new Utf8StreamReader(bais);

        char a = (char) reader.read();
        char b = (char) reader.read();

        assertTrue(Character.isHighSurrogate(a));
        assertTrue(Character.isLowSurrogate(b));
        assertEquals(0x2A6B2, Character.toCodePoint(a, b));
    }
}