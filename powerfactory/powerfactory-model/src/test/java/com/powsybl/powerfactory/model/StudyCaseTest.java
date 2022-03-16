/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import org.junit.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StudyCaseTest {

    @Test
    public void test() {
        DataClass clsFoo = DataClass.init("ElmFoo")
                .addAttribute(new DataAttribute("ref", DataAttributeType.STRING));
        DataClass clsBar = DataClass.init("ElmBar");

        DataObject objBar = new DataObject(2L, clsBar)
                .setLocName("bar");
        DataObject objFoo = new DataObject(1L, clsFoo)
                .setLocName("foo")
                .setStringAttributeValue("ref", Long.toString(objBar.getId()));
        Instant time = Instant.parse("2021-10-30T09:35:25Z");
        DataObject elmNet = new DataObject(0L, new DataClass("ElmNet"));
        StudyCase studyCase = new StudyCase("test", time, List.of(elmNet));
        objBar.setStudyCase(studyCase);
        objFoo.setStudyCase(studyCase);

        assertEquals("test", studyCase.getName());
        assertEquals(time, studyCase.getTime());
        assertEquals(List.of(elmNet), studyCase.getElmNets());
        assertSame(studyCase, objFoo.getStudyCase());
    }
}
