/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.*;

import java.util.Map;
import java.util.function.Function;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public final class ContingencyElementFactory {

    private ContingencyElementFactory() {

    }

    private static final Map<Class<?>, Function<Identifiable<?>, ContingencyElement>> TYPE_TO_CONSTRUCTOR =
        Map.ofEntries(
            Map.entry(Line.class, id -> new LineContingency(id.getId())),
            Map.entry(BusbarSection.class, id -> new BusbarSectionContingency(id.getId())),
            Map.entry(TwoWindingsTransformer.class, id -> new TwoWindingsTransformerContingency(id.getId())),
            Map.entry(ThreeWindingsTransformer.class, id -> new ThreeWindingsTransformerContingency(id.getId())),
            Map.entry(Generator.class, id -> new GeneratorContingency(id.getId())),
            Map.entry(Switch.class, id -> new SwitchContingency(id.getId())),
            Map.entry(DanglingLine.class, id -> new DanglingLineContingency(id.getId())),
            Map.entry(Load.class, id -> new LoadContingency(id.getId())),
            Map.entry(HvdcLine.class, id -> new HvdcLineContingency(id.getId())),
            Map.entry(ShuntCompensator.class, id -> new ShuntCompensatorContingency(id.getId())),
            Map.entry(StaticVarCompensator.class, id -> new StaticVarCompensatorContingency(id.getId())),
            Map.entry(Battery.class, id -> new BatteryContingency(id.getId())),
            Map.entry(Bus.class, id -> new BusContingency(id.getId())),
            Map.entry(TieLine.class, id -> new TieLineContingency(id.getId())),
            Map.entry(HvdcConverterStation.class, id -> new HvdcLineContingency(((HvdcConverterStation<?>) id).getHvdcLine().getId()))
        );

    public static ContingencyElement create(Identifiable<?> identifiable) {
        return TYPE_TO_CONSTRUCTOR.entrySet().stream()
            .filter(entry -> entry.getKey().isInstance(identifiable))
            .findFirst()
            .map(entry -> entry.getValue().apply(identifiable))
            .orElseThrow(() -> new PowsyblException(identifiable.getId() + " can not be a ContingencyElement"));
    }
}
