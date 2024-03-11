/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dataframe.network.modifications;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.ReporterModel;
import com.powsybl.dataframe.SeriesMetadata;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.modification.topology.ConnectVoltageLevelOnLineBuilder;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Optional;

import static com.powsybl.dataframe.network.adders.SeriesUtils.applyIfPresent;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class ConnectVoltageLevelOnLine implements DataframeNetworkModification {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("bbs_or_bus_id"),
        SeriesMetadata.strings("line_id"),
        SeriesMetadata.doubles("position_percent"),
        SeriesMetadata.strings("line1_id"),
        SeriesMetadata.strings("line1_name"),
        SeriesMetadata.strings("line2_id"),
        SeriesMetadata.strings("line2_name")
    );

    @Override
    public List<SeriesMetadata> getMetadata() {
        return METADATA;
    }

    private ConnectVoltageLevelOnLineBuilder createBuilder(Network network, UpdatingDataframe dataframe, int row) {
        ConnectVoltageLevelOnLineBuilder builder = new ConnectVoltageLevelOnLineBuilder();
        applyIfPresent(dataframe.getStrings("bbs_or_bus_id"), row, builder::withBusbarSectionOrBusId);
        applyIfPresent(dataframe.getStrings("line1_id"), row, builder::withLine1Id);
        applyIfPresent(dataframe.getStrings("line2_id"), row, builder::withLine2Id);
        applyIfPresent(dataframe.getStrings("line1_name"), row, builder::withLine1Name);
        applyIfPresent(dataframe.getStrings("line2_name"), row, builder::withLine2Name);
        applyIfPresent(dataframe.getDoubles("position_percent"), row, builder::withPositionPercent);
        Optional<String> lineIdOptional = dataframe.getStringValue("line_id", row);
        if (lineIdOptional.isEmpty()) {
            throw new PowsyblException("LineId cannot be empty.");
        }
        Line line = network.getLine(lineIdOptional.get());
        builder.withLine(line);
        return builder;
    }

    @Override
    public void applyModification(Network network, List<UpdatingDataframe> dataframes, boolean throwException,
                                  ReporterModel reporter) {
        if (dataframes.size() != 1) {
            throw new IllegalArgumentException("Expected only one input dataframe");
        }
        for (int row = 0; row < dataframes.get(0).getRowCount(); row++) {
            ConnectVoltageLevelOnLineBuilder builder = createBuilder(network, dataframes.get(0), row);
            builder.build().apply(network, throwException, reporter == null ? Reporter.NO_OP : reporter);
        }
    }
}
