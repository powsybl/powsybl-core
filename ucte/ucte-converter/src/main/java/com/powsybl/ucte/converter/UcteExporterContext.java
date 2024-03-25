/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.ucte.converter;

import java.util.Objects;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public class UcteExporterContext {

    private final NamingStrategy namingStrategy;

    private final boolean combinePhaseAngleRegulation;

    public UcteExporterContext(NamingStrategy namingStrategy, boolean combinePhaseAngleRegulation) {
        this.namingStrategy = Objects.requireNonNull(namingStrategy);
        this.combinePhaseAngleRegulation = Objects.requireNonNull(combinePhaseAngleRegulation);
    }

    public NamingStrategy getNamingStrategy() {
        return namingStrategy;
    }

    public boolean withCombinePhaseAngleRegulation() {
        return combinePhaseAngleRegulation;
    }
}
