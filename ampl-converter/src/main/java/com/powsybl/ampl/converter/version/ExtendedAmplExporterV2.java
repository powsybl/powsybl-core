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
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;
import com.powsybl.iidm.network.util.HvdcUtils;

import java.util.ArrayList;
import java.util.List;

import static com.powsybl.ampl.converter.AmplConstants.*;

/**
 * 2nd extension of BasicAmplExporter, associated with AMPL version 1.2 (exporter id).
 * The extension adds:
 *  - A condenser boolean in the generator table.
 *  - The load target P and Q in the lcc converter station table.
 *  - The target P and AC emulation parameters, along with a boolean to indicate if emulation is active, in the vsc converter station table.
 *
 * @author Pierre ARVY {@literal <pierre.arvy at artelys.com>}
 */
public class ExtendedAmplExporterV2 extends ExtendedAmplExporter {

    private static final int GENERATOR_IS_CONDENSER_COLUMN_INDEX = 16;
    private static final int LCC_TARGET_P_COLUMN_INDEX = 5;
    private static final int LCC_TARGET_Q_COLUMN_INDEX = 6;
    private static final int VSC_AC_EMULATION_COLUMN_INDEX = 15;
    private static final int VSC_TARGET_P_COLUMN_INDEX = 16;
    private static final int VSC_P_OFFSET_COLUMN_INDEX = 17;
    private static final int VSC_K_COLUMN_INDEX = 18;

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
        generatorsColumns.add(GENERATOR_IS_CONDENSER_COLUMN_INDEX, new Column("condenser"));
        return generatorsColumns;
    }

    @Override
    public List<Column> getLccConverterStationsColumns() {
        List<Column> lccColumns = new ArrayList<>(super.getLccConverterStationsColumns());
        // add columns for load target P/Q of converter station
        lccColumns.add(LCC_TARGET_P_COLUMN_INDEX, new Column(P0));
        lccColumns.add(LCC_TARGET_Q_COLUMN_INDEX, new Column(Q0));
        return lccColumns;
    }

    @Override
    public List<Column> getVscConverterStationsColumns() {
        List<Column> vscColumns = new ArrayList<>(super.getVscConverterStationsColumns());
        // add columns for AC emulation
        vscColumns.add(VSC_AC_EMULATION_COLUMN_INDEX, new Column("ac emul."));
        // add column for target P of converter station
        vscColumns.add(VSC_TARGET_P_COLUMN_INDEX, new Column("targetP (MW)"));
        vscColumns.add(VSC_P_OFFSET_COLUMN_INDEX, new Column("P offset (MW)"));
        vscColumns.add(VSC_K_COLUMN_INDEX, new Column("k (MW/rad)"));
        return vscColumns;
    }

    @Override
    public void addAdditionalCellsGenerator(TableFormatterHelper formatterHelper, Generator gen) {
        super.addAdditionalCellsGenerator(formatterHelper, gen);
        formatterHelper.addCell(gen.isCondenser(), GENERATOR_IS_CONDENSER_COLUMN_INDEX);
    }

    @Override
    public void addAdditionalCellsLccConverterStation(TableFormatterHelper formatterHelper,
                                                      LccConverterStation lccStation) {
        double loadTargetP = HvdcUtils.getConverterStationTargetP(lccStation);
        formatterHelper.addCell(loadTargetP, LCC_TARGET_P_COLUMN_INDEX);
        double loadTargetQ = HvdcUtils.getLccConverterStationLoadTargetQ(lccStation);
        formatterHelper.addCell(loadTargetQ, LCC_TARGET_Q_COLUMN_INDEX);
    }

    @Override
    public void addAdditionalCellsVscConverterStation(TableFormatterHelper formatterHelper,
                                                      VscConverterStation vscStation) {
        double targetP = HvdcUtils.getConverterStationTargetP(vscStation);
        boolean isEnabled = false;
        double p0 = Double.NaN;
        double k = Double.NaN;
        HvdcAngleDroopActivePowerControl droopControl = vscStation.getHvdcLine().getExtension(HvdcAngleDroopActivePowerControl.class);
        if (droopControl != null) {
            isEnabled = droopControl.isEnabled();
            p0 = droopControl.getP0();
            k = droopControl.getDroop() * 180 / Math.PI;
        }
        formatterHelper.addCell(isEnabled, VSC_AC_EMULATION_COLUMN_INDEX);
        formatterHelper.addCell(targetP, VSC_TARGET_P_COLUMN_INDEX);
        formatterHelper.addCell(p0, VSC_P_OFFSET_COLUMN_INDEX);
        formatterHelper.addCell(k, VSC_K_COLUMN_INDEX);
    }
}
