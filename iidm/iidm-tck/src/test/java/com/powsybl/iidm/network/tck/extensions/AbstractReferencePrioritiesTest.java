/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ReferencePriorities;
import com.powsybl.iidm.network.extensions.ReferencePriority;
import com.powsybl.iidm.network.extensions.ReferencePrioritiesAdder;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public abstract class AbstractReferencePrioritiesTest {

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

        ReferencePriorities.setPriority(bbs1, 3);
        ReferencePriorities.setPriority(bbs2, 8);
        ReferencePriorities.setPriority(bbs3, 9);
        ReferencePriorities.setPriority(gh1, 1);
        ReferencePriorities.setPriority(gh2, 2);
        ReferencePriorities.setPriority(gh3, 3);
        ReferencePriorities.setPriority(ld1, 2);
        ReferencePriorities.setPriority(ld2, 5);
        ReferencePriorities.setPriority(ld3, 6);
        ReferencePriorities.setPriority(lineS2S3, Branch.Side.ONE, 3);
        ReferencePriorities.setPriority(lineS2S3, Branch.Side.TWO, 0);
    }

    @Test
    void test() {
        assertEquals(3, ReferencePriorities.getPriority(bbs1));
        assertEquals(8, ReferencePriorities.getPriority(bbs2));
        assertEquals(9, ReferencePriorities.getPriority(bbs3));
        assertEquals(1, ReferencePriorities.getPriority(gh1));
        assertEquals(2, ReferencePriorities.getPriority(gh2));
        assertEquals(3, ReferencePriorities.getPriority(gh3));
        assertEquals(2, ReferencePriorities.getPriority(ld1));
        assertEquals(5, ReferencePriorities.getPriority(ld2));
        assertEquals(6, ReferencePriorities.getPriority(ld3));
        assertEquals(3, ReferencePriorities.getPriority(lineS2S3, Branch.Side.ONE));
        assertEquals(0, ReferencePriorities.getPriority(lineS2S3, Branch.Side.TWO));

        assertNull(lineS3S4.getExtension(ReferencePriorities.class));

        List<ReferencePriority> referencePriorities = ReferencePriorities.get(network);
        assertEquals(10, referencePriorities.size());
        assertEquals(gh1.getTerminal(), referencePriorities.get(0).getTerminal()); // p1
        assertEquals(gh2.getTerminal(), referencePriorities.get(1).getTerminal()); // p2 - gen
        assertEquals(ld1.getTerminal(), referencePriorities.get(2).getTerminal()); // p2 - load
        assertEquals(gh3.getTerminal(), referencePriorities.get(3).getTerminal()); // p3 - gen
        assertEquals(bbs1.getTerminal(), referencePriorities.get(4).getTerminal()); // p3 - bbs
        assertEquals(lineS2S3.getTerminal1(), referencePriorities.get(5).getTerminal()); // p3 - line
        assertEquals(ld2.getTerminal(), referencePriorities.get(6).getTerminal()); // p5
        assertEquals(ld3.getTerminal(), referencePriorities.get(7).getTerminal()); // p6
        assertEquals(bbs2.getTerminal(), referencePriorities.get(8).getTerminal()); // p8
        assertEquals(bbs3.getTerminal(), referencePriorities.get(9).getTerminal()); // p9
    }

    @Test
    void testThrowsMultipleTerminals() {
        Throwable thrownGet = assertThrows(PowsyblException.class, () -> ReferencePriorities.getPriority(lineS2S3));
        assertEquals("This method can only be used on a connectable having a single Terminal", thrownGet.getMessage());
        Throwable thrownSet = assertThrows(PowsyblException.class, () -> ReferencePriorities.setPriority(lineS2S3, 1));
        assertEquals("This method can only be used on a connectable having a single Terminal", thrownSet.getMessage());
    }

    @Test
    void testThreeWindingsTransformer() {
        network = ThreeWindingsTransformerNetworkFactory.create();
        ThreeWindingsTransformer t3wf = network.getThreeWindingsTransformer("3WT");
        assertEquals(0, ReferencePriorities.getPriority(t3wf, ThreeWindingsTransformer.Side.ONE));
        assertEquals(0, ReferencePriorities.getPriority(t3wf, ThreeWindingsTransformer.Side.TWO));
        assertEquals(0, ReferencePriorities.getPriority(t3wf, ThreeWindingsTransformer.Side.THREE));

        ReferencePriorities.setPriority(t3wf, ThreeWindingsTransformer.Side.ONE, 4);
        ReferencePriorities.setPriority(t3wf, ThreeWindingsTransformer.Side.TWO, 5);
        ReferencePriorities.setPriority(t3wf, ThreeWindingsTransformer.Side.THREE, 6);

        assertEquals(4, ReferencePriorities.getPriority(t3wf, ThreeWindingsTransformer.Side.ONE));
        assertEquals(5, ReferencePriorities.getPriority(t3wf, ThreeWindingsTransformer.Side.TWO));
        assertEquals(6, ReferencePriorities.getPriority(t3wf, ThreeWindingsTransformer.Side.THREE));

        Throwable thrownGet = assertThrows(PowsyblException.class, () -> ReferencePriorities.getPriority(t3wf));
        assertEquals("This method can only be used on a connectable having a single Terminal", thrownGet.getMessage());
        Throwable thrownSet = assertThrows(PowsyblException.class, () -> ReferencePriorities.setPriority(t3wf, 1));
        assertEquals("This method can only be used on a connectable having a single Terminal", thrownSet.getMessage());
    }

    @Test
    void testDeleteAll() {
        ReferencePriorities.delete(network);
        assertEquals(0, ReferencePriorities.get(network).size());
        assertEquals(0, ReferencePriorities.getPriority(bbs1));
        assertEquals(0, ReferencePriorities.getPriority(gh1));
        assertEquals(0, ReferencePriorities.getPriority(ld1));
        assertEquals(0, ReferencePriorities.getPriority(lineS2S3, Branch.Side.ONE));
    }

    @Test
    void testNoTerminalProvided() {
        lineS3S4.newExtension(ReferencePrioritiesAdder.class).add();
        Throwable thrown = assertThrows(NullPointerException.class, () -> lineS3S4.getExtension(ReferencePriorities.class)
                .newReferencePriority()
                .setPriority(1)
                .add());
        assertEquals("Terminal needs to be set for ReferencePriority extension", thrown.getMessage());
    }

    @Test
    void testBadPriority() {
        lineS3S4.newExtension(ReferencePrioritiesAdder.class).add();
        Throwable thrown = assertThrows(PowsyblException.class, () -> lineS3S4.getExtension(ReferencePriorities.class)
                .newReferencePriority()
                .setTerminal(lineS3S4.getTerminal1())
                .setPriority(-2)
                .add());
        assertEquals("Priority should be zero or positive for ReferencePriority extension", thrown.getMessage());
    }

    @Test
    void testTerminalNotInConnectable() {
        lineS3S4.newExtension(ReferencePrioritiesAdder.class).add();
        Throwable thrown = assertThrows(PowsyblException.class, () -> lineS3S4.getExtension(ReferencePriorities.class)
                .newReferencePriority()
                .setTerminal(gh1.getTerminal())
                .setPriority(5)
                .add());
        assertEquals("The provided terminal does not belong to this connectable", thrown.getMessage());
    }

}
