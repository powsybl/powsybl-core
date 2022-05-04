/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import com.google.common.io.ByteStreams;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StudyCaseTest {

    private StudyCase studyCase;
    private DataObjectIndex index;
    private DataObject objBar;
    private DataObject objFoo;
    private DataObject elmNet;

    @Before
    public void setUp() throws Exception {
        DataClass clsFoo = DataClass.init("ElmFoo")
                .addAttribute(new DataAttribute("ref", DataAttributeType.INTEGER64));
        DataClass clsBar = DataClass.init("ElmBar");
        DataClass clsNet = DataClass.init("ElmNet");

        index = new DataObjectIndex();
        objBar = new DataObject(2L, clsBar, index)
                .setLocName("bar");
        objFoo = new DataObject(1L, clsFoo, index)
                .setLocName("foo")
                .setLongAttributeValue("ref", objBar.getId());
        Instant studyTime = Instant.parse("2021-10-30T09:35:25Z");
        elmNet = new DataObject(0L, clsNet, index)
                .setLocName("net");
        studyCase = new StudyCase("test", studyTime, List.of(elmNet), index);
    }

    @Test
    public void test() {
        assertEquals("test", studyCase.getName());
        Instant studyTime = Instant.parse("2021-10-30T09:35:25Z");
        assertEquals(studyTime, studyCase.getTime());
        assertSame(index, objFoo.getIndex());
        assertEquals(List.of(elmNet, objFoo, objBar), new ArrayList<>(studyCase.getIndex().getDataObjects()));
    }

    @Test
    public void jsonTest() throws IOException {
        String json;
        try (StringWriter writer = new StringWriter()) {
            studyCase.writeJson(writer);
            json = writer.toString();
        }
        var is = Objects.requireNonNull(getClass().getResourceAsStream("/studyCase.json"));
        assertEquals(new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8), json);

        try (StringReader reader = new StringReader(json)) {
            StudyCase studyCase2 = StudyCase.parseJson(reader);
            assertEquals("test", studyCase2.getName());
            Instant studyTime = Instant.parse("2021-10-30T09:35:25Z");
            assertEquals(studyTime, studyCase2.getTime());
            assertEquals(1, studyCase2.getElmNets().size());
        }
    }
}
