/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;

import java.util.Objects;

/**
 *  An extension to describe the operating status of an equipment. This is typically used in addition to an equipment
 *  disconnection (so using topological changes), to specify the reason of the disconnection: for instance a planned
 *  outage.
 * `Identifiable` is needed as generic type because we also have to support HVDC lines which are not `Connectable`
 * @author Nicolas Noir {@literal <nicolas.noir at rte-france.com>}
 */
public interface OperatingStatus<I extends Identifiable<I>> extends Extension<I> {

    String NAME = "operatingStatus";

    enum Status {
        IN_OPERATION,
        PLANNED_OUTAGE,
        FORCED_OUTAGE
    }

    static boolean isAllowedIdentifiable(Identifiable<?> identifiable) {
        Objects.requireNonNull(identifiable);
        return switch (identifiable.getType()) {
            case LINE, TWO_WINDINGS_TRANSFORMER, THREE_WINDINGS_TRANSFORMER, DANGLING_LINE, HVDC_LINE, TIE_LINE -> true;
            default -> false;
        };
    }

    @Override
    default String getName() {
        return NAME;
    }

    Status getStatus();

    OperatingStatus<I> setStatus(Status status);
}
