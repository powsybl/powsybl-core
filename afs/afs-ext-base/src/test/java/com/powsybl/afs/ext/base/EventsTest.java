/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.ext.base.events.CaseImported;
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
}
