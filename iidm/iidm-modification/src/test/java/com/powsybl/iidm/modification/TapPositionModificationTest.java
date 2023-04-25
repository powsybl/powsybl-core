/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas PIERRE <nicolas.pierre at artelys.com>
 */
public class TapPositionModificationTest {
    private Network network;
    private TwoWindingsTransformer transformer;

    @BeforeEach
    public void setUp() {
        network = FourSubstationsNodeBreakerFactory.create();
        transformer = network.getTwoWindingsTransformerStream().findAny().orElseThrow();
        assertTrue(transformer.hasPhaseTapChanger());
        assertTrue(transformer.hasRatioTapChanger());
        transformer.getPhaseTapChanger().setTapPosition(transformer.getPhaseTapChanger().getHighTapPosition());
        transformer.getRatioTapChanger().setTapPosition(transformer.getRatioTapChanger().getHighTapPosition());
    }

    @Test
    public void testArgumentCoherence() {
        // Good
        new TapPositionModification(transformer.getId(),
                TapPositionModification.TransformerElement.TWO_WINDING_TRANSFORMER,
                TapPositionModification.TapType.PHASE, 0, OptionalInt.empty());
        // Log warning, but good, Leg value is ignored
        new TapPositionModification(transformer.getId(),
                TapPositionModification.TransformerElement.TWO_WINDING_TRANSFORMER,
                TapPositionModification.TapType.PHASE, 0, OptionalInt.of(10));
        // good
        new TapPositionModification(transformer.getId(),
                TapPositionModification.TransformerElement.THREE_WINDING_TRANSFORMER,
                TapPositionModification.TapType.PHASE, 0, OptionalInt.of(1));
        // bad should throw
        assertThrows(PowsyblException.class, () -> new TapPositionModification(transformer.getId(),
                        TapPositionModification.TransformerElement.THREE_WINDING_TRANSFORMER,
                        TapPositionModification.TapType.PHASE, 0, OptionalInt.empty()),
                "Constructor should throw on three winding without specifying the leg.");
        // bad should throw
        assertThrows(PowsyblException.class, () -> new TapPositionModification(transformer.getId(),
                        TapPositionModification.TransformerElement.THREE_WINDING_TRANSFORMER,
                        TapPositionModification.TapType.PHASE, 0, OptionalInt.of(3)),
                "Constructor should throw on three winding with a wrong leg number.");
    }

    @Test
    public void testUnknownId() {
        TapPositionModification modif = new TapPositionModification("UNKNOWN_ID",
                TapPositionModification.TransformerElement.TWO_WINDING_TRANSFORMER,
                TapPositionModification.TapType.PHASE, 5, OptionalInt.empty());
        assertThrows(PowsyblException.class, () -> modif.apply(network, true, Reporter.NO_OP));
    }

    @Test
    public void testTwoWindingsModif() {
        // These assert are assumptions for the rest of the test.
        assertTrue(transformer.getPhaseTapChanger().getLowTapPosition() < transformer.getPhaseTapChanger()
                                                                                     .getHighTapPosition());
        assertTrue(transformer.getRatioTapChanger().getLowTapPosition() < transformer.getRatioTapChanger()
                                                                                     .getHighTapPosition());
        testValidTapPos(TapPositionModification.TapType.PHASE,
                transformer.getPhaseTapChanger().getHighTapPosition() - 1);
        testValidTapPos(TapPositionModification.TapType.RATIO,
                transformer.getRatioTapChanger().getHighTapPosition() - 1);

        int actualRtcPos = transformer.getRatioTapChanger().getTapPosition();
        int actualPtcPos = transformer.getPhaseTapChanger().getTapPosition();
        testInvalidTapPosition(actualPtcPos, TapPositionModification.TapType.PHASE,
                TapPositionModification.TransformerElement.TWO_WINDING_TRANSFORMER,
                transformer.getPhaseTapChanger().getHighTapPosition() + 1);
        testInvalidTapPosition(actualPtcPos, TapPositionModification.TapType.PHASE,
                TapPositionModification.TransformerElement.TWO_WINDING_TRANSFORMER,
                transformer.getPhaseTapChanger().getLowTapPosition() - 1);
        testInvalidTapPosition(actualRtcPos, TapPositionModification.TapType.RATIO,
                TapPositionModification.TransformerElement.TWO_WINDING_TRANSFORMER,
                transformer.getRatioTapChanger().getHighTapPosition() + 1);
        testInvalidTapPosition(actualRtcPos, TapPositionModification.TapType.RATIO,
                TapPositionModification.TransformerElement.TWO_WINDING_TRANSFORMER,
                transformer.getRatioTapChanger().getLowTapPosition() - 1);
    }

    private void testValidTapPos(final TapPositionModification.TapType type, final int tapPos) {
        TapPositionModification modif = new TapPositionModification(transformer.getId(),
                TapPositionModification.TransformerElement.TWO_WINDING_TRANSFORMER, type, tapPos, OptionalInt.empty());
        modif.apply(network);
        assertEquals(tapPos, getTapPositionSupplier(type).get(), "Tap Modification did not change the network");
    }

    private void testInvalidTapPosition(int currentTapPos, final TapPositionModification.TapType type,
                                        TapPositionModification.TransformerElement element, int invalidTapPos) {
        Supplier<Integer> tapPositionSupplier = getTapPositionSupplier(type);
        TapPositionModification modif = new TapPositionModification(transformer.getId(), element, type, invalidTapPos,
                OptionalInt.empty());
        assertThrows(PowsyblException.class, () -> modif.apply(network, true, Reporter.NO_OP));
        assertEquals(currentTapPos, tapPositionSupplier.get(),
                "Invalid tap position should not be applied to the network");
        modif.apply(network, false, Reporter.NO_OP);
        assertEquals(currentTapPos, tapPositionSupplier.get(),
                "Invalid tap position should not be applied to the network");
    }

    private Supplier<Integer> getTapPositionSupplier(TapPositionModification.TapType type) {
        switch (type) {
            case PHASE:
                return transformer.getPhaseTapChanger()::getTapPosition;
            case RATIO:
                return transformer.getRatioTapChanger()::getTapPosition;
        }
        fail();
        return null;
    }

}
