/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.modification.tap.PhaseTapPositionModification;
import com.powsybl.iidm.modification.tap.RatioTapPositionModification;
import com.powsybl.iidm.modification.tap.TapType;
import com.powsybl.iidm.modification.tap.TransformerType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TapChanger;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas PIERRE <nicolas.pierre at artelys.com>
 */
class TapPositionModificationTest {
    private Network network;
    private Network threeWindingNetwork;
    private TwoWindingsTransformer twoWindingsTransformer;
    private ThreeWindingsTransformer threeWindingTransformer;
    private static final int LEG_NUM = 1; // Using leg number 2
    private ThreeWindingsTransformer.Leg threeWindingTransformerLeg;

    @BeforeEach
    public void setUp() {
        network = FourSubstationsNodeBreakerFactory.create();
        threeWindingNetwork = ThreeWindingsTransformerNetworkFactory.create();
        threeWindingTransformer = threeWindingNetwork.getThreeWindingsTransformers().iterator().next();
        twoWindingsTransformer = network.getTwoWindingsTransformerStream().findAny().orElseThrow();
        assertTrue(twoWindingsTransformer.hasPhaseTapChanger());
        assertTrue(twoWindingsTransformer.hasRatioTapChanger());
        twoWindingsTransformer.getPhaseTapChanger()
            .setTapPosition(twoWindingsTransformer.getPhaseTapChanger().getHighTapPosition());
        twoWindingsTransformer.getRatioTapChanger()
            .setTapPosition(twoWindingsTransformer.getRatioTapChanger().getHighTapPosition());
        threeWindingTransformerLeg = threeWindingTransformer.getLegStream().collect(Collectors.toList()).get(LEG_NUM);
    }

    @Test
    void testArgumentCoherence() {
        // Good
        String id = twoWindingsTransformer.getId();
        int optionalLeg = 2;
        int optionalBadLeg = 3;
        assertDoesNotThrow(
            () -> PhaseTapPositionModification.createTwoWindingsPtcPosition(id, 0));
        // Log warning, but good, Leg value is ignored
        assertDoesNotThrow(
            () -> PhaseTapPositionModification.createTwoWindingsPtcPosition(id, 0));
        // good
        assertDoesNotThrow(
            () -> PhaseTapPositionModification.createThreeWindingsPtcPosition(id, 0, optionalLeg));
        // bad should throw
        assertThrows(PowsyblException.class,
            () -> new PhaseTapPositionModification(id, TransformerType.THREE_WINDINGS_TRANSFORMER, 0, null),
            "Constructor should throw on three winding without specifying the leg.");
        // bad should throw
        assertThrows(PowsyblException.class,
            () -> PhaseTapPositionModification.createThreeWindingsPtcPosition(id, 0, optionalBadLeg),
            "Constructor should throw on three winding with a wrong leg number.");
    }

    @Test
    void testUnknownId() {
        NetworkModification modif = new PhaseTapPositionModification("UNKNOWN_ID",
            TransformerType.TWO_WINDINGS_TRANSFORMER, 5, null);
        assertThrows(PowsyblException.class, () -> modif.apply(network, true, Reporter.NO_OP));
        assertDoesNotThrow(() -> modif.apply(network, false, Reporter.NO_OP),
            "An invalid ID should not throw if throwException is false.");
        NetworkModification modif2 = new RatioTapPositionModification("UNKNOWN_ID",
            TransformerType.TWO_WINDINGS_TRANSFORMER, 5, null);
        assertThrows(PowsyblException.class, () -> modif2.apply(network, true, Reporter.NO_OP));
        assertDoesNotThrow(() -> modif2.apply(network, false, Reporter.NO_OP),
            "An invalid ID should not throw if throwException is false.");

    }

    @Test
    void testTwoWindingsModif() {
        testTapTransformer(twoWindingsTransformer.getPhaseTapChanger(),
            TransformerType.TWO_WINDINGS_TRANSFORMER,
            TapType.PHASE,
            twoWindingsTransformer.getId());
        testTapTransformer(twoWindingsTransformer.getRatioTapChanger(),
            TransformerType.TWO_WINDINGS_TRANSFORMER,
            TapType.RATIO,
            twoWindingsTransformer.getId());
    }

