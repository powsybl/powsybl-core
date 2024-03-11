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
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.modification.topology.CreateLineOnLineBuilder;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.iidm.network.Network;

import java.util.List;

import static com.powsybl.dataframe.network.adders.SeriesUtils.applyIfPresent;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class CreateLineOnLine implements DataframeNetworkModification {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("bbs_or_bus_id"),
        SeriesMetadata.strings("new_line_id"),
        SeriesMetadata.doubles("new_line_r"),
        SeriesMetadata.doubles("new_line_x"),
        SeriesMetadata.doubles("new_line_b1"),
        SeriesMetadata.doubles("new_line_b2"),
        SeriesMetadata.doubles("new_line_g1"),
        SeriesMetadata.doubles("new_line_g2"),
        SeriesMetadata.strings("line_id"),
        SeriesMetadata.strings("line1_id"),
        SeriesMetadata.strings("line1_name"),
        SeriesMetadata.strings("line2_id"),
        SeriesMetadata.strings("line2_name"),
        SeriesMetadata.doubles("position_percent"),
        SeriesMetadata.booleans("create_fictitious_substation"),
        SeriesMetadata.strings("fictitious_voltage_level_id"),
        SeriesMetadata.strings("fictitious_voltage_level_name"),
        SeriesMetadata.strings("fictitious_substation_id"),
        SeriesMetadata.strings("fictitious_substation_name")
    );

    @Override
    public List<SeriesMetadata> getMetadata() {
        return METADATA;
    }

    private LineAdder createAdder(Network network, UpdatingDataframe dataframe, int row) {
        LineAdder adder = network.newLine();
        applyIfPresent(dataframe.getStrings("new_line_id"), row, adder::setId);
        applyIfPresent(dataframe.getDoubles("new_line_r"), row, adder::setR);
        applyIfPresent(dataframe.getDoubles("new_line_b1"), row, adder::setB1);
        applyIfPresent(dataframe.getDoubles("new_line_b2"), row, adder::setB2);
        applyIfPresent(dataframe.getDoubles("new_line_x"), row, adder::setX);
        applyIfPresent(dataframe.getDoubles("new_line_g1"), row, adder::setG1);
        applyIfPresent(dataframe.getDoubles("new_line_g2"), row, adder::setG2);
        return adder;
    }

    private CreateLineOnLineBuilder createBuilder(Network network, UpdatingDataframe dataframe, int row) {
        LineAdder adder = createAdder(network, dataframe, row);
        CreateLineOnLineBuilder builder = new CreateLineOnLineBuilder().withLineAdder(adder);
        applyIfPresent(dataframe.getDoubles("position_percent"), row, builder::withPositionPercent);
        applyIfPresent(dataframe.getStrings("bbs_or_bus_id"), row, builder::withBusbarSectionOrBusId);
        applyIfPresent(dataframe.getStrings("fictitious_voltage_level_id"), row, builder::withFictitiousVoltageLevelId);
        applyIfPresent(dataframe.getStrings("fictitious_voltage_level_name"), row,
            builder::withFictitiousVoltageLevelName);
        applyIfPresent(dataframe.getStrings("fictitious_substation_id"), row, builder::withFictitiousSubstationId);
        applyIfPresent(dataframe.getStrings("fictitious_substation_name"), row, builder::withFictitiousSubstationName);
        applyIfPresent(dataframe.getStrings("line1_id"), row, builder::withLine1Id);
        applyIfPresent(dataframe.getStrings("line2_id"), row, builder::withLine2Id);
        applyIfPresent(dataframe.getStrings("line1_name"), row, builder::withLine1Name);
        applyIfPresent(dataframe.getStrings("line2_name"), row, builder::withLine2Name);
        String lineId = dataframe.getStringValue("line_id", row)
            .orElseThrow(() -> new PowsyblException("LineId cannot be empty."));
        builder.withLine(network.getLine(lineId));
        boolean createFictitiousSubstation = Boolean.parseBoolean(
            dataframe.getStringValue("create_fictitious_substation", row).orElse("False"));
        builder.withCreateFictitiousSubstation(createFictitiousSubstation);
        return builder;
    }

    @Override
    public void applyModification(Network network, List<UpdatingDataframe> dataframes, boolean throwException,
                                  ReporterModel reporter) {
        if (dataframes.size() != 1) {
            throw new IllegalArgumentException("Expected only one input dataframe");
        }
        for (int row = 0; row < dataframes.get(0).getRowCount(); row++) {
            CreateLineOnLineBuilder builder = createBuilder(network, dataframes.get(0), row);
            NetworkModification modification = builder.build();
            modification.apply(network, throwException, reporter == null ? Reporter.NO_OP : reporter);
        }
    }
}
