/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
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

import java.util.Arrays;
import java.util.List;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public abstract class AbstractReferencePrioritiesTest {

    Network network;
    VariantManager variantManager;
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
        variantManager = network.getVariantManager();
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

        ReferencePriority.set(bbs1, 3);
        ReferencePriority.set(bbs2, 8);
        ReferencePriority.set(bbs3, 9);
        ReferencePriority.set(gh1, 1);
        ReferencePriority.set(gh2, 2);
        ReferencePriority.set(gh3, 3);
        ReferencePriority.set(ld1, 2);
        ReferencePriority.set(ld2, 5);
        ReferencePriority.set(ld3, 6);
        ReferencePriority.set(lineS2S3, TwoSides.ONE, 3);
        ReferencePriority.set(lineS2S3, TwoSides.TWO, 0);
    }

    @Test
    public void test() {
        assertEquals(3, ReferencePriority.get(bbs1));
        assertEquals(8, ReferencePriority.get(bbs2));
        assertEquals(9, ReferencePriority.get(bbs3));
        assertEquals(1, ReferencePriority.get(gh1));
        assertEquals(2, ReferencePriority.get(gh2));
        assertEquals(3, ReferencePriority.get(gh3));
        assertEquals(2, ReferencePriority.get(ld1));
        assertEquals(5, ReferencePriority.get(ld2));
        assertEquals(6, ReferencePriority.get(ld3));
        assertEquals(3, ReferencePriority.get(lineS2S3, TwoSides.ONE));
        assertEquals(0, ReferencePriority.get(lineS2S3, TwoSides.TWO));

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
    public void testThreeWindingsTransformer() {
        network = ThreeWindingsTransformerNetworkFactory.create();
        ThreeWindingsTransformer t3wf = network.getThreeWindingsTransformer("3WT");
        assertEquals(0, ReferencePriority.get(t3wf, ThreeSides.ONE));
        assertEquals(0, ReferencePriority.get(t3wf, ThreeSides.TWO));
        assertEquals(0, ReferencePriority.get(t3wf, ThreeSides.THREE));

        ReferencePriority.set(t3wf, ThreeSides.ONE, 4);
        ReferencePriority.set(t3wf, ThreeSides.TWO, 5);
        ReferencePriority.set(t3wf, ThreeSides.THREE, 6);

        assertEquals(4, ReferencePriority.get(t3wf, ThreeSides.ONE));
        assertEquals(5, ReferencePriority.get(t3wf, ThreeSides.TWO));
        assertEquals(6, ReferencePriority.get(t3wf, ThreeSides.THREE));
    }

    @Test
    public void testDeleteAll() {
        ReferencePriorities.delete(network);
        assertEquals(0, ReferencePriorities.get(network).size());
        assertEquals(0, ReferencePriority.get(bbs1));
        assertEquals(0, ReferencePriority.get(gh1));
        assertEquals(0, ReferencePriority.get(ld1));
        assertEquals(0, ReferencePriority.get(lineS2S3, TwoSides.ONE));
    }

    @Test
    public void testVariants() {
        // create variants
        String variant1 = "variant1";
        String variant2 = "variant2";
        List<String> targetVariantIds = Arrays.asList(variant1, variant2);
        variantManager.cloneVariant(INITIAL_VARIANT_ID, targetVariantIds);

        // add gh2 priority to 20 in variant1
        variantManager.setWorkingVariant(variant1);
        ReferencePriority.set(gh2, 20);
        // add gh3 priority to 0 in variant2
        variantManager.setWorkingVariant(variant2);
        ReferencePriority.set(gh3, 0);

        // initial variant unmodified
        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        assertEquals(2, ReferencePriority.get(gh2));
        assertEquals(3, ReferencePriority.get(gh3));

        // check variant 1 as expected
        variantManager.setWorkingVariant(variant1);
        assertEquals(20, ReferencePriority.get(gh2));

        // check variant 2 as expected
        variantManager.setWorkingVariant(variant2);
        assertEquals(0, ReferencePriority.get(gh3));

        // clear variant 1
        variantManager.setWorkingVariant(variant1);
        ReferencePriorities.delete(network);

        // check variant 1 empty
        assertTrue(ReferencePriorities.get(network).isEmpty());

        // check other variants unchanged
        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        assertEquals(10, ReferencePriorities.get(network).size());
        variantManager.setWorkingVariant(variant2);
        assertEquals(9, ReferencePriorities.get(network).size());

        // test variant recycling in array
        String variant3 = "variant3";
        variantManager.removeVariant(variant1);
        variantManager.cloneVariant(variant2, variant3);
        variantManager.setWorkingVariant(variant3);
        assertEquals(9, ReferencePriorities.get(network).size());
        assertEquals(0, ReferencePriority.get(gh3));

        // test array resize for coverage completeness
        variantManager.removeVariant(variant2);
    }

    @Test
    public void testNoTerminalProvided() {
        lineS3S4.newExtension(ReferencePrioritiesAdder.class).add();
        Throwable thrown = assertThrows(NullPointerException.class, () -> lineS3S4.getExtension(ReferencePriorities.class)
                .newReferencePriority()
                .setPriority(1)
                .add());
        assertEquals("Terminal needs to be set for ReferencePriority extension", thrown.getMessage());
    }

    @Test
    public void testBadPriority() {
        lineS3S4.newExtension(ReferencePrioritiesAdder.class).add();
        Throwable thrown = assertThrows(PowsyblException.class, () -> lineS3S4.getExtension(ReferencePriorities.class)
                .newReferencePriority()
                .setTerminal(lineS3S4.getTerminal1())
                .setPriority(-2)
                .add());
        assertEquals("Priority (-2) of terminal (equipment LINE_S3S4) should be zero or positive for ReferencePriority extension", thrown.getMessage());
    }

    @Test
    public void testTerminalNotInConnectable() {
        lineS3S4.newExtension(ReferencePrioritiesAdder.class).add();
        Throwable thrown = assertThrows(PowsyblException.class, () -> lineS3S4.getExtension(ReferencePriorities.class)
                .newReferencePriority()
                .setTerminal(gh1.getTerminal())
                .setPriority(5)
                .add());
        assertEquals("The provided terminal does not belong to the connectable LINE_S3S4", thrown.getMessage());
    }

}
