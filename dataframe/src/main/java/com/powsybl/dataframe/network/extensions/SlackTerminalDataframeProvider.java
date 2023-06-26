/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.dataframe.network.ExtensionInformation;
import com.powsybl.dataframe.network.NetworkDataframeMapper;
import com.powsybl.dataframe.network.NetworkDataframeMapperBuilder;
import com.powsybl.dataframe.network.adders.NetworkElementAdder;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
@AutoService(NetworkExtensionDataframeProvider.class)
public class SlackTerminalDataframeProvider extends AbstractSingleDataframeNetworkExtension {
    @Override
    public String getExtensionName() {
        return "slackTerminal";
    }

    @Override
    public ExtensionInformation getExtensionInformation() {
        return new ExtensionInformation("slackTerminal",
            "a terminal that determines the slack bus for loadflow analysis",
            "index : voltage_level_id (str), element_id (str), bus_id (str)");
    }

    private Stream<SlackTerminal> itemsStream(Network network) {
        return network.getVoltageLevelStream()
            .map(vl -> (SlackTerminal) vl.getExtension(SlackTerminal.class))
            .filter(Objects::nonNull);
    }

    @Override
    public NetworkDataframeMapper createMapper() {
        return NetworkDataframeMapperBuilder.ofStream(this::itemsStream)
            .stringsIndex("voltage_level_id", st -> st.getTerminal().getVoltageLevel().getId())
            .strings("element_id", st -> st.getTerminal().getConnectable() == null ? "" :
                st.getTerminal().getConnectable().getId())
            .strings("bus_id", st -> st.getTerminal().getBusView().getBus() == null ? "" :
                st.getTerminal().getBusView().getBus().getId())
            .build();
    }

    @Override
    public void removeExtensions(Network network, List<String> ids) {
        ids.stream().map(network::getVoltageLevel)
            .filter(Objects::nonNull)
            .forEach(voltageLevel -> voltageLevel.removeExtension(SlackTerminal.class));
    }

    @Override
    public NetworkElementAdder createAdder() {
        return new SlackTerminalDataframeAdder();
    }
}
