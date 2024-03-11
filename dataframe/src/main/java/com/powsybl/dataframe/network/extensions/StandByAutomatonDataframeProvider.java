/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dataframe.network.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.dataframe.network.ExtensionInformation;
import com.powsybl.dataframe.network.NetworkDataframeMapper;
import com.powsybl.dataframe.network.NetworkDataframeMapperBuilder;
import com.powsybl.dataframe.network.adders.NetworkElementAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
@AutoService(NetworkExtensionDataframeProvider.class)
public class StandByAutomatonDataframeProvider extends AbstractSingleDataframeNetworkExtension {

    @Override
    public String getExtensionName() {
        return "standbyAutomaton";
    }

    @Override
    public ExtensionInformation getExtensionInformation() {
        return new ExtensionInformation("standbyAutomaton", "allow to manage standby mode for static var compensator",
            "index : id (str), standby (boolean), b0 (double), low_voltage_threshold (double), low_voltage_setpoint (double)," +
                " high_voltage_threshold (double), high_voltage_setpoint (double)");
    }

    private Stream<StandbyAutomaton> itemsStream(Network network) {
        return network.getStaticVarCompensatorStream()
            .map(svc -> (StandbyAutomaton) svc.getExtension(StandbyAutomaton.class))
            .filter(Objects::nonNull);
    }

    @Override
    public NetworkDataframeMapper createMapper() {
        return NetworkDataframeMapperBuilder.ofStream(this::itemsStream, this::getOrThrow)
            .stringsIndex("id", ext -> ext.getExtendable().getId())
            .booleans("standby", StandbyAutomaton::isStandby, StandbyAutomaton::setStandby)
            .doubles("b0", StandbyAutomaton::getB0, StandbyAutomaton::setB0)
            .doubles("low_voltage_threshold", StandbyAutomaton::getLowVoltageThreshold,
                StandbyAutomaton::setLowVoltageThreshold)
            .doubles("low_voltage_setpoint", StandbyAutomaton::getLowVoltageSetpoint,
                StandbyAutomaton::setLowVoltageSetpoint)
            .doubles("high_voltage_threshold", StandbyAutomaton::getHighVoltageThreshold,
                StandbyAutomaton::setHighVoltageThreshold)
            .doubles("high_voltage_setpoint", StandbyAutomaton::getHighVoltageSetpoint,
                StandbyAutomaton::setHighVoltageSetpoint)
            .build();
    }

    @Override
    public void removeExtensions(Network network, List<String> ids) {
        ids.stream().map(network::getStaticVarCompensator)
            .filter(Objects::nonNull)
            .forEach(staticVarCompensator -> staticVarCompensator.removeExtension(StandbyAutomaton.class));
    }

    @Override
    public NetworkElementAdder createAdder() {
        return new StandByAutomatonDataframeAdder();
    }

    private StandbyAutomaton getOrThrow(Network network, String id) {
        StaticVarCompensator staticVarCompensator = network.getStaticVarCompensator(id);
        if (staticVarCompensator == null) {
            throw new PowsyblException("StaticVarCompensator '" + id + "' not found");
        }
        StandbyAutomaton standbyAutomaton = staticVarCompensator.getExtension(StandbyAutomaton.class);
        if (standbyAutomaton == null) {
            throw new PowsyblException("StaticVarCompensator '" + id + "' has no StandbyAutomaton extension");
        }
        return standbyAutomaton;
    }
}
