/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChangerHolder;
import com.powsybl.iidm.network.RatioTapChangerHolder;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.OptionalInt;

/**
 * Allow to modify a tap phase or ratio on two or three winding transformers.
 * For three windings you must specify the Leg.
 *
 * @author Nicolas PIERRE <nicolas.pierre at artelys.com>
 */
public class TapPositionModification extends AbstractNetworkModification {

    private static final Logger LOGGER = LoggerFactory.getLogger(VscConverterStationModification.class);

    private final String transfoId;
    private final TransformerElement element;
    private final TapType type;
    private final int tapPosition;
    /**
     * Defines the leg on which to apply the change for three winding tranformers. 0, 1 or 2
     *
     * @implNote Must NOT be empty if element == TransformerElement.THREE_WINDING_TRANSFORMER
     */
    private final OptionalInt leg;

    /**
     * @param tapPosition the new tag position
     * @param leg         defines on which leg of the three winding transformer the modification will be done. Ignored on two windings.
     */
    public TapPositionModification(String transfoId, TransformerElement element, TapType type, int tapPosition,
                                   OptionalInt leg) {
        this.transfoId = Objects.requireNonNull(transfoId);
        this.element = Objects.requireNonNull(element);
        this.type = Objects.requireNonNull(type);
        this.tapPosition = tapPosition;
        this.leg = Objects.requireNonNullElse(leg, OptionalInt.empty());
        if (element == TransformerElement.THREE_WINDING_TRANSFORMER && leg.isEmpty()) {
            throw new PowsyblException("TapPositionModification needs a leg for three winding transformers");
        } else if (element == TransformerElement.THREE_WINDING_TRANSFORMER && (leg.getAsInt() < 0 || leg.getAsInt() > 2)) {
            throw new PowsyblException("Leg number is invalid, must be  0, 1 or 2");
        }
        if (element == TransformerElement.TWO_WINDING_TRANSFORMER && leg.isPresent()) {
            LOGGER.warn("TapPositionModification does not need a side for two winding transformers");
        }

    }

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager,
                      Reporter reporter) {
        switch (type) {
            case RATIO:
                applyRtc(network, throwException);
                break;
            case PHASE:
                applyPtc(network, throwException);
                break;
        }
    }

    private void applyPtc(Network network, boolean throwException) {
        PhaseTapChangerHolder transformer = null;
        switch (element) {
            case TWO_WINDING_TRANSFORMER:
                transformer = network.getTwoWindingsTransformer(transfoId);
                break;
            case THREE_WINDING_TRANSFORMER:
                transformer = getLeg(network.getThreeWindingsTransformer(transfoId));
                break;
        }

        if (transformer == null) {
            logOrThrow(throwException, "Transformer '" + transfoId + "' not found");
            return;
        }
        if (!transformer.hasPhaseTapChanger()) {
            logOrThrow(throwException, "Transformer '" + transfoId + "' does not have a PhaseTapChanger");
            return;
        }
        transformer.getPhaseTapChanger().setTapPosition(tapPosition);
    }

    private void applyRtc(Network network, boolean throwException) {
        RatioTapChangerHolder transformer = null;
        switch (element) {
            case TWO_WINDING_TRANSFORMER:
                transformer = network.getTwoWindingsTransformer(transfoId);
                break;
            case THREE_WINDING_TRANSFORMER:
                transformer = getLeg(network.getThreeWindingsTransformer(transfoId));
                break;
        }

        if (transformer == null) {
            logOrThrow(throwException, "Transformer '" + transfoId + "' not found");
            return;
        }
        if (!transformer.hasRatioTapChanger()) {
            logOrThrow(throwException, "Transformer '" + transfoId + "' does not have a RatioTapChanger");
            return;
        }
        transformer.getRatioTapChanger().setTapPosition(tapPosition);
    }

    private ThreeWindingsTransformer.Leg getLeg(ThreeWindingsTransformer threeWindingsTransformer) {
        if (threeWindingsTransformer == null) {
            return null;
        }
        // Constructor ensures that, on Three Winding Transformers, leg field is not empty.
        return threeWindingsTransformer.getLegs().get(leg.getAsInt());
    }

    public String getTransfoId() {
        return transfoId;
    }

    public int getTapPosition() {
        return tapPosition;
    }

    public OptionalInt getLeg() {
        return leg;
    }

    public TransformerElement getElement() {
        return element;
    }

    public TapType getType() {
        return type;
    }

    public enum TransformerElement {
        TWO_WINDING_TRANSFORMER, THREE_WINDING_TRANSFORMER
    }

    public enum TapType {
        PHASE, RATIO
    }

}

