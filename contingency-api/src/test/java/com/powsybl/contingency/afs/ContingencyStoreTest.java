/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.afs;

import com.google.common.collect.ImmutableList;
import com.powsybl.afs.AbstractProjectFileTest;
import com.powsybl.afs.Project;
import com.powsybl.afs.ProjectFileExtension;
import com.powsybl.afs.mapdb.storage.MapDbAppStorage;
import com.powsybl.afs.storage.AppStorage;
import com.powsybl.contingency.BranchContingency;
import com.powsybl.contingency.Contingency;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ContingencyStoreTest extends AbstractProjectFileTest {

    @Override
    protected AppStorage createStorage() {
        return MapDbAppStorage.createHeap("mem");
    }

    @Override
    protected List<ProjectFileExtension> getProjectFileExtensions() {
        return ImmutableList.of(new ContingencyStoreExtension());
    }

    @Test
    public void test() {
        // create project in the root folder
        Project project = afs.getRootFolder().createProject("project");

        // create contingency list
        ContingencyStore contingencyStore = project.getRootFolder().fileBuilder(ContingencyStoreBuilder.class)
                .withName("contingencies")
                .build();
        List<Contingency> contingencies = Collections.singletonList(new Contingency("c1", new BranchContingency("l1")));
        contingencyStore.write(contingencies);
        assertEquals(contingencies, contingencyStore.read());
    }
}
