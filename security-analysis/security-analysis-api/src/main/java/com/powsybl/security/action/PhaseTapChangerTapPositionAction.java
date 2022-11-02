/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.action;

import com.powsybl.iidm.network.ThreeWindingsTransformer;

import java.util.Objects;
import java.util.Optional;

/**
 * An action changing the tap position of a phase-shifting transformer.
 *
 * @author Hadrien Godard <hadrien.godard@artelys.com>
 */
public class PhaseTapChangerTapPositionAction extends AbstractAction {

    public static final String NAME = "PHASE_TAP_CHANGER_TAP_POSITION";

    private final String transformerId;
    private final int value;
    private final boolean relativeValue; // true if relative value chosen, false if absolute value
    private final ThreeWindingsTransformer.Side side;

    public PhaseTapChangerTapPositionAction(String id, String transformerId, boolean relativeValue, int value, ThreeWindingsTransformer.Side side) {
        super(id);
        this.transformerId = Objects.requireNonNull(transformerId);
        this.relativeValue = relativeValue;
        this.value = value;
        this.side = side;
    }

    public PhaseTapChangerTapPositionAction(String id, String transformerId, boolean relativeValue, int value) {
        this(id, transformerId, relativeValue, value, null);
    }

    @Override
    public String getType() {
        return NAME;
    }

    public String getTransformerId() {
        return transformerId;
    }

    public int getValue() {
        return value;
    }

    public boolean isRelativeValue() {
        return relativeValue;
    }

    public Optional<ThreeWindingsTransformer.Side> getSide() {
        return Optional.ofNullable(side);
    }
}
