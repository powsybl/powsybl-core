/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class NominalVoltageNetworkFilter implements NetworkFilter {

    private final double minNominalVoltage;

    private final double maxNominalVoltage;

    public NominalVoltageNetworkFilter(double minNominalVoltage, double maxNominalVoltage) {
        this.minNominalVoltage = Double.max(0, minNominalVoltage);
        this.maxNominalVoltage = Double.max(this.minNominalVoltage, maxNominalVoltage);
    }

    @Override
    public boolean accept(Substation substation) {
        Objects.requireNonNull(substation);
        return substation.getVoltageLevelStream()
                .anyMatch(this::accept);
    }

    @Override
    public boolean accept(VoltageLevel voltageLevel) {
        Objects.requireNonNull(voltageLevel);
        return voltageLevel.getNominalV() >= minNominalVoltage && voltageLevel.getNominalV() <= maxNominalVoltage;
    }

    @Override
    public boolean accept(Line line) {
        return accept((Branch) line);
    }

    @Override
    public boolean accept(TwoWindingsTransformer transformer) {
        return accept((Branch) transformer);
    }

    private boolean accept(Branch<?> branch) {
        Objects.requireNonNull(branch);
        VoltageLevel vl1 = branch.getTerminal1().getVoltageLevel();
        VoltageLevel vl2 = branch.getTerminal2().getVoltageLevel();

        return accept(vl1) && accept(vl2);
    }

    @Override
    public boolean accept(ThreeWindingsTransformer transformer) {
        Objects.requireNonNull(transformer);
        VoltageLevel vl1 = transformer.getLeg1().getTerminal().getVoltageLevel();
        VoltageLevel vl2 = transformer.getLeg2().getTerminal().getVoltageLevel();
        VoltageLevel vl3 = transformer.getLeg3().getTerminal().getVoltageLevel();

        return accept(vl1) && accept(vl2) && accept(vl3);
    }

    @Override
    public boolean accept(HvdcLine hvdcLine) {
        Objects.requireNonNull(hvdcLine);
        VoltageLevel vl1 = hvdcLine.getConverterStation1().getTerminal().getVoltageLevel();
        VoltageLevel vl2 = hvdcLine.getConverterStation2().getTerminal().getVoltageLevel();

        return accept(vl1) && accept(vl2);
    }
}
