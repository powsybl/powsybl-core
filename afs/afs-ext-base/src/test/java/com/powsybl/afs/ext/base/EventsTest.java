/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.ext.base.events.CaseImported;
import com.powsybl.afs.ext.base.events.ScriptModified;
import com.powsybl.afs.ext.base.events.VirtualCaseCreated;
import com.powsybl.afs.storage.events.*;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EventsTest {

    @Test
    public void caseImportedTest() throws IOException {
        CaseImported caseImported = new CaseImported("a", "b");
        assertEquals("a", caseImported.getId());
        assertEquals(NodeEventType.CASE_IMPORTED, caseImported.getType());
        assertEquals("b", caseImported.getParentId());

        CaseImported caseImported2 = new CaseImported("a", "c");
        assertNotEquals(caseImported, caseImported2);
    }

    @Test
    public void scriptModifiedTest() throws IOException {
        ScriptModified scriptModified = new ScriptModified("a", "b");
        assertEquals("a", scriptModified.getId());
        assertEquals(NodeEventType.SCRIPT_MODIFIED, scriptModified.getType());
        assertEquals("b", scriptModified.getParentId());

        ScriptModified scriptModified2 = new ScriptModified("a", "c");
        assertNotEquals(scriptModified, scriptModified2);
    }

    @Test
    public void virtualCaseCreatedTest() throws IOException {
        VirtualCaseCreated virtualCaseCreated = new VirtualCaseCreated("a", "b");
        assertEquals("a", virtualCaseCreated.getId());
        assertEquals(NodeEventType.VIRTUAL_CASE_CREATED, virtualCaseCreated.getType());
        assertEquals("b", virtualCaseCreated.getParentId());

        VirtualCaseCreated virtualCaseCreated2 = new VirtualCaseCreated("a", "c");
        assertNotEquals(virtualCaseCreated, virtualCaseCreated2);
    }
}
