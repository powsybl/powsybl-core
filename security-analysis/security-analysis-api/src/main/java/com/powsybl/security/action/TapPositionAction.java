/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.action;

import java.util.Objects;

/**
 * An action changing the tap position of a phase-shifting transformer.
 *
 * @author Hadrien Godard <hadrien.godard@artelys.com>
 */
public class TapPositionAction extends AbstractAction {

    public static final String NAME = "TAP_POSITION";

    private final String transformerId;
    private final int newTapPosition;

    public TapPositionAction(String id, String transformerId, int newTapPosition) {
        super(id);
        this.transformerId = Objects.requireNonNull(transformerId);
        this.newTapPosition = newTapPosition;
    }

    @Override
    public String getType() {
        return NAME;
    }

    public String getTransformerId() {
        return transformerId;
    }

    public int getNewTapPosition() {
        return newTapPosition;
    }
}
