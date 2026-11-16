/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter;

import java.io.IOException;

/**
 * This enum maps elements to their reading function in {@link AmplNetworkReader}
 *
 * @author Nicolas Pierre {@literal < nicolas.pierre@artelys.com >}
 */
public enum AmplReadableElement {
    BATTERY(AmplNetworkReader::readBatteries),
    BUS(AmplNetworkReader::readBuses),
    BRANCH(AmplNetworkReader::readBranches),
    GENERATOR(AmplNetworkReader::readGenerators),
    HVDCLINE(AmplNetworkReader::readHvdcLines),
    LCC_CONVERTER_STATION(AmplNetworkReader::readLccConverterStations),
    LOAD(AmplNetworkReader::readLoads),
    PHASE_TAP_CHANGER(AmplNetworkReader::readPhaseTapChangers),
    RATIO_TAP_CHANGER(AmplNetworkReader::readRatioTapChangers),
    SHUNT(AmplNetworkReader::readShunts),
    STATIC_VAR_COMPENSATOR(AmplNetworkReader::readStaticVarcompensator),
    VSC_CONVERTER_STATION(AmplNetworkReader::readVscConverterStations);
    private final AmplElementReader readElementConsumer;

    AmplReadableElement(AmplElementReader readElementConsumer) {
        this.readElementConsumer = readElementConsumer;
    }

    public void readElement(AmplNetworkReader reader) throws IOException {
        this.readElementConsumer.read(reader);
    }

}
