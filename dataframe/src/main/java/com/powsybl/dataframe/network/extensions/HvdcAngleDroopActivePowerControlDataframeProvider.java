/**
 * Copyright (c) 2021-2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.dataframe.network.ExtensionInformation;
import com.powsybl.dataframe.network.NetworkDataframeMapper;
import com.powsybl.dataframe.network.NetworkDataframeMapperBuilder;
import com.powsybl.dataframe.network.adders.NetworkElementAdder;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@soft.it>
 */
@AutoService(NetworkExtensionDataframeProvider.class)
public class HvdcAngleDroopActivePowerControlDataframeProvider extends AbstractSingleDataframeNetworkExtension {

    @Override
    public String getExtensionName() {
        return HvdcAngleDroopActivePowerControl.NAME;
    }

    @Override
    public ExtensionInformation getExtensionInformation() {
        return new ExtensionInformation(HvdcAngleDroopActivePowerControl.NAME,
            "Active power control mode based on an offset in MW and a droop in MW/degree",
            "index : id (str), droop (float), p0 (float), enabled (bool)");
    }

    private Stream<HvdcAngleDroopActivePowerControl> itemsStream(Network network) {
        return network.getHvdcLineStream()
            .map(g -> (HvdcAngleDroopActivePowerControl) g.getExtension(HvdcAngleDroopActivePowerControl.class))
            .filter(Objects::nonNull);
    }

    private HvdcAngleDroopActivePowerControl getOrThrow(Network network, String id) {
        HvdcLine hl = network.getHvdcLine(id);
        if (hl == null) {
            throw new PowsyblException("HvdcLine '" + id + "' not found");
        }
        HvdcAngleDroopActivePowerControl hadapc = hl.getExtension(HvdcAngleDroopActivePowerControl.class);
        if (hadapc == null) {
            throw new PowsyblException("HvdcLine '" + id + "' has no HvdcAngleDroopActivePowerControl extension");
        }
        return hadapc;
    }

    @Override
    public NetworkDataframeMapper createMapper() {
        return NetworkDataframeMapperBuilder.ofStream(this::itemsStream, this::getOrThrow)
            .stringsIndex("id", ext -> ext.getExtendable().getId())
            .doubles("droop", HvdcAngleDroopActivePowerControl::getDroop, (c, d) -> c.setDroop((float) d))
            .doubles("p0", HvdcAngleDroopActivePowerControl::getP0, (c, d) -> c.setP0((float) d))
            .booleans("enabled", HvdcAngleDroopActivePowerControl::isEnabled,
                HvdcAngleDroopActivePowerControl::setEnabled)
            .build();
    }

    @Override
    public void removeExtensions(Network network, List<String> ids) {
        ids.stream().filter(Objects::nonNull)
            .map(network::getHvdcLine)
            .filter(Objects::nonNull)
            .forEach(g -> g.removeExtension(HvdcAngleDroopActivePowerControl.class));
    }

    @Override
    public NetworkElementAdder createAdder() {
        return new HvdcAngleDroopActivePowerControlDataframeAdder();
    }
}
