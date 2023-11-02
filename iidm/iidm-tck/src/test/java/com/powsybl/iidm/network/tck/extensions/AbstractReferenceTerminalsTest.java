/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ReferenceTerminal;
import com.powsybl.iidm.network.extensions.ReferenceTerminals;
import com.powsybl.iidm.network.extensions.ReferenceTerminalsAdder;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerWithExtensionsFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public abstract class AbstractReferenceTerminalsTest {

    Network network;
    Generator gh1;
    Load ld1;
    Line lineS2S3;
    Line lineS3S4;

    @BeforeEach
    void setUp() {
        network = FourSubstationsNodeBreakerWithExtensionsFactory.create();
        gh1 = network.getGenerator("GH1");
        ld1 = network.getLoad("LD1");
        lineS2S3 = network.getLine("LINE_S2S3");
        lineS3S4 = network.getLine("LINE_S3S4");

        gh1.newExtension(ReferenceTerminalsAdder.class).add();
        gh1.getExtension(ReferenceTerminals.class)
                .newReferenceTerminal()
                .setTerminal(gh1.getTerminal())
                .setPriority(1)
                .add();

        ld1.newExtension(ReferenceTerminalsAdder.class).add();
        ld1.getExtension(ReferenceTerminals.class)
                .newReferenceTerminal()
                .setTerminal(ld1.getTerminal())
                .setPriority(2)
                .add();

        lineS2S3.newExtension(ReferenceTerminalsAdder.class).add();
        lineS2S3.getExtension(ReferenceTerminals.class)
                .newReferenceTerminal()
                .setTerminal(lineS2S3.getTerminal1())
                .setPriority(3)
                .add();
        lineS2S3.getExtension(ReferenceTerminals.class)
                .newReferenceTerminal()
                .setTerminal(lineS2S3.getTerminal2())
                .setPriority(4)
                .add();
    }

    @Test
    void test() {
        List<ReferenceTerminal> gh1RefTerminals = gh1.getExtension(ReferenceTerminals.class).getReferenceTerminals();
        assertEquals(1, gh1RefTerminals.size());
        assertEquals(gh1.getTerminal(), gh1RefTerminals.get(0).getTerminal());
        assertEquals(1, gh1RefTerminals.get(0).getPriority());

        List<ReferenceTerminal> ld1RefTerminals = ld1.getExtension(ReferenceTerminals.class).getReferenceTerminals();
        assertEquals(1, ld1RefTerminals.size());
        assertEquals(ld1.getTerminal(), ld1RefTerminals.get(0).getTerminal());
        assertEquals(2, ld1RefTerminals.get(0).getPriority());

        List<ReferenceTerminal> lineS2S3RefTerminals = lineS2S3.getExtension(ReferenceTerminals.class).getReferenceTerminals();
        assertEquals(2, lineS2S3RefTerminals.size());
        assertEquals(lineS2S3.getTerminal1(), lineS2S3RefTerminals.get(0).getTerminal());
        assertEquals(3, lineS2S3RefTerminals.get(0).getPriority());
        assertEquals(lineS2S3.getTerminal2(), lineS2S3RefTerminals.get(1).getTerminal());
        assertEquals(4, lineS2S3RefTerminals.get(1).getPriority());

        assertNull(lineS3S4.getExtension(ReferenceTerminals.class));
    }

    @Test
    void testNoTerminalProvided() {
        lineS3S4.newExtension(ReferenceTerminalsAdder.class).add();
        Throwable thrown = assertThrows(NullPointerException.class, () -> lineS3S4.getExtension(ReferenceTerminals.class)
                .newReferenceTerminal()
                .setPriority(1)
                .add());
        assertEquals("Terminal needs to be set for ReferenceTerminal extension", thrown.getMessage());
    }

    @Test
    void testBadPriority() {
        lineS3S4.newExtension(ReferenceTerminalsAdder.class).add();
        Throwable thrown = assertThrows(PowsyblException.class, () -> lineS3S4.getExtension(ReferenceTerminals.class)
                .newReferenceTerminal()
                .setTerminal(lineS3S4.getTerminal1())
                .setPriority(-2)
                .add());
        assertEquals("Priority should be zero or positive for ReferenceTerminal extension", thrown.getMessage());
    }

    @Test
    void testTerminalNoInConnectable() {
        lineS3S4.newExtension(ReferenceTerminalsAdder.class).add();
        Throwable thrown = assertThrows(PowsyblException.class, () -> lineS3S4.getExtension(ReferenceTerminals.class)
                .newReferenceTerminal()
                .setTerminal(gh1.getTerminal())
                .setPriority(5)
                .add());
        assertEquals("The provided terminal does not below to this connectable", thrown.getMessage());
    }

}
