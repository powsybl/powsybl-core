/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.action;

import com.powsybl.iidm.network.ThreeWindingsTransformer;

import java.util.Objects;
import java.util.Optional;

/**
 * An action modifying the regulation of a two or three windings transformer
 *
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public abstract class AbstractTapChangerRegulationAction extends AbstractAction {

    private final String transformerId;
    private final ThreeWindingsTransformer.Side side;
    private final boolean regulating;

    protected AbstractTapChangerRegulationAction(String id, String transformerId, ThreeWindingsTransformer.Side side, boolean regulating) {
        super(id);
        this.transformerId = Objects.requireNonNull(transformerId);
        this.side = side;
        this.regulating = regulating;
    }

    public String getTransformerId() {
        return transformerId;
    }

    public Optional<ThreeWindingsTransformer.Side> getSide() {
        return Optional.ofNullable(side);
    }

    public boolean isRegulating() {
        return regulating;
    }
}
