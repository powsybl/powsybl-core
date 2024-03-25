/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ProjectTest extends AbstractPowerFactoryTest {

    private DataObject prj;
    private Project project;

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();

        DataClass clsIntPrj = DataClass.init("IntPrj")
                .addAttribute(new DataAttribute("pCase", DataAttributeType.OBJECT));
        DataClass clsIntPrjfolder = DataClass.init("IntPrjfolder");
        DataClass clsIntCase = DataClass.init("IntCase")
                .addAttribute(new DataAttribute("iStudyTime", DataAttributeType.INTEGER64));

        prj = new DataObject(4L, clsIntPrj, index)
                .setLocName("TestProject")
                .setObjectAttributeValue("pCase", 6L);

        var studyCasesFolder = new DataObject(5L, clsIntPrjfolder, index)
                .setLocName("Study Cases");
        studyCasesFolder.setParent(prj);

        Instant studyTime = Instant.parse("2021-10-30T09:35:25Z");
        var studyCase = new DataObject(6L, clsIntCase, index)
                .setLocName("TestStudyCase")
                .setLongAttributeValue("iStudyTime", studyTime.getEpochSecond());
        studyCase.setParent(studyCasesFolder);

        var netModel = new DataObject(7L, clsIntPrjfolder, index)
                .setLocName("Network Model");
        netModel.setParent(prj);

        var netData = new DataObject(8L, clsIntPrjfolder, index)
                .setLocName("Network Data");
        netData.setParent(netModel);

        elmNet.setParent(netData);
        project = new Project("TestProject", prj, index);
    }

    @Test
    void test() {
        assertEquals("TestProject", project.getName());
        assertSame(index, project.getIndex());
        assertSame(prj, project.getRootObject());
        StudyCase studyCase = project.getActiveStudyCase();
        assertNotNull(studyCase);
        assertSame(index, studyCase.getIndex());
        assertEquals("TestProject - TestStudyCase", studyCase.getName());
        Instant studyTime = Instant.parse("2021-10-30T09:35:25Z");
        assertEquals(studyTime, studyCase.getTime());
        assertEquals(1, studyCase.getElmNets().size());
        assertTrue(project.getIndex().getBackwardLinks(elmNet.getId()).isEmpty());
        assertEquals(List.of(objFoo), project.getIndex().getBackwardLinks(objBar.getId()));
        assertEquals(List.of(objFoo), project.getIndex().getBackwardLinks(objBaz.getId()));
    }

    @Test
    void jsonTest() throws IOException {
        var project2 = roundTripTest(project, Project::writeJson, Project::readJson, "/project.json");
        assertEquals("TestProject", project2.getName());
    }

    @Test
    void loaderTest() {
        var loader = new JsonProjectLoader();
        assertEquals(Project.class, loader.getDataClass());
        assertEquals("json", loader.getExtension());
        assertTrue(loader.test(getClass().getResourceAsStream("/project.json")));
        assertTrue(loader.test(new ByteArrayInputStream(new byte[] {}))); // FIXME
        Project project2 = loader.doLoad("project.json", getClass().getResourceAsStream("/project.json"));
        assertNotNull(project2);
    }
}
