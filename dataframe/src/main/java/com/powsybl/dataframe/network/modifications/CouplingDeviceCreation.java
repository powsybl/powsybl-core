/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dataframe.network.modifications;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.ReporterModel;
import com.powsybl.dataframe.SeriesMetadata;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.modification.topology.CreateCouplingDeviceBuilder;
import com.powsybl.iidm.network.Network;

import java.util.List;

import static com.powsybl.dataframe.network.adders.SeriesUtils.applyIfPresent;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class CouplingDeviceCreation implements DataframeNetworkModification {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("bus_or_busbar_section_id_1"),
        SeriesMetadata.strings("bus_or_busbar_section_id_2"),
        SeriesMetadata.strings("switch_prefix_id")
    );

    @Override
    public List<SeriesMetadata> getMetadata() {
        return METADATA;
    }

    private CreateCouplingDeviceBuilder createBuilder(UpdatingDataframe dataframe, int row) {
        CreateCouplingDeviceBuilder builder = new CreateCouplingDeviceBuilder();
        applyIfPresent(dataframe.getStrings("bus_or_busbar_section_id_1"), row, builder::withBusOrBusbarSectionId1);
        applyIfPresent(dataframe.getStrings("bus_or_busbar_section_id_2"), row, builder::withBusOrBusbarSectionId2);
        applyIfPresent(dataframe.getStrings("switch_prefix_id"), row, builder::withSwitchPrefixId);
        return builder;
    }

    @Override
    public void applyModification(Network network, List<UpdatingDataframe> dataframes, boolean throwException,
                                  ReporterModel reporter) {
        if (dataframes.size() != 1) {
            throw new IllegalArgumentException("Expected only one input dataframe");
        }
        for (int row = 0; row < dataframes.get(0).getRowCount(); row++) {
            CreateCouplingDeviceBuilder builder = createBuilder(dataframes.get(0), row);
            builder.build().apply(network, throwException, reporter == null ? Reporter.NO_OP : reporter);
        }
    }
}
