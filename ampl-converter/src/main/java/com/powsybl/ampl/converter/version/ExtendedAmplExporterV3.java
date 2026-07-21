/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.VoltageRegulation;

import java.util.ArrayList;
import java.util.List;

import static com.powsybl.ampl.converter.AmplConstants.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class ExtendedAmplExporterV3 extends ExtendedAmplExporterV2 {
    private static final int BATTERY_V_REGUL_COLUMN_INDEX = 15;
    private static final int BATTERY_TARGET_V_COLUMN_INDEX = 16;
    private static final int GENERATOR_V_REGUL_BUS_COLUMN_INDEX = 17;

    public ExtendedAmplExporterV3(AmplExportConfig config,
                                  Network network,
                                  StringToIntMapper<AmplSubset> mapper,
                                  int variantIndex, int faultNum, int actionNum) {
        super(config, network, mapper, variantIndex, faultNum, actionNum);
    }

    @Override
    public List<Column> getBatteriesColumns() {
        List<Column> batteriesColumns = new ArrayList<>(super.getBatteriesColumns());
        // add columns for voltage regulation
        batteriesColumns.add(BATTERY_V_REGUL_COLUMN_INDEX, new Column(V_REGUL));
        batteriesColumns.add(BATTERY_TARGET_V_COLUMN_INDEX, new Column(TARGET_V));
        batteriesColumns.add(GENERATOR_V_REGUL_BUS_COLUMN_INDEX, new Column(V_REGUL_BUS));
        return batteriesColumns;
    }

    @Override
    public void addAdditionalCellsBattery(TableFormatterHelper formatterHelper, Battery battery) {
        super.addAdditionalCellsBattery(formatterHelper, battery);

        boolean isRegulating = false;
        double targetV = Double.NaN;
        int regulatingBusNum = -1;

        VoltageRegulation voltageRegulation = battery.getExtension(VoltageRegulation.class);
        if (voltageRegulation != null) {
            isRegulating = voltageRegulation.isVoltageRegulatorOn();
            targetV = voltageRegulation.getTargetV();
            regulatingBusNum = isRegulating && voltageRegulation.getRegulatingTerminal().isConnected() ?
                    getMapper().getInt(AmplSubset.BUS, voltageRegulation.getRegulatingTerminal().getBusView().getBus().getId()) : -1;
        }
        formatterHelper.addCell(isRegulating, BATTERY_V_REGUL_COLUMN_INDEX);
        formatterHelper.addCell(targetV, BATTERY_TARGET_V_COLUMN_INDEX);
        formatterHelper.addCell(regulatingBusNum, GENERATOR_V_REGUL_BUS_COLUMN_INDEX);
    }
}
