/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

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
public class StudyCaseTest extends AbstractPowerFactoryTest {

    private StudyCase studyCase;

    @Before
    public void setUp() throws IOException {
        super.setUp();
        var studyTime = Instant.parse("2021-10-30T09:35:25Z");
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
