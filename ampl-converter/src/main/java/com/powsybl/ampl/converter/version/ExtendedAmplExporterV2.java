/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter.version;

import com.powsybl.ampl.converter.AmplExportConfig;
import com.powsybl.ampl.converter.AmplSubset;
import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatterHelper;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.util.HvdcUtils;

import java.util.ArrayList;
import java.util.List;

import static com.powsybl.ampl.converter.AmplConstants.*;

/**
 * 2nd extension of BasicAmplExporter, associated with AMPL version 1.2 (exporter id).
 * The extension adds:
 *  - A condenser boolean in the generator table.
 *  - The load target Q in the lcc converter station table.
 *  - TODO
 *
 * @author Pierre ARVY {@literal <pierre.arvy at artelys.com>}
 */
public class ExtendedAmplExporterV2 extends ExtendedAmplExporter {

    private static final int GENERATOR_IS_CONDENSER_COLUMN_INDEX = 16;
    private static final int LCC_TARGET_Q_COLUMN_INDEX = 5;

    public ExtendedAmplExporterV2(AmplExportConfig config,
                                Network network,
                                StringToIntMapper<AmplSubset> mapper,
                                int variantIndex, int faultNum, int actionNum) {
        super(config, network, mapper, variantIndex, faultNum, actionNum);
    }

    @Override
    public List<Column> getGeneratorsColumns() {
        List<Column> generatorsColumns = new ArrayList<>(super.getGeneratorsColumns());
        // add column to indicate if generator is a condenser
        generatorsColumns.add(GENERATOR_IS_CONDENSER_COLUMN_INDEX, new Column(CONDENSER));
        return generatorsColumns;
    }

    @Override
    public List<Column> getLccConverterStationsColumns() {
        List<Column> lccColumns = new ArrayList<>(super.getLccConverterStationsColumns());
        // add column for target Q of converter station
        lccColumns.add(LCC_TARGET_Q_COLUMN_INDEX, new Column(Q0));
        return lccColumns;
    }

    @Override
    public void addAdditionalCellsGenerator(TableFormatterHelper formatterHelper, Generator gen) {
        super.addAdditionalCellsGenerator(formatterHelper, gen);
        formatterHelper.addCell(gen.isCondenser(), GENERATOR_IS_CONDENSER_COLUMN_INDEX);
    }

    @Override
    public void addAdditionalCellsLccConverterStation(TableFormatterHelper formatterHelper,
                                                      LccConverterStation lccStation) {
        double loadTargetQ = HvdcUtils.getLccConverterStationLoadTargetQ(lccStation);
        formatterHelper.addCell(loadTargetQ, LCC_TARGET_Q_COLUMN_INDEX);
    }

}
