/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import com.powsybl.commons.AbstractConverterTest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StudyCaseTest extends AbstractConverterTest {

    private StudyCase studyCase;
    private DataObjectIndex index;
    private DataObject objBar;
    private DataObject objFoo;
    private DataObject elmNet;

    @Before
    public void setUp() throws IOException {
        super.setUp();
        DataClass clsFoo = DataClass.init("ElmFoo")
                .addAttribute(new DataAttribute("i", DataAttributeType.INTEGER))
                .addAttribute(new DataAttribute("l", DataAttributeType.INTEGER64))
                .addAttribute(new DataAttribute("f", DataAttributeType.FLOAT))
                .addAttribute(new DataAttribute("d", DataAttributeType.DOUBLE))
                .addAttribute(new DataAttribute("obj", DataAttributeType.OBJECT));
        DataClass clsBar = DataClass.init("ElmBar");
        DataClass clsNet = DataClass.init("ElmNet");

        index = new DataObjectIndex();
        objBar = new DataObject(2L, clsBar, index)
                .setLocName("bar");
        objFoo = new DataObject(1L, clsFoo, index)
                .setLocName("foo")
                .setIntAttributeValue("i", 3)
                .setLongAttributeValue("l", 49494L)
                .setFloatAttributeValue("f", 3.4f)
                .setDoubleAttributeValue("d", 3494.93939d)
                .setObjectAttributeValue("obj", objBar.getId());
        Instant studyTime = Instant.parse("2021-10-30T09:35:25Z");
        elmNet = new DataObject(0L, clsNet, index)
                .setLocName("net");
        objFoo.setParent(elmNet);
        objBar.setParent(objFoo);
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
        var studyCase2 = roundTripTest(studyCase, StudyCase::writeJson, StudyCase::readJson, "/studyCase.json");
        assertEquals("test", studyCase2.getName());
        Instant studyTime = Instant.parse("2021-10-30T09:35:25Z");
        assertEquals(studyTime, studyCase2.getTime());
        assertEquals(1, studyCase2.getElmNets().size());
    }
}
