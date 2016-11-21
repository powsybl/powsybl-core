/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network;

/**
 * HVDC line builder and adder.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public interface HvdcLineAdder extends IdentifiableAdder<HvdcLineAdder> {

    HvdcLineAdder setR(float r);

    HvdcLineAdder setConvertersMode(HvdcLine.ConvertersMode convertersMode);

    HvdcLineAdder setNominalV(float nominalV);

    HvdcLineAdder setActivePowerSetPoint(float activePowerSetPoint);

    HvdcLineAdder setMaxP(float maxP);

    HvdcLineAdder setConverterStationId1(String converterStationId1);

    HvdcLineAdder setConverterStationId2(String converterStationId2);

    HvdcLine add();
}
