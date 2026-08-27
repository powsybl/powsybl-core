/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.ucte.converter;

import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Switch;
import com.powsybl.ucte.network.UcteElementId;
import com.powsybl.ucte.network.UcteNodeCode;

import java.util.Objects;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public class UcteExporterContext {

    private final NamingStrategy namingStrategy;

    private final NamingStrategy.Context namingStrategyContext;

    private final boolean combinePhaseAngleRegulation;

    public UcteExporterContext(NamingStrategy namingStrategy, NamingStrategy.Context namingStrategyContext, boolean combinePhaseAngleRegulation) {
        this.namingStrategy = Objects.requireNonNull(namingStrategy);
        this.namingStrategyContext = Objects.requireNonNull(namingStrategyContext);
        this.combinePhaseAngleRegulation = Objects.requireNonNull(combinePhaseAngleRegulation);
    }

    public UcteNodeCode getUcteNodeCode(String id) {
        return namingStrategy.getUcteNodeCode(namingStrategyContext, id);
    }

    public UcteNodeCode getUcteNodeCode(Bus bus) {
        return namingStrategy.getUcteNodeCode(namingStrategyContext, bus);
    }

    public UcteNodeCode getUcteNodeCode(BoundaryLine boundaryLine) {
        return namingStrategy.getUcteNodeCode(namingStrategyContext, boundaryLine);
    }

    public UcteElementId getUcteElementId(String id) {
        return namingStrategy.getUcteElementId(namingStrategyContext, id);
    }

    public UcteElementId getUcteElementId(Switch sw) {
        return namingStrategy.getUcteElementId(namingStrategyContext, sw);
    }

    public UcteElementId getUcteElementId(Branch branch) {
        return namingStrategy.getUcteElementId(namingStrategyContext, branch);
    }

    public UcteElementId getUcteElementId(BoundaryLine boundaryLine) {
        return namingStrategy.getUcteElementId(namingStrategyContext, boundaryLine);
    }

    public boolean withCombinePhaseAngleRegulation() {
        return combinePhaseAngleRegulation;
    }
}
