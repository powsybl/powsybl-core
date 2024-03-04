/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction.criteria.translation;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.criteria.translation.DefaultNetworkElementAdapter;

import java.util.Optional;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class DefaultNetworkElementWithLimitsAdapter extends DefaultNetworkElementAdapter implements LimitsHolder<LoadingLimits> {
    public DefaultNetworkElementWithLimitsAdapter(Identifiable<?> identifiable) {
        super(identifiable);
    }

    @Override
    public Optional<LoadingLimits> getLimits(LimitType limitType, ThreeSides side) {
        return switch (getIdentifiable().getType()) {
            case LINE -> (Optional<LoadingLimits>) ((Line) getIdentifiable()).getLimits(limitType, side.toTwoSides());
            case TIE_LINE -> (Optional<LoadingLimits>) ((TieLine) getIdentifiable()).getLimits(limitType, side.toTwoSides());
            case TWO_WINDINGS_TRANSFORMER -> (Optional<LoadingLimits>) ((TwoWindingsTransformer) getIdentifiable()).getLimits(limitType, side.toTwoSides());
            case THREE_WINDINGS_TRANSFORMER -> (Optional<LoadingLimits>) ((ThreeWindingsTransformer) getIdentifiable()).getLeg(side).getLimits(limitType);
            default -> Optional.empty();
        };
    }
}
