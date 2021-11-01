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
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ProjectTest {

    @Test
    public void testLinks() {
        DataClass clsFoo = DataClass.init("ElmFoo")
                .addAttribute(new DataAttribute("ref", DataAttributeType.OBJECT));
        DataClass clsBar = DataClass.init("ElmBar");
        DataClass clsBaz = DataClass.init("ElmBaz")
                .addAttribute(new DataAttribute("refs", DataAttributeType.OBJECT_VECTOR));

        DataObject objBar = new DataObject(2L, clsBar)
                .setLocName("bar");
        DataObject objFoo = new DataObject(1L, clsFoo)
                .setLocName("foo")
                .setObjectAttributeValue("ref", objBar);
        DataObject objBaz = new DataObject(3L, clsBaz)
                .setLocName("baz")
                .setObjectVectorAttributeValue("refs", List.of(objFoo, objBar));
        Instant creationTime = Instant.parse("2021-10-30T09:35:25Z");
        DataObject rootObj = new DataObject(0L, new DataClass("ElmNet"));
        Project project = new Project("test", creationTime, Map.of("k", "v"), rootObj,
                List.of(objFoo, objBar, objBaz));

        assertEquals("test", project.getName());
        assertEquals(creationTime, project.getCreationTime());
        assertEquals(rootObj, project.getRootObject());
        assertSame(project, objFoo.getProject());
        assertEquals(Map.of("k", "v"), project.getProperties());
        assertSame(objFoo, project.getObjectById(1L).orElseThrow());
        assertEquals(1, project.getObjectsByClass("ElmFoo").size());
        assertEquals(Map.of("ElmBar", 1, "ElmBaz", 1, "ElmFoo", 1), project.getObjectCountByClass());

        // test links and backward links
        assertSame(objBar, objFoo.findObjectAttributeValue("ref").orElseThrow());
        assertSame(objBar, objFoo.getObjectAttributeValue("ref"));
        assertEquals(2, project.getBackwardLinks(objBar.getId()).size());
        assertSame(objFoo, project.getBackwardLinks(objBar.getId()).get(0));
        assertSame(objBaz, project.getBackwardLinks(objBar.getId()).get(1));
        assertEquals(2, objBaz.findObjectVectorAttributeValue("refs").orElseThrow().size());
        assertEquals(2, objBaz.getObjectVectorAttributeValue("refs").size());
        assertSame(objFoo, objBaz.findObjectVectorAttributeValue("refs").orElseThrow().get(0));
        assertSame(objBar, objBaz.findObjectVectorAttributeValue("refs").orElseThrow().get(1));
        assertEquals(1, project.getBackwardLinks(objFoo.getId()).size());
        assertSame(objBaz, project.getBackwardLinks(objFoo.getId()).get(0));
    }

    private static Project createProject() {
        DataClass intPrj = DataClass.init("IntPrj");
        long nextId = 0L;
        DataObject rootObj = new DataObject(nextId++, intPrj)
                .setLocName("Test");
        Instant creationTime = Instant.parse("2021-10-30T09:35:25Z");

        DataClass intPrjfolder = DataClass.init("IntPrjfolder");
        DataObject studyCasesFolder = new DataObject(nextId++, intPrjfolder)
                .setLocName("Study Cases")
                .setParent(rootObj);
        DataObject networkDataFolder = new DataObject(nextId++, intPrjfolder)
                .setLocName("Network Data")
                .setParent(rootObj);

        DataClass intCase = DataClass.init("IntCase");
        DataObject studyCase1 = new DataObject(nextId++, intCase)
                .setLocName("Study Case 1")
                .setParent(rootObj);
        DataClass setTime1 = DataClass.init("SetTime")
                .addAttribute(new DataAttribute("datetime", DataAttributeType.INTEGER));
        DataObject setStudyTime1 = new DataObject(nextId++, setTime1)
                .setLocName("Set Study Time")
                .setInstantAttributeValue("datetime", Instant.parse("2021-10-30T09:35:25Z"))
                .setParent(studyCase1);

        return new Project("test", creationTime, Map.of("k", "v"), rootObj,
                List.of(studyCasesFolder, networkDataFolder, studyCase1, setStudyTime1));
    }

    @Test
    public void testStudyCases() {
        Project project = createProject();
        List<StudyCase> studyCases = project.getStudyCases();
        assertEquals(1, studyCases.size());
        StudyCase studyCase1 = studyCases.get(0);
        assertEquals("Study Case 1", studyCase1.getName());
        assertEquals(Instant.parse("2021-10-30T09:35:25Z"), studyCase1.getTime());
        List<NetworkVariation> networkVariations = studyCase1.getNetworkVariations();
        assertEquals(0, networkVariations.size());
    }
}