    @Test
    void testThreeWindingsModif() {
        ThreeWindingsTransformer.Leg leg = threeWindingTransformer.getLeg2();
        leg.newPhaseTapChanger()
            .setLowTapPosition(0)
            .setTapPosition(0)
            .beginStep()
            .setR(0.01)
            .setX(0.0001)
            .setB(0)
            .setG(0)
            .setRho(1.1)
            .setAlpha(1)
            .endStep()
            .beginStep()
            .setR(0.02)
            .setX(0.0002)
            .setB(0)
            .setG(0)
            .setRho(1.2)
            .setAlpha(1.1)
            .endStep()
            .add();
        testTapTransformer(threeWindingTransformerLeg.getPhaseTapChanger(),
            TransformerType.THREE_WINDINGS_TRANSFORMER,
            TapType.PHASE,
            threeWindingTransformer.getId());
        testTapTransformer(threeWindingTransformerLeg.getRatioTapChanger(),
            TransformerType.THREE_WINDINGS_TRANSFORMER,
            TapType.RATIO,
            threeWindingTransformer.getId());
    }

    private void testTapTransformer(TapChanger<?, ?> tapChanger,
                                    final TransformerType transformerElement,
                                    final TapType tapType,
                                    final String transformerId) {
        Supplier<Integer> tapPositionSupplier = tapChanger::getTapPosition;
        assertTrue(
            tapChanger.getLowTapPosition() < tapChanger.getHighTapPosition());

        testValidTapPos(tapType, tapChanger.getHighTapPosition() - 1,
            transformerId, transformerElement, tapPositionSupplier);
        int actualPtcPos = tapChanger.getTapPosition();
        testInvalidTapPosition(actualPtcPos, tapType, transformerElement,
            tapChanger.getHighTapPosition() + 1, transformerId, tapPositionSupplier);
        testInvalidTapPosition(actualPtcPos, tapType, transformerElement,
            tapChanger.getLowTapPosition() - 1, transformerId, tapPositionSupplier);
    }

    private void testValidTapPos(final TapType type, final int tapPos,
                                 final String transformerId, final TransformerType element,
                                 final Supplier<Integer> tapPositionSupplier) {
        Integer leg;
        Network networkToApply;
        NetworkModification modif;
        if (TransformerType.TWO_WINDINGS_TRANSFORMER.equals(element)) {
            leg = null;
            networkToApply = network;
        } else {
            leg = LEG_NUM;
            networkToApply = threeWindingNetwork;
        }
        if (TapType.RATIO.equals(type)) {
            modif = new RatioTapPositionModification(transformerId, element, tapPos, leg);
        } else { // type ==  TapType.Phase
            modif = new PhaseTapPositionModification(transformerId, element, tapPos, leg);
        }
        modif.apply(networkToApply);
        assertEquals(tapPos, tapPositionSupplier.get(), "Tap Modification did not change the network");
    }

    private void testInvalidTapPosition(int currentTapPos, final TapType type,
                                        TransformerType element, int invalidTapPos,
                                        final String transformerId, final Supplier<Integer> tapPositionSupplier) {
        Integer leg;
        Network networkToApply;
        NetworkModification modif;
        if (TransformerType.TWO_WINDINGS_TRANSFORMER.equals(element)) {
            leg = null;
            networkToApply = network;
        } else {
            leg = LEG_NUM;
            networkToApply = threeWindingNetwork;
        }
        if (TapType.RATIO.equals(type)) {
            modif = new RatioTapPositionModification(transformerId, element, invalidTapPos, leg);
        } else { // type ==  TapType.Phase
            modif = new PhaseTapPositionModification(transformerId, element, invalidTapPos, leg);
        }
        assertThrows(PowsyblException.class, () -> modif.apply(networkToApply, true, Reporter.NO_OP));
        assertEquals(currentTapPos, tapPositionSupplier.get(),
            "Invalid tap position should not be applied to the network");
        modif.apply(networkToApply, false, Reporter.NO_OP);
        assertEquals(currentTapPos, tapPositionSupplier.get(),
            "Invalid tap position should not be applied to the network");
    }

}
