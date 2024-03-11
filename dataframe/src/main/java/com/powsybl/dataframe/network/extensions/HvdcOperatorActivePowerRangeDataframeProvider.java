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
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRange;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@soft.it>
 */
@AutoService(NetworkExtensionDataframeProvider.class)
public class HvdcOperatorActivePowerRangeDataframeProvider extends AbstractSingleDataframeNetworkExtension {

    @Override
    public String getExtensionName() {
        return HvdcOperatorActivePowerRange.NAME;
    }

    @Override
    public ExtensionInformation getExtensionInformation() {
        return new ExtensionInformation(HvdcOperatorActivePowerRange.NAME, "",
            "index : id (str), opr_from_cs1_to_cs2 (float), opr_from_cs2_to_cs1 (float)");
    }

    private Stream<HvdcOperatorActivePowerRange> itemsStream(Network network) {
        return network.getHvdcLineStream()
            .map(g -> (HvdcOperatorActivePowerRange) g.getExtension(HvdcOperatorActivePowerRange.class))
            .filter(Objects::nonNull);
    }

    private HvdcOperatorActivePowerRange getOrThrow(Network network, String id) {
        HvdcLine hl = network.getHvdcLine(id);
        if (hl == null) {
            throw new PowsyblException("HvdcLine '" + id + "' not found");
        }
        HvdcOperatorActivePowerRange hoapr = hl.getExtension(HvdcOperatorActivePowerRange.class);
        if (hoapr == null) {
            throw new PowsyblException("HvdcLine '" + id + "' has no HvdcOperatorActivePowerRange extension");
        }
        return hoapr;
    }

    @Override
    public NetworkDataframeMapper createMapper() {
        return NetworkDataframeMapperBuilder.ofStream(this::itemsStream, this::getOrThrow)
            .stringsIndex("id", ext -> ext.getExtendable().getId())
            .doubles("opr_from_cs1_to_cs2", HvdcOperatorActivePowerRange::getOprFromCS1toCS2,
                (r, d) -> r.setOprFromCS1toCS2((float) d))
            .doubles("opr_from_cs2_to_cs1", HvdcOperatorActivePowerRange::getOprFromCS2toCS1,
                (r, d) -> r.setOprFromCS2toCS1((float) d))
            .build();
    }

    @Override
    public void removeExtensions(Network network, List<String> ids) {
        ids.stream().filter(Objects::nonNull)
            .map(network::getHvdcLine)
            .filter(Objects::nonNull)
            .forEach(g -> g.removeExtension(HvdcOperatorActivePowerRange.class));
    }

    @Override
    public NetworkElementAdder createAdder() {
        return new HvdcOperatorActivePowerRangeDataframeAdder();
    }
}
