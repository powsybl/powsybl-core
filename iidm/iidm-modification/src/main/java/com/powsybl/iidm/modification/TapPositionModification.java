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

import java.util.Objects;
import java.util.OptionalInt;

/**
 * TODO
 *
 * @author Nicolas PIERRE <nicolas.pierre at artelys.com>
 */
public class TapPositionModification extends AbstractNetworkModification {

    private final String tapId;
    private final TransformerElement element;
    private final TransformerType type;
    private final int tapPosition;
    /**
     * Defines the leg on which to apply the change for three winding tranformers.
     *
     * @implNote Must NOT be empty if element == TransformerElement.THREE_WINDING_TRANSFORMER
     */
    private final OptionalInt leg;

    public TapPositionModification(String tapId, TransformerElement element, TransformerType type, int tapPosition,
                                   OptionalInt leg) {
        this.tapId = Objects.requireNonNull(tapId);
        this.element = Objects.requireNonNull(element);
        this.type = Objects.requireNonNull(type);
        this.tapPosition = tapPosition;
        this.leg = leg;
        if (element == TransformerElement.THREE_WINDING_TRANSFORMER && leg.isEmpty()) {
            throw new PowsyblException("TapPositionModification needs a side for Three winding transformers");
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
                transformer = network.getTwoWindingsTransformer(tapId);
                break;
            case THREE_WINDING_TRANSFORMER:
                transformer = getLeg(network.getThreeWindingsTransformer(tapId));
                break;
        }

        if (transformer == null) {
            logOrThrow(throwException, "Transformer '" + tapId + "' not found");
            return;
        }
        if (!transformer.hasPhaseTapChanger()) {
            logOrThrow(throwException, "Transformer '" + tapId + "' does not have a PhaseTapChanger");
            return;
        }
        transformer.getPhaseTapChanger().setTapPosition(tapPosition);
    }

    private void applyRtc(Network network, boolean throwException) {
        RatioTapChangerHolder transformer = null;
        switch (element) {
            case TWO_WINDING_TRANSFORMER:
                transformer = network.getTwoWindingsTransformer(tapId);
                break;
            case THREE_WINDING_TRANSFORMER:
                transformer = getLeg(network.getThreeWindingsTransformer(tapId));
                break;
        }

        if (transformer == null) {
            logOrThrow(throwException, "Transformer '" + tapId + "' not found");
            return;
        }
        if (!transformer.hasRatioTapChanger()) {
            logOrThrow(throwException, "Transformer '" + tapId + "' does not have a RatioTapChanger");
            return;
        }
        transformer.getRatioTapChanger().setTapPosition(tapPosition);
    }

    private ThreeWindingsTransformer.Leg getLeg(ThreeWindingsTransformer threeWindingsTransformer) {
        if (threeWindingsTransformer == null) {
            return null;
        }
        // Constructor ensures that on ThreeWinding, side member is not empty.
        return threeWindingsTransformer.getLegs().get(leg.getAsInt());
    }

    public String getTapId() {
        return tapId;
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

    public TransformerType getType() {
        return type;
    }

    public enum TransformerElement {
        TWO_WINDING_TRANSFORMER, THREE_WINDING_TRANSFORMER
    }

    public enum TransformerType {
        PHASE, RATIO
    }

}

