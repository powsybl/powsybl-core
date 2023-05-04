/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas PIERRE <nicolas.pierre at artelys.com>
 */
class TapPositionModificationTest {
    private Network network;
    private Network threeWindingNetwork;
    private TwoWindingsTransformer twoWindingsTransformer;
    private ThreeWindingsTransformer threeWindingTransformer;

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
    }

    @Test
    void testArgumentCoherence() {
        // Good
        String id = twoWindingsTransformer.getId();
        OptionalInt empty = OptionalInt.empty();
        OptionalInt optionalLeg = OptionalInt.of(2);
        OptionalInt optionalBadLeg = OptionalInt.of(3);
        assertDoesNotThrow(
            () -> new TapPositionModification(id, TapPositionModification.TransformerElement.TWO_WINDING_TRANSFORMER,
                TapPositionModification.TapType.PHASE, 0, empty));
        // Log warning, but good, Leg value is ignored
        assertDoesNotThrow(
            () -> new TapPositionModification(id, TapPositionModification.TransformerElement.TWO_WINDING_TRANSFORMER,
                TapPositionModification.TapType.PHASE, 0, optionalLeg));
        // good
        assertDoesNotThrow(
            () -> new TapPositionModification(id, TapPositionModification.TransformerElement.THREE_WINDING_TRANSFORMER,
                TapPositionModification.TapType.PHASE, 0, optionalLeg));
        // bad should throw
        assertThrows(PowsyblException.class,
            () -> new TapPositionModification(id, TapPositionModification.TransformerElement.THREE_WINDING_TRANSFORMER,
                TapPositionModification.TapType.PHASE, 0, empty),
            "Constructor should throw on three winding without specifying the leg.");
        // bad should throw
        assertThrows(PowsyblException.class,
            () -> new TapPositionModification(id, TapPositionModification.TransformerElement.THREE_WINDING_TRANSFORMER,
                TapPositionModification.TapType.PHASE, 0, optionalBadLeg),
            "Constructor should throw on three winding with a wrong leg number.");
    }

    @Test
    void testUnknownId() {
        TapPositionModification modif = new TapPositionModification("UNKNOWN_ID",
            TapPositionModification.TransformerElement.TWO_WINDING_TRANSFORMER, TapPositionModification.TapType.PHASE,
            5, OptionalInt.empty());
        assertThrows(PowsyblException.class, () -> modif.apply(network, true, Reporter.NO_OP));
        assertDoesNotThrow(() -> modif.apply(network, false, Reporter.NO_OP),
            "An invalid ID should not throw if throwException is false.");
        TapPositionModification modif2 = new TapPositionModification("UNKNOWN_ID",
            TapPositionModification.TransformerElement.TWO_WINDING_TRANSFORMER, TapPositionModification.TapType.RATIO,
            5, OptionalInt.empty());
        assertThrows(PowsyblException.class, () -> modif2.apply(network, true, Reporter.NO_OP));
        assertDoesNotThrow(() -> modif2.apply(network, false, Reporter.NO_OP),
            "An invalid ID should not throw if throwException is false.");

    }

    @Test
    void testTwoWindingsModif() {
        testRtcTransformer(twoWindingsTransformer, TapPositionModification.TransformerElement.TWO_WINDING_TRANSFORMER,
            twoWindingsTransformer.getId());
        testPtcTransformer(twoWindingsTransformer, TapPositionModification.TransformerElement.TWO_WINDING_TRANSFORMER,
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
        testRtcTransformer(leg, TapPositionModification.TransformerElement.THREE_WINDING_TRANSFORMER,
            threeWindingTransformer.getId());
        testPtcTransformer(leg, TapPositionModification.TransformerElement.THREE_WINDING_TRANSFORMER,
            threeWindingTransformer.getId());
    }

    private void testPtcTransformer(PhaseTapChangerHolder ptcHolder,
                                    final TapPositionModification.TransformerElement transformerElement,
                                    final String transformerId) {
        Supplier<Integer> tapPositionSupplier = ptcHolder.getPhaseTapChanger()::getTapPosition;

        assertTrue(
            ptcHolder.getPhaseTapChanger().getLowTapPosition() < ptcHolder.getPhaseTapChanger().getHighTapPosition());

        testValidTapPos(TapPositionModification.TapType.PHASE, ptcHolder.getPhaseTapChanger().getHighTapPosition() - 1,
            transformerId, transformerElement, tapPositionSupplier);
        int actualPtcPos = ptcHolder.getPhaseTapChanger().getTapPosition();
        testInvalidTapPosition(actualPtcPos, TapPositionModification.TapType.PHASE, transformerElement,
            ptcHolder.getPhaseTapChanger().getHighTapPosition() + 1, transformerId, tapPositionSupplier);
        testInvalidTapPosition(actualPtcPos, TapPositionModification.TapType.PHASE, transformerElement,
            ptcHolder.getPhaseTapChanger().getLowTapPosition() - 1, transformerId, tapPositionSupplier);
    }

    private void testRtcTransformer(RatioTapChangerHolder rtcHolder,
                                    final TapPositionModification.TransformerElement transformerElement,
                                    final String transformerId) {
        Supplier<Integer> tapPositionSupplier = rtcHolder.getRatioTapChanger()::getTapPosition;
        // This assert is an assumption for the rest of the test.
        assertTrue(
            rtcHolder.getRatioTapChanger().getLowTapPosition() < rtcHolder.getRatioTapChanger().getHighTapPosition());
        testValidTapPos(TapPositionModification.TapType.RATIO, rtcHolder.getRatioTapChanger().getHighTapPosition() - 1,
            transformerId, transformerElement, tapPositionSupplier);

        int actualRtcPos = rtcHolder.getRatioTapChanger().getTapPosition();
        testInvalidTapPosition(actualRtcPos, TapPositionModification.TapType.RATIO, transformerElement,
            rtcHolder.getRatioTapChanger().getHighTapPosition() + 1, transformerId, tapPositionSupplier);
        testInvalidTapPosition(actualRtcPos, TapPositionModification.TapType.RATIO, transformerElement,
            rtcHolder.getRatioTapChanger().getLowTapPosition() - 1, transformerId, tapPositionSupplier);
    }

    private void testValidTapPos(final TapPositionModification.TapType type, final int tapPos,
                                 final String transformerId, final TapPositionModification.TransformerElement element,
                                 final Supplier<Integer> tapPositionSupplier) {
        OptionalInt optLeg;
        Network networkToApply;
        if (TapPositionModification.TransformerElement.TWO_WINDING_TRANSFORMER.equals(element)) {
            optLeg = OptionalInt.empty();
            networkToApply = network;
        } else {
            // using leg2
            optLeg = OptionalInt.of(1);
            networkToApply = threeWindingNetwork;
        }
        TapPositionModification modif = new TapPositionModification(transformerId, element, type, tapPos, optLeg);
        modif.apply(networkToApply);
        assertEquals(tapPos, tapPositionSupplier.get(), "Tap Modification did not change the network");
    }

    private void testInvalidTapPosition(int currentTapPos, final TapPositionModification.TapType type,
                                        TapPositionModification.TransformerElement element, int invalidTapPos,
                                        final String id, final Supplier<Integer> tapPositionSupplier) {
        OptionalInt optLeg;
        Network networkToApply;
        if (TapPositionModification.TransformerElement.TWO_WINDING_TRANSFORMER.equals(element)) {
            optLeg = OptionalInt.empty();
            networkToApply = network;
        } else {
            // using leg2
            optLeg = OptionalInt.of(1);
            networkToApply = threeWindingNetwork;
        }
        TapPositionModification modif = new TapPositionModification(id, element, type, invalidTapPos, optLeg);
        assertThrows(PowsyblException.class, () -> modif.apply(networkToApply, true, Reporter.NO_OP));
        assertEquals(currentTapPos, tapPositionSupplier.get(),
            "Invalid tap position should not be applied to the network");
        modif.apply(networkToApply, false, Reporter.NO_OP);
        assertEquals(currentTapPos, tapPositionSupplier.get(),
            "Invalid tap position should not be applied to the network");
    }

}
