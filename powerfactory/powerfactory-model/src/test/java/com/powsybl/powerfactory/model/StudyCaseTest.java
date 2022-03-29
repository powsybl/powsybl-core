/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
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
                .addAttribute(new DataAttribute("ref", DataAttributeType.INTEGER64));
        DataClass clsBar = DataClass.init("ElmBar");

        DataObjectIndex index = new DataObjectIndex();
        DataObject objBar = new DataObject(2L, clsBar, index)
                .setLocName("bar");
        DataObject objFoo = new DataObject(1L, clsFoo, index)
                .setLocName("foo")
                .setLongAttributeValue("ref", objBar.getId());
        Instant time = Instant.parse("2021-10-30T09:35:25Z");
        DataClass clsNet = DataClass.init("ElmNet");
        DataObject elmNet = new DataObject(0L, clsNet, index)
                .setLocName("net");
        StudyCase studyCase = new StudyCase("test", time, index);

        assertEquals("test", studyCase.getName());
        assertEquals(time, studyCase.getTime());
        assertSame(index, objFoo.getIndex());
        assertEquals(List.of(elmNet, objFoo, objBar), new ArrayList<>(studyCase.getIndex().getDataObjects()));
    }
}
