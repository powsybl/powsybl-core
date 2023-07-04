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
import com.powsybl.iidm.modification.topology.ReplaceTeePointByVoltageLevelOnLineBuilder;
import com.powsybl.iidm.network.Network;

import java.util.List;

import static com.powsybl.dataframe.network.adders.SeriesUtils.applyIfPresent;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class ReplaceTeePointByVoltageLevelOnLine implements DataframeNetworkModification {

    public static final String NEW_LINE_1_ID = "new_line1_id";
    public static final String NEW_LINE_2_ID = "new_line2_id";
    public static final String NEW_LINE_1_NAME = "new_line1_name";
    public static final String NEW_LINE_2_NAME = "new_line2_name";
    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("tee_point_line1"),
        SeriesMetadata.strings("tee_point_line2"),
        SeriesMetadata.strings("tee_point_line_to_remove"),
        SeriesMetadata.strings("bbs_or_bus_id"),
        SeriesMetadata.strings(NEW_LINE_1_ID),
        SeriesMetadata.strings(NEW_LINE_2_ID),
        SeriesMetadata.strings(NEW_LINE_1_NAME),
        SeriesMetadata.strings(NEW_LINE_2_NAME)
    );

    @Override
    public List<SeriesMetadata> getMetadata() {
        return METADATA;
    }

    private ReplaceTeePointByVoltageLevelOnLineBuilder createBuilder(UpdatingDataframe dataframe, int row) {
        ReplaceTeePointByVoltageLevelOnLineBuilder builder = new ReplaceTeePointByVoltageLevelOnLineBuilder();
        applyIfPresent(dataframe.getStrings("tee_point_line1"), row, builder::withTeePointLine1);
        applyIfPresent(dataframe.getStrings("tee_point_line2"), row, builder::withTeePointLine2);
        applyIfPresent(dataframe.getStrings("tee_point_line_to_remove"), row, builder::withTeePointLineToRemove);
        applyIfPresent(dataframe.getStrings("bbs_or_bus_id"), row, builder::withBbsOrBusId);
        applyIfPresent(dataframe.getStrings(NEW_LINE_1_ID), row, builder::withNewLine1Id);
        applyIfPresent(dataframe.getStrings(NEW_LINE_2_ID), row, builder::withNewLine2Id);
        applyIfPresent(dataframe.getStrings(NEW_LINE_1_NAME), row, builder::withNewLine1Name);
        applyIfPresent(dataframe.getStrings(NEW_LINE_2_NAME), row, builder::withNewLine2Id);
        if (dataframe.getStringValue(NEW_LINE_1_NAME, row).isEmpty()) {
            applyIfPresent(dataframe.getStrings(NEW_LINE_1_ID), row, builder::withNewLine1Name);
        }
        if (dataframe.getStringValue(NEW_LINE_2_NAME, row).isEmpty()) {
            applyIfPresent(dataframe.getStrings(NEW_LINE_2_ID), row, builder::withNewLine2Name);
        }
        return builder;
    }

    @Override
    public void applyModification(Network network, List<UpdatingDataframe> dataframes, boolean throwException,
                                  ReporterModel reporter) {
        if (dataframes.size() != 1) {
            throw new IllegalArgumentException("Expected only one input dataframe");
        }
        for (int row = 0; row < dataframes.get(0).getRowCount(); row++) {
            ReplaceTeePointByVoltageLevelOnLineBuilder builder = createBuilder(dataframes.get(0), row);
            builder.build().apply(network, throwException, reporter == null ? Reporter.NO_OP : reporter);
        }
    }
}
