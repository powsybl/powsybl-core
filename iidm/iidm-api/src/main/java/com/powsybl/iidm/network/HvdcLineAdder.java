/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * HVDC line builder and adder.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public interface HvdcLineAdder extends IdentifiableAdder<HvdcLine, HvdcLineAdder> {

    /**
     * Set the resistance of the HVDC line in &#937. Since this can be an equivalent model, calculations can lead to the value being negative.
     * Therefore, a negative value is allowed.
     * @param r the value of the resistance of the HVDC line in &#937
     * @return this adder, for chaining
     */
    HvdcLineAdder setR(double r);

    HvdcLineAdder setConvertersMode(HvdcLine.ConvertersMode convertersMode);

    HvdcLineAdder setNominalV(double nominalV);

    HvdcLineAdder setActivePowerSetpoint(double activePowerSetpoint);

    HvdcLineAdder setMaxP(double maxP);

    HvdcLineAdder setConverterStationId1(String converterStationId1);

    HvdcLineAdder setConverterStationId2(String converterStationId2);

    @Override
    HvdcLine add();
}
