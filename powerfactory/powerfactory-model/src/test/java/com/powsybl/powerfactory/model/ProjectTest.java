/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import org.junit.Test;

import java.io.IOException;
import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ProjectTest extends AbstractPowerFactoryTest {

    private DataObject prj;
    private Project project;

    @Override
    public void setUp() throws IOException {
        super.setUp();

        DataClass clsIntPrj = DataClass.init("IntPrj")
                .addAttribute(new DataAttribute("pCase", DataAttributeType.OBJECT));
        DataClass clsIntPrjfolder = DataClass.init("IntPrjfolder");
        DataClass clsIntCase = DataClass.init("IntCase")
                .addAttribute(new DataAttribute("iStudyTime", DataAttributeType.INTEGER64));

        prj = new DataObject(3L, clsIntPrj, index)
                .setLocName("TestProject")
                .setObjectAttributeValue("pCase", 5L);

        var studyCasesFolder = new DataObject(4L, clsIntPrjfolder, index)
                .setLocName("Study Cases");
        studyCasesFolder.setParent(prj);

        Instant studyTime = Instant.parse("2021-10-30T09:35:25Z");
        var studyCase = new DataObject(5L, clsIntCase, index)
                .setLocName("TestStudyCase")
                .setLongAttributeValue("iStudyTime", studyTime.toEpochMilli());
        studyCase.setParent(studyCasesFolder);

        var netModel = new DataObject(6L, clsIntPrjfolder, index)
                .setLocName("Network Model");
        netModel.setParent(prj);

        var netData = new DataObject(7L, clsIntPrjfolder, index)
                .setLocName("Network Data");
        netData.setParent(netModel);

        elmNet.setParent(netData);
        project = new Project("TestProject", prj, index);
    }

    @Test
    public void test() {
        assertEquals("TestProject", project.getName());
        assertSame(prj, project.getRootObject());
    }

    @Test
    public void jsonTest() throws IOException {
        var project2 = roundTripTest(project, Project::writeJson, Project::readJson, "/project.json");
        assertEquals("TestProject", project2.getName());
    }
}
