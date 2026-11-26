/**
 * Copyright (c) 2026, Elia Group (http://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.model.statement;

import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class DeferredCgmesObjectSinkTest {

    @Test
    void keepsOneObjectForEachSubject() throws XMLStreamException {
        DeferredCgmesObjectSink sink = new DeferredCgmesObjectSink();
        sink.accept(terminal("T1", true));
        sink.accept(CgmesObject.builder("Breaker", "SW1").literal("Switch.open", "false").build());
        sink.accept(terminal("T1", false));

        // The second object of T1 replaces the first one, and T1 keeps its first position
        List<CgmesObject> expected = List.of(terminal("T1", false), CgmesObject.builder("Breaker", "SW1").literal("Switch.open", "false").build());
        assertEquals(expected, List.copyOf(sink.getObjects()));

        // The collected objects are sent in the same order
        List<CgmesObject> sent = new ArrayList<>();
        sink.sendAll(sent::add);
        assertEquals(expected, sent);
    }

    private static CgmesObject terminal(String subject, boolean connected) {
        return CgmesObject.builder("Terminal", subject).literal("ACDCTerminal.connected", String.valueOf(connected)).build();
    }
}
