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
import com.powsybl.entsoe.util.EntsoeArea;
import com.powsybl.entsoe.util.EntsoeGeographicalCode;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
@AutoService(NetworkExtensionDataframeProvider.class)
public class EntsoeAreaDataframeProvider extends AbstractSingleDataframeNetworkExtension {

    @Override
    public String getExtensionName() {
        return EntsoeArea.NAME;
    }

    @Override
    public ExtensionInformation getExtensionInformation() {
        return new ExtensionInformation(EntsoeArea.NAME, "Provides Entsoe geographical code for a substation",
            "index : id (str), code (str)");
    }

    private Stream<EntsoeArea> itemsStream(Network network) {
        return network.getSubstationStream()
            .map(s -> (EntsoeArea) s.getExtension(EntsoeArea.class))
            .filter(Objects::nonNull);
    }

    private EntsoeArea getOrThrow(Network network, String id) {
        Substation s = network.getSubstation(id);
        if (s == null) {
            throw new PowsyblException("Substation '" + id + "' not found");
        }
        EntsoeArea ea = s.getExtension(EntsoeArea.class);
        if (ea == null) {
            throw new PowsyblException("Substation '" + id + "' has no EntsoeArea extension");
        }
        return ea;
    }

    @Override
    public NetworkDataframeMapper createMapper() {
        return NetworkDataframeMapperBuilder.ofStream(this::itemsStream, this::getOrThrow)
            .stringsIndex("id", ext -> ext.getExtendable().getId())
            .enums("code", EntsoeGeographicalCode.class, EntsoeArea::getCode, EntsoeArea::setCode)
            .build();
    }

    @Override
    public void removeExtensions(Network network, List<String> ids) {
        ids.stream().filter(Objects::nonNull)
            .map(id -> network.getSubstation(id))
            .filter(Objects::nonNull)
            .forEach(g -> g.removeExtension(EntsoeArea.class));
    }

    @Override
    public NetworkElementAdder createAdder() {
        return new EntsoeAreaDataframeAdder();
    }
}
