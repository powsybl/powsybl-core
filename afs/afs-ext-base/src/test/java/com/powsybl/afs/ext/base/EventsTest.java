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
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EventsTest {

    @Test
    public void caseImportedTest() throws IOException {
        CaseImported caseImported = new CaseImported("a", "b", Paths.get("/tmp/foo").toString());
        assertEquals("a", caseImported.getId());
        assertEquals("CASE_IMPORTED", caseImported.getType());
        assertEquals("b", caseImported.getParentId());
        assertNotNull(caseImported.toString());
        assertEquals(Paths.get("/tmp/foo").toString(), caseImported.getPath());

        CaseImported caseImported2 = new CaseImported("a", "c", Paths.get("/tmp/foo").toString());
        assertNotEquals(caseImported, caseImported2);
        assertNotEquals(caseImported.hashCode(), caseImported2.hashCode());
        assertNotEquals(caseImported, new ScriptModified("", "", Paths.get("/tmp/foo").toString()));
    }

    @Test
    public void scriptModifiedTest() throws IOException {
        ScriptModified scriptModified = new ScriptModified("a", "b", Paths.get("/tmp/foo").toString());
        assertEquals("a", scriptModified.getId());
        assertEquals("SCRIPT_MODIFIED", scriptModified.getType());
        assertEquals("b", scriptModified.getParentId());
        assertNotNull(scriptModified.toString());

        ScriptModified scriptModified2 = new ScriptModified("a", "c", Paths.get("/tmp/foo").toString());
        assertNotEquals(scriptModified, scriptModified2);
        assertNotEquals(scriptModified.hashCode(), scriptModified2.hashCode());
        assertNotEquals(scriptModified, new CaseImported("", "", Paths.get("/tmp/foo").toString()));

    }

    @Test
    public void virtualCaseCreatedTest() throws IOException {
        VirtualCaseCreated virtualCaseCreated = new VirtualCaseCreated("a", "b", Paths.get("/tmp/foo").toString());
        assertEquals("a", virtualCaseCreated.getId());
        assertEquals("VIRTUAL_CASE_CREATED", virtualCaseCreated.getType());
        assertEquals("b", virtualCaseCreated.getParentId());
        assertNotNull(virtualCaseCreated.toString());
        assertEquals(Paths.get("/tmp/foo").toString(), virtualCaseCreated.getPath());

        VirtualCaseCreated virtualCaseCreated2 = new VirtualCaseCreated("a", "c", Paths.get("/tmp/foo").toString());
        assertNotEquals(virtualCaseCreated, virtualCaseCreated2);
        assertNotEquals(virtualCaseCreated.hashCode(), virtualCaseCreated2.hashCode());
        assertNotEquals(virtualCaseCreated, new CaseImported("", "", Paths.get("/tmp/foo").toString()));
    }
}
