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
    private final Boolean delta; // true if relative mode chosen, false if absolute mode.
    private int value;
    private Optional<ThreeWindingsTransformer.Side> side;

    public PhaseTapChangerTapPositionAction(String id, String transformerId, Boolean delta, int value, Optional<ThreeWindingsTransformer.Side> side) {
        super(id);
        this.transformerId = Objects.requireNonNull(transformerId);
        this.delta = delta;
        this.value = value;
        this.side = side;
    }

    public PhaseTapChangerTapPositionAction(String id, String transformerId, Boolean delta, int value) {
        this(id, transformerId, delta, value, Optional.empty());
    }

    @Override
    public String getType() {
        return NAME;
    }

    public String getTransformerId() {
        return transformerId;
    }

    public Boolean isInRelativeMode() {
        return delta;
    }

    public int getValue() {
        return value;
    }

    public Optional<ThreeWindingsTransformer.Side> getSide() {
        return side;
    }
}
