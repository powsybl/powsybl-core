/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * HVDC line builder and adder.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public interface HvdcLineAdder extends IdentifiableAdder<HvdcLineAdder> {

    HvdcLineAdder setR(double r);

    HvdcLineAdder setConvertersMode(HvdcLine.ConvertersMode convertersMode);

    HvdcLineAdder setNominalV(double nominalV);

    HvdcLineAdder setActivePowerSetpoint(double activePowerSetpoint);

    HvdcLineAdder setMaxP(double maxP);

    HvdcLineAdder setConverterStationId1(String converterStationId1);

    HvdcLineAdder setConverterStationId2(String converterStationId2);

    HvdcLine add();
}
