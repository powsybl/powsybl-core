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
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.util.HvdcUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public class ExtendedAmplExporterV4 extends ExtendedAmplExporterV3 {

    private static final int GENERATOR_MINQ_TARGETP_INDEX = 13;
    private static final int GENERATOR_MAXQ_TARGETP_INDEX = 14;
    private static final int GENERATOR_REACTIVE_POWER_PER_ACTIVE_POWER_INDEX = 19;
    private static final int GENERATOR_REACTIVE_POWER_PER_ACTIVE_POWER_SLOPE_INDEX = 20;

    private static final int BATTERY_MINQ_TARGETP_INDEX = 15;
    private static final int BATTERY_MAXQ_TARGETP_INDEX = 16;
    private static final int BATTERY_REACTIVE_POWER_PER_ACTIVE_POWER_INDEX = 20;
    private static final int BATTERY_REACTIVE_POWER_PER_ACTIVE_POWER_SLOPE_INDEX = 21;


    private static final int VSC_MINQ_TARGETP_INDEX = 13;
    private static final int VSC_MAXQ_TARGETP_INDEX = 14;


    public ExtendedAmplExporterV4(AmplExportConfig config,
                                  Network network,
                                  StringToIntMapper<AmplSubset> mapper,
                                  int variantIndex, int faultNum, int actionNum) {
        super(config, network, mapper, variantIndex, faultNum, actionNum);
    }

    @Override
    public List<Column> getGeneratorsColumns() {
        List<Column> generatorsColumns = new ArrayList<>(super.getGeneratorsColumns());
        // Add column for MinQ at TargetP
        generatorsColumns.add(GENERATOR_MINQ_TARGETP_INDEX, new Column("minQTargetP (MVar)"));
        // Add column for MaxQ at TargetP
        generatorsColumns.add(GENERATOR_MAXQ_TARGETP_INDEX, new Column("maxQTargetP (MVar)"));
        // Add column for reactive power per active power regulation
        generatorsColumns.add(GENERATOR_REACTIVE_POWER_PER_ACTIVE_POWER_INDEX, new Column("Q/P in use"));
        // Add column for reactive power per active power regulation slope
        generatorsColumns.add(GENERATOR_REACTIVE_POWER_PER_ACTIVE_POWER_SLOPE_INDEX, new Column("Q/P slope"));
        return generatorsColumns;
    }

    @Override
    public List<Column> getBatteriesColumns() {
        List<Column> batteriesColumns = new ArrayList<>(super.getBatteriesColumns());
        // Add column for MinQ at TargetP
        batteriesColumns.add(BATTERY_MINQ_TARGETP_INDEX, new Column("minQTargetP (MVar)"));
        // Add column for MaxQ at TargetP
        batteriesColumns.add(BATTERY_MAXQ_TARGETP_INDEX, new Column("maxQTargetP (MVar)"));
        // Add column for reactive power per active power regulation
        batteriesColumns.add(BATTERY_REACTIVE_POWER_PER_ACTIVE_POWER_INDEX, new Column("Q/P in use"));
        // Add column for reactive power per active power regulation slope
        batteriesColumns.add(BATTERY_REACTIVE_POWER_PER_ACTIVE_POWER_SLOPE_INDEX, new Column("Q/P slope"));
        return batteriesColumns;
    }

    @Override
    public List<Column> getVscConverterStationsColumns() {
        List<Column> vscConverterStationsColumns = new ArrayList<>(super.getVscConverterStationsColumns());
        // Add column for MinQ at TargetP
        vscConverterStationsColumns.add(VSC_MINQ_TARGETP_INDEX, new Column("minQTargetP (MVar)"));
        // Add column for MaxQ at TargetP
        vscConverterStationsColumns.add(VSC_MAXQ_TARGETP_INDEX, new Column("maxQTargetP (MVar)"));
        return vscConverterStationsColumns;
    }

    @Override
    public void addAdditionalCellsGenerator(TableFormatterHelper formatterHelper, Generator generator) {
        super.addAdditionalCellsGenerator(formatterHelper, generator);
        ReactiveLimits reactiveLimits = generator.getReactiveLimits();
        double targetP = generator.getTargetP();
        Pair<Double, Double> minQMaxQAtP = getMinQMaxQAtP(reactiveLimits, targetP);

        formatterHelper.addCell(minQMaxQAtP.getLeft(), GENERATOR_MINQ_TARGETP_INDEX);
        formatterHelper.addCell(minQMaxQAtP.getRight(), GENERATOR_MAXQ_TARGETP_INDEX);

        boolean isRegulatingReactivePowerPerActivePower = generator.isRegulatingWithMode(RegulationMode.REACTIVE_POWER_PER_ACTIVE_POWER);
        formatterHelper.addCell(isRegulatingReactivePowerPerActivePower, GENERATOR_REACTIVE_POWER_PER_ACTIVE_POWER_INDEX);
        double reactivePowerPerActivePowerSlope = isRegulatingReactivePowerPerActivePower? generator.getVoltageRegulation().getSlope() : Double.NaN;
        formatterHelper.addCell(reactivePowerPerActivePowerSlope, GENERATOR_REACTIVE_POWER_PER_ACTIVE_POWER_SLOPE_INDEX);
    }

    @Override
    public void addAdditionalCellsBattery(TableFormatterHelper formatterHelper, Battery battery) {
        super.addAdditionalCellsBattery(formatterHelper, battery);
        ReactiveLimits reactiveLimits = battery.getReactiveLimits();
        double targetP = battery.getTargetP();
        Pair<Double, Double> minQMaxQAtP = getMinQMaxQAtP(reactiveLimits, targetP);

        formatterHelper.addCell(minQMaxQAtP.getLeft(), BATTERY_MINQ_TARGETP_INDEX);
        formatterHelper.addCell(minQMaxQAtP.getRight(), BATTERY_MAXQ_TARGETP_INDEX);

        boolean isRegulatingReactivePowerPerActivePower = battery.isRegulatingWithMode(RegulationMode.REACTIVE_POWER_PER_ACTIVE_POWER);
        formatterHelper.addCell(isRegulatingReactivePowerPerActivePower, BATTERY_REACTIVE_POWER_PER_ACTIVE_POWER_INDEX);
        double reactivePowerPerActivePowerSlope = isRegulatingReactivePowerPerActivePower? battery.getVoltageRegulation().getSlope() : Double.NaN;
        formatterHelper.addCell(reactivePowerPerActivePowerSlope, BATTERY_REACTIVE_POWER_PER_ACTIVE_POWER_SLOPE_INDEX);
    }

    @Override
    public void addAdditionalCellsVscConverterStation(TableFormatterHelper formatterHelper, VscConverterStation vscStation) {
        super.addAdditionalCellsVscConverterStation(formatterHelper, vscStation);
        ReactiveLimits reactiveLimits = vscStation.getReactiveLimits();
        double targetP = HvdcUtils.getConverterStationTargetP(vscStation);
        Pair<Double, Double> minQMaxQAtP = getMinQMaxQAtP(reactiveLimits, targetP);

        formatterHelper.addCell(minQMaxQAtP.getLeft(), VSC_MINQ_TARGETP_INDEX);
        formatterHelper.addCell(minQMaxQAtP.getRight(), VSC_MAXQ_TARGETP_INDEX);
    }

    private Pair<Double, Double> getMinQMaxQAtP(ReactiveLimits reactiveLimits, double p) {
        double minQAtP;
        double maxQAtP;

        if (reactiveLimits.getKind() == ReactiveLimitsKind.CURVE) {
            minQAtP = ((ReactiveCapabilityCurve) reactiveLimits).getMinQ(p, true);
            maxQAtP = ((ReactiveCapabilityCurve) reactiveLimits).getMaxQ(p, true);
        }
        else {
            minQAtP = reactiveLimits.getMinQ(p);
            maxQAtP = reactiveLimits.getMaxQ(p);
        }
        return Pair.of(minQAtP, maxQAtP);
    }
}
