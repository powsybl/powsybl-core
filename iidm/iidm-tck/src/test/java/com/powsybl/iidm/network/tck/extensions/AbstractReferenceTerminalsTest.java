/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ReferenceTerminal;
import com.powsybl.iidm.network.extensions.ReferenceTerminals;
import com.powsybl.iidm.network.extensions.ReferenceTerminalsAdder;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public abstract class AbstractReferenceTerminalsTest {

    Network network;
    BusbarSection bbs1;
    BusbarSection bbs2;
    BusbarSection bbs3;
    Generator gh1;
    Generator gh2;
    Generator gh3;
    Load ld1;
    Load ld2;
    Load ld3;
    Line lineS2S3;
    Line lineS3S4;

    @BeforeEach
    void setUp() {
        network = FourSubstationsNodeBreakerFactory.create();
        bbs1 = network.getBusbarSection("S1VL1_BBS");
        bbs2 = network.getBusbarSection("S2VL1_BBS");
        bbs3 = network.getBusbarSection("S3VL1_BBS");
        gh1 = network.getGenerator("GH1");
        gh2 = network.getGenerator("GH2");
        gh3 = network.getGenerator("GH3");
        ld1 = network.getLoad("LD1");
        ld2 = network.getLoad("LD2");
        ld3 = network.getLoad("LD3");
        lineS2S3 = network.getLine("LINE_S2S3");
        lineS3S4 = network.getLine("LINE_S3S4");

        ReferenceTerminals.setPriority(bbs1, 3);
        ReferenceTerminals.setPriority(bbs2, 8);
        ReferenceTerminals.setPriority(bbs3, 9);
        ReferenceTerminals.setPriority(gh1, 1);
        ReferenceTerminals.setPriority(gh2, 2);
        ReferenceTerminals.setPriority(gh3, 3);
        ReferenceTerminals.setPriority(ld1, 2);
        ReferenceTerminals.setPriority(ld2, 5);
        ReferenceTerminals.setPriority(ld3, 6);
        ReferenceTerminals.setPriority(lineS2S3, Branch.Side.ONE, 3);
        ReferenceTerminals.setPriority(lineS2S3, Branch.Side.TWO, 0);
    }

    @Test
    void test() {
        assertEquals(3, ReferenceTerminals.getPriority(bbs1));
        assertEquals(8, ReferenceTerminals.getPriority(bbs2));
        assertEquals(9, ReferenceTerminals.getPriority(bbs3));
        assertEquals(1, ReferenceTerminals.getPriority(gh1));
        assertEquals(2, ReferenceTerminals.getPriority(gh2));
        assertEquals(3, ReferenceTerminals.getPriority(gh3));
        assertEquals(2, ReferenceTerminals.getPriority(ld1));
        assertEquals(5, ReferenceTerminals.getPriority(ld2));
        assertEquals(6, ReferenceTerminals.getPriority(ld3));
        assertEquals(3, ReferenceTerminals.getPriority(lineS2S3, Branch.Side.ONE));
        assertEquals(0, ReferenceTerminals.getPriority(lineS2S3, Branch.Side.TWO));

        assertNull(lineS3S4.getExtension(ReferenceTerminals.class));

        List<ReferenceTerminal> referenceTerminals = ReferenceTerminals.get(network);
        assertEquals(10, referenceTerminals.size());
        assertEquals(gh1.getTerminal(), referenceTerminals.get(0).getTerminal()); // p1
        assertEquals(gh2.getTerminal(), referenceTerminals.get(1).getTerminal()); // p2 - gen
        assertEquals(ld1.getTerminal(), referenceTerminals.get(2).getTerminal()); // p2 - load
        assertEquals(gh3.getTerminal(), referenceTerminals.get(3).getTerminal()); // p3 - gen
        assertEquals(bbs1.getTerminal(), referenceTerminals.get(4).getTerminal()); // p3 - bbs
        assertEquals(lineS2S3.getTerminal1(), referenceTerminals.get(5).getTerminal()); // p3 - line
        assertEquals(ld2.getTerminal(), referenceTerminals.get(6).getTerminal()); // p5
        assertEquals(ld3.getTerminal(), referenceTerminals.get(7).getTerminal()); // p6
        assertEquals(bbs2.getTerminal(), referenceTerminals.get(8).getTerminal()); // p8
        assertEquals(bbs3.getTerminal(), referenceTerminals.get(9).getTerminal()); // p9

        Throwable thrownGet = assertThrows(PowsyblException.class, () -> ReferenceTerminals.getPriority(lineS2S3));
        assertEquals("This method can only be used on a connectable having a single Terminal", thrownGet.getMessage());
        Throwable thrownSet = assertThrows(PowsyblException.class, () -> ReferenceTerminals.setPriority(lineS2S3, 1));
        assertEquals("This method can only be used on a connectable having a single Terminal", thrownSet.getMessage());
    }

    @Test
    void testThreeWindingsTransformer() {
        network = ThreeWindingsTransformerNetworkFactory.create();
        ThreeWindingsTransformer t3wf = network.getThreeWindingsTransformer("3WT");
        assertEquals(0, ReferenceTerminals.getPriority(t3wf, ThreeWindingsTransformer.Side.ONE));
        assertEquals(0, ReferenceTerminals.getPriority(t3wf, ThreeWindingsTransformer.Side.TWO));
        assertEquals(0, ReferenceTerminals.getPriority(t3wf, ThreeWindingsTransformer.Side.THREE));

        ReferenceTerminals.setPriority(t3wf, ThreeWindingsTransformer.Side.ONE, 4);
        ReferenceTerminals.setPriority(t3wf, ThreeWindingsTransformer.Side.TWO, 5);
        ReferenceTerminals.setPriority(t3wf, ThreeWindingsTransformer.Side.THREE, 6);

        assertEquals(4, ReferenceTerminals.getPriority(t3wf, ThreeWindingsTransformer.Side.ONE));
        assertEquals(5, ReferenceTerminals.getPriority(t3wf, ThreeWindingsTransformer.Side.TWO));
        assertEquals(6, ReferenceTerminals.getPriority(t3wf, ThreeWindingsTransformer.Side.THREE));

        Throwable thrownGet = assertThrows(PowsyblException.class, () -> ReferenceTerminals.getPriority(t3wf));
        assertEquals("This method can only be used on a connectable having a single Terminal", thrownGet.getMessage());
        Throwable thrownSet = assertThrows(PowsyblException.class, () -> ReferenceTerminals.setPriority(t3wf, 1));
        assertEquals("This method can only be used on a connectable having a single Terminal", thrownSet.getMessage());
    }

    @Test
    void testDeleteAll() {
        ReferenceTerminals.delete(network);
        assertEquals(0, ReferenceTerminals.get(network).size());
        assertEquals(0, ReferenceTerminals.getPriority(bbs1));
        assertEquals(0, ReferenceTerminals.getPriority(gh1));
        assertEquals(0, ReferenceTerminals.getPriority(ld1));
        assertEquals(0, ReferenceTerminals.getPriority(lineS2S3, Branch.Side.ONE));
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
    void testTerminalNotInConnectable() {
        lineS3S4.newExtension(ReferenceTerminalsAdder.class).add();
        Throwable thrown = assertThrows(PowsyblException.class, () -> lineS3S4.getExtension(ReferenceTerminals.class)
                .newReferenceTerminal()
                .setTerminal(gh1.getTerminal())
                .setPriority(5)
                .add());
        assertEquals("The provided terminal does not belong to this connectable", thrown.getMessage());
    }

}
