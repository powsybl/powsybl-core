/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class IdentifierNetworkFilter implements NetworkFilter {

    private final Set<String> ids = new HashSet<>();

    public IdentifierNetworkFilter(Collection<String> ids) {
        Objects.requireNonNull(ids);

        this.ids.addAll(ids);
    }

    /**
     * Keep this substation if the IDs list contains the ID of this substation or one of its voltage levels.
     * @param substation The substation to test
     * @return true if the IDs list contains the ID of this substation or one of its voltage levels, false otherwise
     */
    @Override
    public boolean accept(Substation substation) {
        Objects.requireNonNull(substation);
        if (ids.contains(substation.getId())) {
            return true;
        }

        return substation.getVoltageLevelStream()
                .map(VoltageLevel::getId)
                .anyMatch(ids::contains);
    }

    /**
     * Keep this voltage level if the IDs list contains the ID of this voltage level.
     * @param voltageLevel The voltage level to test
     * @return true if the IDs list contains the ID of this voltage level, false otherwise
     */
    @Override
    public boolean accept(VoltageLevel voltageLevel) {
        Objects.requireNonNull(voltageLevel);
        return ids.contains(voltageLevel.getId()) || ids.contains(voltageLevel.getSubstation().getId());
    }

    @Override
    public boolean accept(Line line) {
        Objects.requireNonNull(line);
        return accept(line.getTerminal1().getVoltageLevel()) &&
                accept(line.getTerminal2().getVoltageLevel());
    }

    @Override
    public boolean accept(TwoWindingsTransformer transformer) {
        Objects.requireNonNull(transformer);
        return accept(transformer.getTerminal1().getVoltageLevel()) &&
                accept(transformer.getTerminal2().getVoltageLevel());
    }

    @Override
    public boolean accept(ThreeWindingsTransformer transformer) {
        Objects.requireNonNull(transformer);
        return accept(transformer.getLeg1().getTerminal().getVoltageLevel()) &&
                accept(transformer.getLeg2().getTerminal().getVoltageLevel()) &&
                accept(transformer.getLeg3().getTerminal().getVoltageLevel());
    }

    @Override
    public boolean accept(HvdcLine hvdcLine) {
        Objects.requireNonNull(hvdcLine);
        return accept(hvdcLine.getConverterStation1().getTerminal().getVoltageLevel()) &&
                accept(hvdcLine.getConverterStation2().getTerminal().getVoltageLevel());
    }
}
