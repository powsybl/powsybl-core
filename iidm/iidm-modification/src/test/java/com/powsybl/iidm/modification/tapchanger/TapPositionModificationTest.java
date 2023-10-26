/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.tapchanger;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas PIERRE {@literal <nicolas.pierre at artelys.com>}
 */
class TapPositionModificationTest {
    private Network network;
    private Network threeWindingNetwork;
    private TwoWindingsTransformer twoWindingsTransformer;
    private ThreeWindingsTransformer threeWindingTransformer;
    private static final ThreeWindingsTransformer.Side LEG_NUM = ThreeWindingsTransformer.Side.TWO; // Using leg number 2
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
        threeWindingTransformerLeg = threeWindingTransformer.getLeg(LEG_NUM);
    }

    @Test
    void testArgumentCoherence() {
        // Good
        String id = twoWindingsTransformer.getId();
        ThreeWindingsTransformer.Side leg = ThreeWindingsTransformer.Side.TWO;
        assertDoesNotThrow(() -> new PhaseTapPositionModification(id, 0));
        // Log warning, but good, Leg value is ignored
        assertDoesNotThrow(() -> new PhaseTapPositionModification(id, 0));
        // good
        assertDoesNotThrow(
            () -> new PhaseTapPositionModification(id, 0, leg));
        assertDoesNotThrow(() -> new RatioTapPositionModification(id, 0));
        // Log warning, but good, Leg value is ignored
        assertDoesNotThrow(() -> new RatioTapPositionModification(id, 0));
        // good
        assertDoesNotThrow(
            () -> new RatioTapPositionModification(id, 0, leg));
    }

    @Test
    void testGetLeg() {
        RatioTapPositionModification modifRTC = new RatioTapPositionModification("ID", 1, LEG_NUM);
        assertNull(modifRTC.getLeg(null, leg -> true, true));
        // defined leg in constructor
        assertEquals(threeWindingTransformerLeg, modifRTC.getLeg(threeWindingTransformer, leg -> true, true));
        modifRTC = new RatioTapPositionModification("ID", 1);
        // no match
        assertNull(modifRTC.getLeg(threeWindingTransformer, leg -> false, false));
        // mutliple match
        assertNull(modifRTC.getLeg(threeWindingTransformer, RatioTapChangerHolder::hasRatioTapChanger, false));
        // single match
        assertEquals(threeWindingTransformerLeg,
            modifRTC.getLeg(threeWindingTransformer, leg -> leg.equals(threeWindingTransformerLeg), false));
    }

    @Test
    void testUnknownId() {
        NetworkModification modif = new PhaseTapPositionModification("UNKNOWN_ID", 5);
        assertThrows(PowsyblException.class, () -> modif.apply(network, true, Reporter.NO_OP));
        assertDoesNotThrow(() -> modif.apply(network, false, Reporter.NO_OP),
            "An invalid ID should not throw if throwException is false.");
        NetworkModification modif2 = new RatioTapPositionModification("UNKNOWN_ID", 5);
        assertThrows(PowsyblException.class, () -> modif2.apply(network, true, Reporter.NO_OP));
        assertDoesNotThrow(() -> modif2.apply(network, false, Reporter.NO_OP),
            "An invalid ID should not throw if throwException is false.");
    }

    @Test
    void testTwoWindingsModif() {
        testTapTransformer(twoWindingsTransformer.getPhaseTapChanger(), IdentifiableType.TWO_WINDINGS_TRANSFORMER,
            TapType.PHASE, twoWindingsTransformer.getId());
        testTapTransformer(twoWindingsTransformer.getRatioTapChanger(), IdentifiableType.TWO_WINDINGS_TRANSFORMER,
            TapType.RATIO, twoWindingsTransformer.getId());
    }

    @Test
    void testMissingTap() {
        ThreeWindingsTransformer.Leg leg = threeWindingTransformer.getLeg1();
        // Leg1 does not have a Rtc/Ptc. Trying to modify them must throw/log.
        // Ratio
        assertFalse(leg.hasPhaseTapChanger(), "Test assumptions are wrong.");
        NetworkModification modifRtc = new RatioTapPositionModification(threeWindingNetwork.getId(), 1,
            ThreeWindingsTransformer.Side.ONE);
        assertThrows(PowsyblException.class, () -> modifRtc.apply(threeWindingNetwork, true, Reporter.NO_OP),
            "Modifying a Ratio tap that is not present should throw.");
        // Phase
        assertFalse(leg.hasRatioTapChanger(), "Test assumptions are wrong.");
        NetworkModification modifPtc = new PhaseTapPositionModification(threeWindingNetwork.getId(), 1,
            ThreeWindingsTransformer.Side.ONE);
        assertThrows(PowsyblException.class, () -> modifPtc.apply(threeWindingNetwork, true, Reporter.NO_OP),
            "Modifying a Phasetap that is not present should throw.");
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
        testTapTransformer(threeWindingTransformerLeg.getPhaseTapChanger(), IdentifiableType.THREE_WINDINGS_TRANSFORMER,
            TapType.PHASE, threeWindingTransformer.getId());
        testTapTransformer(threeWindingTransformerLeg.getRatioTapChanger(), IdentifiableType.THREE_WINDINGS_TRANSFORMER,
            TapType.RATIO, threeWindingTransformer.getId());
    }

    private void testTapTransformer(TapChanger<?, ?> tapChanger, final IdentifiableType transformerElement,
                                    final TapType tapType, final String transformerId) {
        Supplier<Integer> tapPositionSupplier = tapChanger::getTapPosition;
        assertTrue(tapChanger.getLowTapPosition() < tapChanger.getHighTapPosition());

        testValidTapPos(tapType, tapChanger.getHighTapPosition() - 1, transformerId, transformerElement,
            tapPositionSupplier);
        int actualPtcPos = tapChanger.getTapPosition();
        testInvalidTapPosition(actualPtcPos, tapType, transformerElement, tapChanger.getHighTapPosition() + 1,
            transformerId, tapPositionSupplier);
        testInvalidTapPosition(actualPtcPos, tapType, transformerElement, tapChanger.getLowTapPosition() - 1,
            transformerId, tapPositionSupplier);
    }

    private void testValidTapPos(final TapType type, final int tapPos, final String transformerId,
                                 final IdentifiableType element, final Supplier<Integer> tapPositionSupplier) {
        ThreeWindingsTransformer.Side leg;
        Network networkToApply;
        NetworkModification modif;
        if (IdentifiableType.TWO_WINDINGS_TRANSFORMER.equals(element)) {
            leg = null;
            networkToApply = network;
        } else {
            leg = LEG_NUM;
            networkToApply = threeWindingNetwork;
        }
        modif = getNetworkModification(type, tapPos, transformerId, leg);
        modif.apply(networkToApply);
        assertEquals(tapPos, tapPositionSupplier.get(), "Tap Modification did not change the network");
    }

    private static NetworkModification getNetworkModification(TapType type, int tapPos, String transformerId,
                                                              ThreeWindingsTransformer.Side leg) {
        NetworkModification modif;
        if (TapType.RATIO.equals(type)) {
            if (leg != null) {
                modif = new RatioTapPositionModification(transformerId, tapPos, leg);
            } else {
                modif = new RatioTapPositionModification(transformerId, tapPos);
            }
        } else { // type ==  TapType.Phase
            if (leg != null) {
                modif = new PhaseTapPositionModification(transformerId, tapPos, leg);
            } else {
                modif = new PhaseTapPositionModification(transformerId, tapPos);
            }
        }
        return modif;
    }

    private void testInvalidTapPosition(int currentTapPos, final TapType type, IdentifiableType element,
                                        int invalidTapPos, final String transformerId,
                                        final Supplier<Integer> tapPositionSupplier) {
        ThreeWindingsTransformer.Side leg;
        Network networkToApply;
        NetworkModification modif;
        if (IdentifiableType.TWO_WINDINGS_TRANSFORMER.equals(element)) {
            leg = null;
            networkToApply = network;
        } else {
            leg = LEG_NUM;
            networkToApply = threeWindingNetwork;
        }
        modif = getNetworkModification(type, invalidTapPos, transformerId, leg);
        assertThrows(PowsyblException.class, () -> modif.apply(networkToApply, true, Reporter.NO_OP));
        assertEquals(currentTapPos, tapPositionSupplier.get(),
            "Invalid tap position should not be applied to the network");
        modif.apply(networkToApply, false, Reporter.NO_OP);
        assertEquals(currentTapPos, tapPositionSupplier.get(),
            "Invalid tap position should not be applied to the network");
    }

    public enum TapType {
        PHASE, RATIO
    }

}
