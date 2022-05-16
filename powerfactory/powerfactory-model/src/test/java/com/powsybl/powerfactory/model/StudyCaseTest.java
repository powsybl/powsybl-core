/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import com.powsybl.commons.AbstractConverterTest;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                .addAttribute(new DataAttribute("o", DataAttributeType.OBJECT))
                .addAttribute(new DataAttribute("iv", DataAttributeType.INTEGER_VECTOR))
                .addAttribute(new DataAttribute("lv", DataAttributeType.INTEGER64_VECTOR))
                .addAttribute(new DataAttribute("fv", DataAttributeType.FLOAT_VECTOR))
                .addAttribute(new DataAttribute("dv", DataAttributeType.DOUBLE_VECTOR))
                .addAttribute(new DataAttribute("ov", DataAttributeType.OBJECT_VECTOR))
                .addAttribute(new DataAttribute("sv", DataAttributeType.STRING_VECTOR))
                .addAttribute(new DataAttribute("dm", DataAttributeType.DOUBLE_MATRIX));
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
                .setObjectAttributeValue("o", objBar.getId())
                .setIntVectorAttributeValue("iv", List.of(1, 2, 3))
                .setFloatVectorAttributeValue("fv", List.of(1.3f, 2.3f, 3.5f))
                .setLongVectorAttributeValue("lv", List.of(4L, 5L, 6943953495493593L))
                .setDoubleVectorAttributeValue("dv", List.of(1.3949d, 2.34d, 3.1223d))
                .setObjectVectorAttributeValue("ov", List.of(objBar.getId()))
                .setStringVectorAttributeValue("sv", List.of("AA", "BBB"))
                .setDoubleMatrixAttributeValue("dm", new BlockRealMatrix(new double[][] {{1d, 2d, 3d}, {4d, 5d, 6d}}));
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
        DataObject objFoo = studyCase2.getIndex().getDataObjectById(1L).orElseThrow();
        assertEquals(List.of(1, 2, 3), objFoo.getIntVectorAttributeValue("iv"));
        assertEquals(List.of(1.3f, 2.3f, 3.5f), objFoo.getFloatVectorAttributeValue("fv"));
        assertEquals(List.of(4L, 5L, 6943953495493593L), objFoo.getLongVectorAttributeValue("lv"));
        assertEquals(List.of(1.3949d, 2.34d, 3.1223d), objFoo.getDoubleVectorAttributeValue("dv"));
        assertEquals(List.of(2L), objFoo.getObjectVectorAttributeValue("ov").stream().map(DataObjectRef::getId).collect(Collectors.toList()));
        assertEquals(List.of("AA", "BBB"), objFoo.getStringVectorAttributeValue("sv"));
    }
}
