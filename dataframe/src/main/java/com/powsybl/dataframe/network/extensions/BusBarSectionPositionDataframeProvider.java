/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
@AutoService(NetworkExtensionDataframeProvider.class)
public class BusBarSectionPositionDataframeProvider extends AbstractSingleDataframeNetworkExtension {
    @Override
    public String getExtensionName() {
        return BusbarSectionPosition.NAME;
    }

    @Override
    public ExtensionInformation getExtensionInformation() {
        return new ExtensionInformation(BusbarSectionPosition.NAME, "Position information about the BusbarSection",
            "index : id (str), busbar_index (int), section_index (int)");
    }

    private Stream<BusbarSectionPosition> itemsStream(Network network) {
        return network.getBusbarSectionStream().filter(Objects::nonNull)
            .map(branch -> (BusbarSectionPosition) branch.getExtension(BusbarSectionPosition.class))
            .filter(Objects::nonNull);
    }

    private BusbarSectionPosition getOrThrow(Network network, String id) {
        BusbarSection busbarSection = network.getBusbarSection(id);
        if (busbarSection == null) {
            throw new PowsyblException("Invalid busbar section id : could not find " + id);
        }
        return busbarSection.getExtension(BusbarSectionPosition.class);
    }

    @Override
    public NetworkDataframeMapper createMapper() {
        return NetworkDataframeMapperBuilder.ofStream(this::itemsStream, this::getOrThrow)
            .stringsIndex("id", busbarSectionPosition -> (busbarSectionPosition.getExtendable()).getId())
            .ints("busbar_index", BusbarSectionPosition::getBusbarIndex, BusbarSectionPosition::setBusbarIndex)
            .ints("section_index", BusbarSectionPosition::getBusbarIndex, BusbarSectionPosition::setBusbarIndex)
            .build();
    }

    @Override
    public void removeExtensions(Network network, List<String> ids) {
        ids.stream().filter(Objects::nonNull)
            .map(network::getBusbarSection)
            .filter(Objects::nonNull)
            .forEach(busbarSection -> busbarSection.removeExtension(BusbarSectionPosition.class));
    }

    @Override
    public NetworkElementAdder createAdder() {
        return new BusBarSectionPositionDataframeAdder();
    }
}
