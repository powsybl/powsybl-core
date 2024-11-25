/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter.version;

import com.powsybl.ampl.converter.AmplConstants;
import com.powsybl.ampl.converter.AmplExportConfig;
import com.powsybl.ampl.converter.AmplSubset;
import com.powsybl.ampl.converter.AmplUtil;
import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.io.table.TableFormatterHelper;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.ConnectedComponents;
import com.powsybl.iidm.network.util.SV;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

import static com.powsybl.ampl.converter.AmplConstants.*;

/**
 * Legacy exporter that must be retrocompatible,
 * and only fixes should be made on this class.
 *
 * @author Nicolas Pierre {@literal <nicolas.pierre at artelys.com>} for the refactor
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>} for the original code
 */
public class BasicAmplExporter implements AmplColumnsExporter {

    private final AmplExportConfig config;
    private final Network network;
    private final StringToIntMapper<AmplSubset> mapper;
    private final int variantIndex;
    private final int faultNum;
    private final int actionNum;
    private HashMap<String, HvdcLine> hvdcLinesMap;

    public BasicAmplExporter(AmplExportConfig config, Network network, StringToIntMapper<AmplSubset> mapper,
                             int variantIndex, int faultNum, int actionNum) {

        this.config = Objects.requireNonNull(config);
        this.network = Objects.requireNonNull(network);
        this.mapper = Objects.requireNonNull(mapper);
        this.variantIndex = variantIndex;
        this.faultNum = faultNum;
        this.actionNum = actionNum;
    }

    @Override
    public List<Column> getRtcColumns() {
        List<Column> columns = new ArrayList<>(8);
        columns.add(new Column(VARIANT));
        columns.add(new Column(NUM));
        columns.add(new Column("tap"));
        columns.add(new Column("table"));
        columns.add(new Column("onLoad"));
        if (config.isExportRatioTapChangerVoltageTarget()) {
            columns.add(new Column("targetV"));
        }
        columns.add(new Column(FAULT));
        columns.add(new Column(config.getActionType().getLabel()));
        columns.add(new Column(ID));
        return columns;
    }

    @Override
    public List<Column> getPtcColumns() {
        return List.of(new Column(VARIANT),
            new Column(NUM),
            new Column("tap"),
            new Column("table"),
            new Column(FAULT),
            new Column(config.getActionType().getLabel()),
            new Column(ID));
    }

    @Override
    public List<Column> getCurrentLimitsColumns() {
        return List.of(new Column(VARIANT),
            new Column(NUM),
            new Column("branch"),
            new Column("side"),
            new Column("limit (A)"),
            new Column("accept. duration (s)"),
            new Column(FAULT),
            new Column(config.getActionType().getLabel()));
    }

    @Override
    public List<Column> getHvdcLinesColumns() {
        return List.of(
            new Column(VARIANT),
            new Column(NUM),
            new Column("type"),
            new Column("converterStation1"),
            new Column("converterStation2"),
            new Column("r (ohm)"),
            new Column("nomV (KV)"),
            new Column("convertersMode"),
            new Column("targetP (MW)"),
            new Column(MAXP),
            new Column(FAULT),
            new Column(config.getActionType().getLabel()),
            new Column(ID),
            new Column(DESCRIPTION)
        );
    }

    @Override
    public List<Column> getLccConverterStationsColumns() {
        return List.of(
            new Column(VARIANT),
            new Column(NUM),
            new Column(BUS),
            new Column(CON_BUS),
            new Column(SUBSTATION),
            new Column("lossFactor (%PDC)"),
            new Column("powerFactor"),
            new Column(FAULT),
            new Column(config.getActionType().getLabel()),
            new Column(ID),
            new Column(DESCRIPTION),
            new Column(ACTIVE_POWER),
            new Column(REACTIVE_POWER)
        );
    }

    @Override
    public List<Column> getVscConverterStationsColumns() {
        return List.of(
            new Column(VARIANT),
            new Column(NUM),
            new Column(BUS),
            new Column(CON_BUS),
            new Column(SUBSTATION),
            new Column(MINP),
            new Column(MAXP),
            new Column(MIN_Q_MAX_P),
            new Column(MIN_Q0),
            new Column(MIN_Q_MIN_P),
            new Column(MAX_Q_MAX_P),
            new Column(MAX_Q0),
            new Column(MAX_Q_MIN_P),
            new Column(V_REGUL),
            new Column(TARGET_V),
            new Column(TARGET_Q),
            new Column("lossFactor (%PDC)"),
            new Column(FAULT),
            new Column(config.getActionType().getLabel()),
            new Column(ID),
            new Column(DESCRIPTION),
            new Column(ACTIVE_POWER),
            new Column(REACTIVE_POWER)
        );
    }

    @Override
    public List<Column> getSubstationsColumns() {
        return List.of(
            new Column(VARIANT),
            new Column(NUM),
            new Column("unused1"),
            new Column("unused2"),
            new Column("nomV (KV)"),
            new Column("minV (pu)"),
            new Column("maxV (pu)"),
            new Column(FAULT),
            new Column(config.getActionType().getLabel()),
            new Column("country"),
            new Column(ID),
            new Column(DESCRIPTION)
        );
    }

    @Override
    public List<Column> getLoadsColumns() {
        return List.of(
            new Column(VARIANT),
            new Column(NUM),
            new Column(BUS),
            new Column(SUBSTATION),
            new Column(P0),
            new Column("q0 (MVar)"),
            new Column(FAULT),
            new Column(config.getActionType().getLabel()),
            new Column(ID),
            new Column(DESCRIPTION),
            new Column("p (MW)"),
            new Column("q (MVar)")
        );
    }

    @Override
    public List<Column> getShuntsColumns() {
        return List.of(
            new Column(VARIANT),
            new Column(NUM),
            new Column(BUS),
            new Column(CON_BUS),
            new Column(SUBSTATION),
            new Column("minB (pu)"),
            new Column("maxB (pu)"),
            new Column("inter. points"),
            new Column("b (pu)"),
            new Column(FAULT),
            new Column(config.getActionType().getLabel()),
            new Column(ID),
            new Column(DESCRIPTION),
            new Column(ACTIVE_POWER),
            new Column(REACTIVE_POWER),
            new Column("sections count")
        );
    }

    @Override
    public List<Column> getStaticVarCompensatorColumns() {
        return List.of(
            new Column(VARIANT),
            new Column(NUM),
            new Column(BUS),
            new Column(CON_BUS),
            new Column(SUBSTATION),
            new Column("minB (pu)"),
            new Column("maxB (pu)"),
            new Column(V_REGUL),
            new Column(TARGET_V),
            new Column(TARGET_Q),
            new Column(FAULT),
            new Column(config.getActionType().getLabel()),
            new Column(ID),
            new Column(DESCRIPTION),
            new Column(ACTIVE_POWER),
            new Column(REACTIVE_POWER)
        );
    }

    @Override
    public List<Column> getGeneratorsColumns() {
        return List.of(
            new Column(VARIANT),
            new Column(NUM),
            new Column(BUS),
            new Column(CON_BUS),
            new Column(SUBSTATION),
            new Column(MINP),
            new Column(MAXP),
            new Column(MIN_Q_MAX_P),
            new Column(MIN_Q0),
            new Column(MIN_Q_MIN_P),
            new Column(MAX_Q_MAX_P),
            new Column(MAX_Q0),
            new Column(MAX_Q_MIN_P),
            new Column(V_REGUL),
            new Column(TARGET_V),
            new Column("targetP (MW)"),
            new Column(TARGET_Q),
            new Column(FAULT),
            new Column(config.getActionType().getLabel()),
            new Column(ID),
            new Column(DESCRIPTION),
            new Column(ACTIVE_POWER),
            new Column(REACTIVE_POWER)
        );
    }

    @Override
    public List<Column> getBatteriesColumns() {
        return List.of(
            new Column(VARIANT),
            new Column(NUM),
            new Column(BUS),
            new Column(CON_BUS),
            new Column(SUBSTATION),
            new Column(P0),
            new Column(Q0),
            new Column(MINP),
            new Column(MAXP),
            new Column(MIN_Q_MAX_P),
            new Column(MIN_Q0),
            new Column(MIN_Q_MIN_P),
            new Column(MAX_Q_MAX_P),
            new Column(MAX_Q0),
            new Column(MAX_Q_MIN_P),
            new Column(FAULT),
            new Column(config.getActionType().getLabel()),
            new Column(ID),
            new Column(DESCRIPTION),
            new Column(ACTIVE_POWER),
            new Column(REACTIVE_POWER)
        );
    }

    @Override
    public List<Column> getBranchesColumns() {
        return List.of(new Column(VARIANT),
            new Column(NUM),
            new Column("bus1"),
            new Column("bus2"),
            new Column("3wt num"),
            new Column("sub.1"),
            new Column("sub.2"),
            new Column("r (pu)"),
            new Column("x (pu)"),
            new Column("g1 (pu)"),
            new Column("g2 (pu)"),
            new Column("b1 (pu)"),
            new Column("b2 (pu)"),
            new Column("cst ratio (pu)"),
            new Column("ratio tc"),
            new Column("phase tc"),
            new Column("p1 (MW)"),
            new Column("p2 (MW)"),
            new Column("q1 (MVar)"),
            new Column("q2 (MVar)"),
            new Column("patl1 (A)"),
            new Column("patl2 (A)"),
            new Column("merged"),
            new Column(FAULT),
            new Column(config.getActionType().getLabel()),
            new Column(ID),
            new Column(DESCRIPTION));
    }

    @Override
    public List<Column> getTapChangerTableColumns() {
        return List.of(new Column(VARIANT),
            new Column(NUM),
            new Column("tap"),
            new Column("var ratio"),
            new Column("x (pu)"),
            new Column("angle (rad)"),
            new Column(FAULT),
            new Column(config.getActionType().getLabel()));
    }

    @Override
    public List<Column> getBusesColumns() {
        return List.of(new Column(VARIANT),
            new Column(NUM),
            new Column(SUBSTATION),
            new Column("cc"),
            new Column("v (pu)"),
            new Column("theta (rad)"),
            new Column("p (MW)"),
            new Column("q (MVar)"),
            new Column(FAULT),
            new Column(config.getActionType().getLabel()),
            new Column(ID));
    }

    @Override
    public void writeTwoWindingsTransformerTapChangerTableToFormatter(TableFormatter formatter,
                                                                      TwoWindingsTransformer twt) throws IOException {
        Terminal t2 = twt.getTerminal2();
        double vb2 = t2.getVoltageLevel().getNominalV();
        double zb2 = vb2 * vb2 / AmplConstants.SB;

        RatioTapChanger rtc = twt.getRatioTapChanger();
        if (rtc != null) {
            String id = twt.getId() + RATIO_TABLE_SUFFIX;
            writeRatioTapChanger(formatter, id, zb2, twt.getX(), rtc);
        }

        PhaseTapChanger ptc = twt.getPhaseTapChanger();
        if (ptc != null) {
            String id = twt.getId() + PHASE_TABLE_SUFFIX;
            writePhaseTapChanger(formatter, id, zb2, twt.getX(), ptc);
        }
    }

    @Override
    public void writeThreeWindingsTransformerTapChangerTableToFormatter(TableFormatter formatter,
                                                                        ThreeWindingsTransformer twt) throws IOException {
        int legNumber = 0;
        for (ThreeWindingsTransformer.Leg leg : twt.getLegs()) {
            legNumber++;
            RatioTapChanger rtc = leg.getRatioTapChanger();
            double vb = twt.getRatedU0();
            double zb = vb * vb / AmplConstants.SB;
            if (rtc != null) {
                String id = twt.getId() + "_leg" + legNumber + RATIO_TABLE_SUFFIX;
                writeRatioTapChanger(formatter, id, zb, leg.getX(), rtc);
            }
            PhaseTapChanger ptc = leg.getPhaseTapChanger();
            if (ptc != null) {
                String id = twt.getId() + "_leg" + legNumber + PHASE_TABLE_SUFFIX;
                writePhaseTapChanger(formatter, id, zb, leg.getX(), ptc);
            }
        }
    }

    @Override
    public void writeRtcToFormatter(TableFormatter formatter) {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            twt.getOptionalRatioTapChanger().ifPresent(rtc -> writeRatioTapChanger(formatter, twt, rtc, ""));
        }
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            twt.getLeg1()
                .getOptionalRatioTapChanger()
                .ifPresent(rtc -> writeRatioTapChanger(formatter, twt, rtc, AmplConstants.LEG1_SUFFIX));
            twt.getLeg2()
                .getOptionalRatioTapChanger()
                .ifPresent(rtc -> writeRatioTapChanger(formatter, twt, rtc, AmplConstants.LEG2_SUFFIX));
            twt.getLeg3()
                .getOptionalRatioTapChanger()
                .ifPresent(rtc -> writeRatioTapChanger(formatter, twt, rtc, AmplConstants.LEG3_SUFFIX));
        }
    }

    @Override
    public void writePtcToFormatter(TableFormatter formatter) {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            twt.getOptionalPhaseTapChanger().ifPresent(ptc -> writePhaseTapChanger(formatter, twt, ptc, ""));
        }
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            twt.getLeg1()
                .getOptionalPhaseTapChanger()
                .ifPresent(ptc -> writePhaseTapChanger(formatter, twt, ptc, AmplConstants.LEG1_SUFFIX));
            twt.getLeg2()
                .getOptionalPhaseTapChanger()
                .ifPresent(ptc -> writePhaseTapChanger(formatter, twt, ptc, AmplConstants.LEG2_SUFFIX));
            twt.getLeg3()
                .getOptionalPhaseTapChanger()
                .ifPresent(ptc -> writePhaseTapChanger(formatter, twt, ptc, AmplConstants.LEG3_SUFFIX));
        }
    }

    @Override
    public void writeCurrentLimits(TableFormatter formatter) throws IOException {
        writeBranchCurrentLimits(formatter);
        writeThreeWindingsTransformerCurrentLimits(formatter);
        writeDanglingLineCurrentLimits(formatter);
    }

    @Override
    public void writeHvdcToFormatter(TableFormatter formatter, HvdcLine hvdcLine) throws IOException {
        String id = hvdcLine.getId();
        int num = mapper.getInt(AmplSubset.HVDC_LINE, id);
        HvdcConverterStation.HvdcType type = hvdcLine.getConverterStation1().getHvdcType();
        AmplSubset subset = type.equals(
            HvdcConverterStation.HvdcType.VSC) ? AmplSubset.VSC_CONVERTER_STATION : AmplSubset.LCC_CONVERTER_STATION;
        formatter.writeCell(variantIndex)
            .writeCell(num)
            .writeCell(type.equals(HvdcConverterStation.HvdcType.VSC) ? 1 : 2)
            .writeCell(mapper.getInt(subset, hvdcLine.getConverterStation1().getId()))
            .writeCell(mapper.getInt(subset, hvdcLine.getConverterStation2().getId()))
            .writeCell(hvdcLine.getR())
            .writeCell(hvdcLine.getNominalV())
            .writeCell(hvdcLine.getConvertersMode().name())
            .writeCell(hvdcLine.getActivePowerSetpoint())
            .writeCell(hvdcLine.getMaxP())
            .writeCell(faultNum)
            .writeCell(actionNum)
            .writeCell(id)
            .writeCell(hvdcLine.getNameOrId());
    }

    @Override
    public void writeLccConverterStationToFormatter(TableFormatter formatter,
                                                    LccConverterStation lccStation) throws IOException {
        Terminal t = lccStation.getTerminal();
        int busNum = AmplUtil.getBusNum(mapper, t);
        int conBusNum = AmplUtil.getConnectableBusNum(mapper, t);

        int vlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, t.getVoltageLevel().getId());

        int num = mapper.getInt(AmplSubset.LCC_CONVERTER_STATION, lccStation.getId());

        formatter.writeCell(variantIndex)
            .writeCell(num)
            .writeCell(busNum)
            .writeCell(conBusNum != -1 ? conBusNum : busNum)
            .writeCell(vlNum)
            .writeCell(lccStation.getLossFactor())
            .writeCell(lccStation.getPowerFactor())
            .writeCell(faultNum)
            .writeCell(actionNum)
            .writeCell(lccStation.getId())
            .writeCell(lccStation.getNameOrId())
            .writeCell(t.getP())
            .writeCell(t.getQ());
    }

    @Override
    public void writeVscConverterStationToFormatter(TableFormatter formatter,
                                                    VscConverterStation vscStation) throws IOException {
        String id = vscStation.getId();
        Terminal t = vscStation.getTerminal();
        int busNum = AmplUtil.getBusNum(mapper, t);
        int conBusNum = AmplUtil.getConnectableBusNum(mapper, t);
        Map<String, HvdcLine> lineMap = getHvdcLinesMap();
        double maxP = lineMap.get(id) != null ? lineMap.get(id).getMaxP() : Double.NaN;

        int vlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, t.getVoltageLevel().getId());
        double vlSet = vscStation.getVoltageSetpoint();
        double vb = t.getVoltageLevel().getNominalV();
        double minP = -maxP;

        int num = mapper.getInt(AmplSubset.VSC_CONVERTER_STATION, vscStation.getId());
        formatter.writeCell(variantIndex)
            .writeCell(num)
            .writeCell(busNum)
            .writeCell(conBusNum != -1 ? conBusNum : busNum)
            .writeCell(vlNum)
            .writeCell(minP)
            .writeCell(maxP)
            .writeCell(vscStation.getReactiveLimits().getMinQ(maxP))
            .writeCell(vscStation.getReactiveLimits().getMinQ(0))
            .writeCell(vscStation.getReactiveLimits().getMinQ(minP))
            .writeCell(vscStation.getReactiveLimits().getMaxQ(maxP))
            .writeCell(vscStation.getReactiveLimits().getMaxQ(0))
            .writeCell(vscStation.getReactiveLimits().getMaxQ(minP))
            .writeCell(vscStation.isVoltageRegulatorOn())
            .writeCell(vlSet / vb)
            .writeCell(vscStation.getReactivePowerSetpoint())
            .writeCell(vscStation.getLossFactor())
            .writeCell(faultNum)
            .writeCell(actionNum)
            .writeCell(vscStation.getId())
            .writeCell(vscStation.getNameOrId())
            .writeCell(t.getP())
            .writeCell(t.getQ());
    }

    private void writeBranchCurrentLimits(TableFormatter formatter) throws IOException {
        for (Branch<?> branch : network.getBranches()) {
            String branchId = branch.getId();
            Optional<CurrentLimits> currentLimits1 = branch.getCurrentLimits1();
            if (currentLimits1.isPresent()) {
                writeTemporaryCurrentLimits(currentLimits1.get(), formatter, branchId, true, "_1_");
            }
            Optional<CurrentLimits> currentLimits2 = branch.getCurrentLimits2();
            if (currentLimits2.isPresent()) {
                writeTemporaryCurrentLimits(currentLimits2.get(), formatter, branchId, false, "_2_");
            }
        }
    }

    private void writeThreeWindingsTransformerCurrentLimits(TableFormatter formatter) throws IOException {
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            for (ThreeSides side : ThreeSides.values()) {
                Optional<CurrentLimits> currentLimits = twt.getLeg(side).getCurrentLimits();
                if (currentLimits.isPresent()) {
                    String branchId = twt.getId() + AmplUtil.getLegSuffix(side);
                    writeTemporaryCurrentLimits(currentLimits.get(), formatter, branchId, true, "");
                }
            }
        }
    }

    private void writeDanglingLineCurrentLimits(TableFormatter formatter) throws IOException {
        for (DanglingLine dl : network.getDanglingLines(DanglingLineFilter.UNPAIRED)) {
            String branchId = dl.getId();
            Optional<CurrentLimits> currentLimits = dl.getCurrentLimits();
            if (currentLimits.isPresent()) {
                writeTemporaryCurrentLimits(currentLimits.get(), formatter, branchId, true, "");
            }
        }
    }

    private void writePhaseTapChanger(TableFormatter formatter, Identifiable<?> twt, PhaseTapChanger ptc, String leg) {
        try {
            String ptcId = twt.getId() + leg;
            String tcsId = twt.getId() + leg + PHASE_TABLE_SUFFIX;
            int ptcNum = mapper.getInt(AmplSubset.PHASE_TAP_CHANGER, ptcId);
            int tcsNum = mapper.getInt(AmplSubset.TAP_CHANGER_TABLE, tcsId);
            formatter.writeCell(variantIndex)
                .writeCell(ptcNum)
                .writeCell(ptc.getTapPosition() - ptc.getLowTapPosition() + 1)
                .writeCell(tcsNum)
                .writeCell(faultNum)
                .writeCell(actionNum)
                .writeCell(ptcId);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeRatioTapChanger(TableFormatter formatter, Identifiable<?> twt, RatioTapChanger rtc, String leg) {
        try {
            String rtcId = twt.getId() + leg;
            String tcsId = twt.getId() + leg + RATIO_TABLE_SUFFIX;
            int rtcNum = mapper.getInt(AmplSubset.RATIO_TAP_CHANGER, rtcId);
            int tcsNum = mapper.getInt(AmplSubset.TAP_CHANGER_TABLE, tcsId);
            formatter.writeCell(variantIndex)
                .writeCell(rtcNum)
                .writeCell(rtc.getTapPosition() - rtc.getLowTapPosition() + 1)
                .writeCell(tcsNum)
                .writeCell(rtc.hasLoadTapChangingCapabilities() && rtc.isRegulating());
            if (config.isExportRatioTapChangerVoltageTarget()) {
                formatter.writeCell(rtc.getTargetV());
            }
            formatter.writeCell(faultNum)
                .writeCell(actionNum)
                .writeCell(rtcId);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeBusesColumnsToFormatter(TableFormatter formatter, Bus b) throws IOException {
        int ccNum = ConnectedComponents.getCcNum(b);
        String id = b.getId();
        VoltageLevel vl = b.getVoltageLevel();
        int num = mapper.getInt(AmplSubset.BUS, id);
        int vlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl.getId());
        double nomV = vl.getNominalV();
        double v = b.getV() / nomV;
        double theta = Math.toRadians(b.getAngle());
        TableFormatterHelper formatterHelper = new TableFormatterHelper(formatter);
        formatterHelper.addCell(variantIndex)
            .addCell(num)
            .addCell(vlNum)
            .addCell(ccNum)
            .addCell(v)
            .addCell(theta)
            .addCell(b.getP())
            .addCell(b.getQ())
            .addCell(faultNum)
            .addCell(actionNum)
            .addCell(id);

        // Add cells if necessary
        addAdditionalCellsBusesColumns(formatterHelper, b);

        // Write the cells
        formatterHelper.write();
    }

    public void addAdditionalCellsBusesColumns(TableFormatterHelper formatterHelper, Bus b) {
        // Nothing to do here
    }

    @Override
    public void writeThreeWindingsTranformersMiddleBusesColumnsToFormatter(TableFormatter formatter,
                                                                           ThreeWindingsTransformer twt,
                                                                           int middleCcNum) throws IOException {
        String middleBusId = AmplUtil.getThreeWindingsTransformerMiddleBusId(twt);
        String middleVlId = AmplUtil.getThreeWindingsTransformerMiddleVoltageLevelId(twt);
        int middleBusNum = mapper.getInt(AmplSubset.BUS, middleBusId);
        int middleVlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, middleVlId);

        double v = twt.getProperty("v") == null ? Double.NaN :
            Double.parseDouble(twt.getProperty("v")) / twt.getRatedU0();
        double angle = twt.getProperty("angle") == null ? Double.NaN :
            Math.toRadians(Double.parseDouble(twt.getProperty("angle")));

        TableFormatterHelper formatterHelper = new TableFormatterHelper(formatter);
        formatterHelper.addCell(variantIndex)
            .addCell(middleBusNum)
            .addCell(middleVlNum)
            .addCell(middleCcNum)
            .addCell(v)
            .addCell(angle)
            .addCell(0.0)
            .addCell(0.0)
            .addCell(faultNum)
            .addCell(actionNum)
            .addCell(middleBusId);

        // Add cells if necessary
        addAdditionalCellsThreeWindingsTranformersMiddleBusesColumns(formatterHelper, twt, middleCcNum);

        // Write the cells
        formatterHelper.write();
    }

    public void addAdditionalCellsThreeWindingsTranformersMiddleBusesColumns(TableFormatterHelper formatterHelper,
                                                                             ThreeWindingsTransformer twt,
                                                                             int middleCcNum) {
        // Nothing to do here
    }

    @Override
    public void writeDanglingLineMiddleBusesToFormatter(TableFormatter formatter, DanglingLine dl,
                                                        int middleCcNum) throws IOException {
        Terminal t = dl.getTerminal();
        Bus b = AmplUtil.getBus(dl.getTerminal());
        String middleBusId = AmplUtil.getDanglingLineMiddleBusId(dl);
        String middleVlId = AmplUtil.getDanglingLineMiddleVoltageLevelId(dl);
        int middleBusNum = mapper.getInt(AmplSubset.BUS, middleBusId);
        int middleVlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, middleVlId);
        SV sv = new SV(t.getP(), t.getQ(), b != null ? b.getV() : Double.NaN, b != null ? b.getAngle() : Double.NaN,
            TwoSides.ONE).otherSide(
            dl, true);
        double nomV = t.getVoltageLevel().getNominalV();
        double v = sv.getU() / nomV;
        double theta = Math.toRadians(sv.getA());

        TableFormatterHelper formatterHelper = new TableFormatterHelper(formatter);
        formatterHelper.addCell(variantIndex)
            .addCell(middleBusNum)
            .addCell(middleVlNum)
            .addCell(middleCcNum)
            .addCell(v)
            .addCell(theta)
            .addCell(0.0) // 0 MW injected at dangling line internal bus
            .addCell(0.0) // 0 MVar injected at dangling line internal bus
            .addCell(faultNum)
            .addCell(actionNum)
            .addCell(middleBusId);

        // Add cells if necessary
        addAdditionalCellsDanglingLineMiddleBuses(formatterHelper, dl, middleCcNum);

        // Write the cells
        formatterHelper.write();
    }

    public void addAdditionalCellsDanglingLineMiddleBuses(TableFormatterHelper formatterHelper, DanglingLine dl,
                                                          int middleCcNum) {
        // Nothing to do here
    }

    @Override
    public void writeTieLineMiddleBusesToFormatter(TableFormatter formatter, TieLine tieLine,
                                                   int xNodeCcNum) throws IOException {
        String xNodeBusId = AmplUtil.getXnodeBusId(tieLine);
        int xNodeBusNum = mapper.getInt(AmplSubset.BUS, xNodeBusId);
        int xNodeVlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, AmplUtil.getXnodeVoltageLevelId(tieLine));

        TableFormatterHelper formatterHelper = new TableFormatterHelper(formatter);
        formatterHelper.addCell(variantIndex)
            .addCell(xNodeBusNum)
            .addCell(xNodeVlNum)
            .addCell(xNodeCcNum)
            .addCell(Float.NaN)
            .addCell(Double.NaN)
            .addCell(0.0)
            .addCell(0.0)
            .addCell(faultNum)
            .addCell(actionNum)
            .addCell(xNodeBusId);

        // Add cells if necessary
        addAdditionalCellsTieLineMiddleBuses(formatterHelper, tieLine, xNodeCcNum);

        // Write the cells
        formatterHelper.write();
    }

    public void addAdditionalCellsTieLineMiddleBuses(TableFormatterHelper formatterHelper, TieLine tieLine,
                                                     int xNodeCcNum) {
        // Nothing to do here
    }

    @Override
    public void writeLinesToFormatter(TableFormatter formatter, Line l) throws IOException {
        String id = l.getId();
        Terminal t1 = l.getTerminal1();
        Terminal t2 = l.getTerminal2();
        Bus bus1 = AmplUtil.getBus(t1);
        Bus bus2 = AmplUtil.getBus(t2);
        int bus1Num = getBusNum(bus1);
        int bus2Num = getBusNum(bus2);
        VoltageLevel vl1 = t1.getVoltageLevel();
        VoltageLevel vl2 = t2.getVoltageLevel();
        int num = mapper.getInt(AmplSubset.BRANCH, id);
        int vl1Num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl1.getId());
        int vl2Num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl2.getId());
        double zSquare = l.getR() * l.getR() + l.getX() * l.getX();
        double r;
        double x;
        double g1;
        double g2;
        double b1;
        double b2;
        if (zSquare == 0) {
            r = 0;
            x = 0;
            g1 = 0;
            g2 = 0;
            b1 = 0;
            b2 = 0;
        } else {
            double g = l.getR() / zSquare;
            double b = -l.getX() / zSquare;
            double vb1 = vl1.getNominalV();
            double vb2 = vl2.getNominalV();
            double zb = vb1 * vb2 / AmplConstants.SB;
            r = l.getR() / zb;
            x = l.getX() / zb;
            g1 = (l.getG1() * vb1 * vb1 + g * vb1 * (vb1 - vb2)) / AmplConstants.SB;
            g2 = (l.getG2() * vb2 * vb2 + g * vb2 * (vb2 - vb1)) / AmplConstants.SB;
            b1 = (l.getB1() * vb1 * vb1 + b * vb1 * (vb1 - vb2)) / AmplConstants.SB;
            b2 = (l.getB2() * vb2 * vb2 + b * vb2 * (vb2 - vb1)) / AmplConstants.SB;
        }

        formatter.writeCell(variantIndex)
            .writeCell(num)
            .writeCell(bus1Num)
            .writeCell(bus2Num)
            .writeCell(-1)
            .writeCell(vl1Num)
            .writeCell(vl2Num)
            .writeCell(r)
            .writeCell(x)
            .writeCell(g1)
            .writeCell(g2)
            .writeCell(b1)
            .writeCell(b2)
            .writeCell(1f) // constant ratio
            .writeCell(-1) // no ratio tap changer
            .writeCell(-1) // no phase tap changer
            .writeCell(t1.getP())
            .writeCell(t2.getP())
            .writeCell(t1.getQ())
            .writeCell(t2.getQ())
            .writeCell(getPermanentLimit(l.getCurrentLimits1().orElse(null)))
            .writeCell(getPermanentLimit(l.getCurrentLimits2().orElse(null)))
            .writeCell(false)
            .writeCell(faultNum)
            .writeCell(actionNum)
            .writeCell(id)
            .writeCell(l.getNameOrId());
    }

    @Override
    public void writeTieLineToFormatter(TableFormatter formatter, TieLine l) throws IOException {
        Terminal t1 = l.getDanglingLine1().getTerminal();
        Terminal t2 = l.getDanglingLine2().getTerminal();
        Bus bus1 = AmplUtil.getBus(t1);
        Bus bus2 = AmplUtil.getBus(t2);
        int bus1Num = getBusNum(bus1);
        int bus2Num = getBusNum(bus2);
        VoltageLevel vl1 = t1.getVoltageLevel();
        VoltageLevel vl2 = t2.getVoltageLevel();
        String id = l.getId();
        int num = mapper.getInt(AmplSubset.BRANCH, id);
        int vl1Num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl1.getId());
        int vl2Num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl2.getId());

        double vb = vl1.getNominalV();
        double zb = vb * vb / AmplConstants.SB;

        boolean merged = !config.isExportXNodes();
        if (config.isExportXNodes()) {
            String dl1Id = l.getDanglingLine1().getId();
            String dl2Id = l.getDanglingLine2().getId();
            int dl1Num = mapper.getInt(AmplSubset.BRANCH, dl1Id);
            int dl2Num = mapper.getInt(AmplSubset.BRANCH, dl2Id);
            String xNodeBusId = AmplUtil.getXnodeBusId(l);
            String xnodeVoltageLevelId = AmplUtil.getXnodeVoltageLevelId(l);
            int xNodeBusNum = mapper.getInt(AmplSubset.BUS, xNodeBusId);
            int xNodeVoltageLevelNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, xnodeVoltageLevelId);

            formatter.writeCell(variantIndex)
                .writeCell(dl1Num)
                .writeCell(bus1Num)
                .writeCell(xNodeBusNum)
                .writeCell(-1)
                .writeCell(vl1Num)
                .writeCell(xNodeVoltageLevelNum)
                .writeCell(l.getDanglingLine1().getR() / zb)
                .writeCell(l.getDanglingLine1().getX() / zb)
                .writeCell(l.getDanglingLine1().getG() * zb / 2)
                .writeCell(l.getDanglingLine1().getG() * zb / 2)
                .writeCell(l.getDanglingLine1().getB() * zb / 2)
                .writeCell(l.getDanglingLine1().getB() * zb / 2)
                .writeCell(1f) // constant ratio
                .writeCell(-1) // no ratio tap changer
                .writeCell(-1) // no phase tap changer
                .writeCell(t1.getP())
                .writeCell(l.getDanglingLine1().getBoundary().getP()) // xnode node flow side 1
                .writeCell(t1.getQ())
                .writeCell(l.getDanglingLine1().getBoundary().getQ()) // xnode node flow side 1
                .writeCell(getPermanentLimit(l.getDanglingLine1().getCurrentLimits().orElse(null)))
                .writeCell(Float.NaN)
                .writeCell(merged)
                .writeCell(faultNum)
                .writeCell(actionNum)
                .writeCell(dl1Id)
                .writeCell(l.getDanglingLine1().getNameOrId());
            formatter.writeCell(variantIndex)
                .writeCell(dl2Num)
                .writeCell(xNodeBusNum)
                .writeCell(bus2Num)
                .writeCell(-1)
                .writeCell(xNodeVoltageLevelNum)
                .writeCell(vl2Num)
                .writeCell(l.getDanglingLine2().getR() / zb)
                .writeCell(l.getDanglingLine2().getX() / zb)
                .writeCell(l.getDanglingLine1().getG() * zb / 2)
                .writeCell(l.getDanglingLine1().getG() * zb / 2)
                .writeCell(l.getDanglingLine1().getB() * zb / 2)
                .writeCell(l.getDanglingLine1().getB() * zb / 2)
                .writeCell(1f) // constant ratio
                .writeCell(-1) // no ratio tap changer
                .writeCell(-1) // no phase tap changer
                .writeCell(l.getDanglingLine2().getBoundary().getP()) // xnode node flow side 2
                .writeCell(t2.getP())
                .writeCell(l.getDanglingLine2().getBoundary().getQ()) // xnode node flow side 2
                .writeCell(t2.getQ())
                .writeCell(Float.NaN)
                .writeCell(getPermanentLimit(l.getDanglingLine2().getCurrentLimits().orElse(null)))
                .writeCell(merged)
                .writeCell(faultNum)
                .writeCell(actionNum)
                .writeCell(dl2Id)
                .writeCell(l.getDanglingLine2().getNameOrId());
        } else {
            formatter.writeCell(variantIndex)
                .writeCell(num)
                .writeCell(bus1Num)
                .writeCell(bus2Num)
                .writeCell(-1)
                .writeCell(vl1Num)
                .writeCell(vl2Num)
                .writeCell(l.getR() / zb)
                .writeCell(l.getX() / zb)
                .writeCell(l.getG1() * zb)
                .writeCell(l.getG2() * zb)
                .writeCell(l.getB1() * zb)
                .writeCell(l.getB2() * zb)
                .writeCell(1f) // constant ratio
                .writeCell(-1) // no ratio tap changer
                .writeCell(-1) // no phase tap changer
                .writeCell(t1.getP())
                .writeCell(t2.getP())
                .writeCell(t1.getQ())
                .writeCell(t2.getQ())
                .writeCell(getPermanentLimit(l.getDanglingLine1().getCurrentLimits().orElse(null)))
                .writeCell(getPermanentLimit(l.getDanglingLine2().getCurrentLimits().orElse(null)))
                .writeCell(merged)
                .writeCell(faultNum)
                .writeCell(actionNum)
                .writeCell(id)
                .writeCell(l.getNameOrId());
        }
    }

    @Override
    public void writeDanglingLineToFormatter(TableFormatter formatter, DanglingLine dl) throws IOException {
        String id = dl.getId();
        Terminal t = dl.getTerminal();
        VoltageLevel vl = t.getVoltageLevel();
        Bus bus = AmplUtil.getBus(t);
        int busNum = getBusNum(bus);
        String middleBusId = AmplUtil.getDanglingLineMiddleBusId(dl);
        String middleVlId = AmplUtil.getDanglingLineMiddleVoltageLevelId(dl);
        int middleBusNum = mapper.getInt(AmplSubset.BUS, middleBusId);
        int num = mapper.getInt(AmplSubset.BRANCH, id);
        int vlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl.getId());
        int middleVlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, middleVlId);
        double vb = vl.getNominalV();
        double zb = vb * vb / AmplConstants.SB;
        double p1 = t.getP();
        double q1 = t.getQ();
        SV sv = new SV(p1, q1, bus != null ? bus.getV() : Double.NaN, bus != null ? bus.getAngle() : Double.NaN,
            TwoSides.ONE).otherSide(
            dl, true);
        double p2 = sv.getP();
        double q2 = sv.getQ();
        double patl = getPermanentLimit(dl.getCurrentLimits().orElse(null));
        formatter.writeCell(variantIndex)
            .writeCell(num)
            .writeCell(busNum)
            .writeCell(middleBusNum)
            .writeCell(-1)
            .writeCell(vlNum)
            .writeCell(middleVlNum)
            .writeCell(dl.getR() / zb)
            .writeCell(dl.getX() / zb)
            .writeCell(dl.getG() / 2 * zb)
            .writeCell(dl.getG() / 2 * zb)
            .writeCell(dl.getB() / 2 * zb)
            .writeCell(dl.getB() / 2 * zb)
            .writeCell(1f)  // constant ratio
            .writeCell(-1) // no ratio tap changer
            .writeCell(-1) // no phase tap changer
            .writeCell(p1)
            .writeCell(p2)
            .writeCell(q1)
            .writeCell(q2)
            .writeCell(patl)
            .writeCell(patl)
            .writeCell(false)
            .writeCell(faultNum)
            .writeCell(actionNum)
            .writeCell(id)
            .writeCell(dl.getNameOrId());
    }

    @Override
    public void writeTwoWindingsTranformerToFormatter(TableFormatter formatter,
                                                      TwoWindingsTransformer twt) throws IOException {
        String id = twt.getId();
        Terminal t1 = twt.getTerminal1();
        Terminal t2 = twt.getTerminal2();
        Bus bus1 = AmplUtil.getBus(t1);
        Bus bus2 = AmplUtil.getBus(t2);
        VoltageLevel vl1 = t1.getVoltageLevel();
        VoltageLevel vl2 = t2.getVoltageLevel();
        int bus1Num = getBusNum(bus1);
        int bus2Num = getBusNum(bus2);
        int num = mapper.getInt(AmplSubset.BRANCH, id);
        int vl1Num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl1.getId());
        int vl2Num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl2.getId());
        double vb1 = vl1.getNominalV();
        double vb2 = vl2.getNominalV();
        double zb2 = vb2 * vb2 / AmplConstants.SB;
        double r = twt.getR() / zb2;
        double x = twt.getX() / zb2;
        double g1;
        double g2;
        double b1;
        double b2;
        if (config.isTwtSplitShuntAdmittance()) {
            g1 = twt.getG() * zb2 / 2;
            g2 = g1;
            b1 = twt.getB() * zb2 / 2;
            b2 = b1;
        } else {
            g1 = twt.getG() * zb2;
            g2 = 0;
            b1 = twt.getB() * zb2;
            b2 = 0;
        }

        double ratedU1 = twt.getRatedU1();
        double ratedU2 = twt.getRatedU2();
        double ratio = ratedU2 / vb2 / (ratedU1 / vb1);
        RatioTapChanger rtc = twt.getRatioTapChanger();
        PhaseTapChanger ptc = twt.getPhaseTapChanger();
        int rtcNum = rtc != null ? mapper.getInt(AmplSubset.RATIO_TAP_CHANGER, twt.getId()) : -1;
        int ptcNum = ptc != null ? mapper.getInt(AmplSubset.PHASE_TAP_CHANGER, twt.getId()) : -1;
        formatter.writeCell(variantIndex)
            .writeCell(num)
            .writeCell(bus1Num)
            .writeCell(bus2Num)
            .writeCell(-1)
            .writeCell(vl1Num)
            .writeCell(vl2Num)
            .writeCell(r)
            .writeCell(x)
            .writeCell(g1)
            .writeCell(g2)
            .writeCell(b1)
            .writeCell(b2)
            .writeCell(ratio)
            .writeCell(rtcNum)
            .writeCell(ptcNum)
            .writeCell(t1.getP())
            .writeCell(t2.getP())
            .writeCell(t1.getQ())
            .writeCell(t2.getQ())
            .writeCell(getPermanentLimit(twt.getCurrentLimits1().orElse(null)))
            .writeCell(getPermanentLimit(twt.getCurrentLimits2().orElse(null)))
            .writeCell(false) // TODO to update
            .writeCell(faultNum)
            .writeCell(actionNum)
            .writeCell(id)
            .writeCell(twt.getNameOrId());
    }

    @Override
    public void writeThreeWindingsTransformerLegToFormatter(TableFormatter formatter, ThreeWindingsTransformer twt,
                                                            int middleBusNum, int middleVlNum,
                                                            ThreeSides legSide) throws IOException {
        ThreeWindingsTransformer.Leg twtLeg = twt.getLeg(legSide);
        Terminal terminal = twtLeg.getTerminal();
        Bus bus = AmplUtil.getBus(terminal);
        VoltageLevel vl = terminal.getVoltageLevel();
        String id = twt.getId() + AmplUtil.getLegSuffix(legSide);
        int num3wt = mapper.getInt(AmplSubset.THREE_WINDINGS_TRANSFO, twt.getId());
        int num = mapper.getInt(AmplSubset.BRANCH, id);
        int legVlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl.getId());
        int legBusNum = getBusNum(bus);
        double vb = vl.getNominalV();
        double ratedU0 = twt.getRatedU0();
        double ratedU = twtLeg.getRatedU();

        double zb0 = ratedU0 * ratedU0 / AmplConstants.SB;
        double r = twtLeg.getR() / zb0;
        double x = twtLeg.getX() / zb0;
        double g = twtLeg.getG() * zb0;
        double b = twtLeg.getB() * zb0;
        double ratio = vb / ratedU;

        RatioTapChanger rtc1 = twtLeg.getRatioTapChanger();
        PhaseTapChanger ptc1 = twtLeg.getPhaseTapChanger();
        int rtc1Num = rtc1 != null ? mapper.getInt(AmplSubset.RATIO_TAP_CHANGER, id) : -1;
        int ptc1Num = ptc1 != null ? mapper.getInt(AmplSubset.PHASE_TAP_CHANGER, id) : -1;

        formatter.writeCell(variantIndex)
            .writeCell(num)
            .writeCell(legBusNum)
            .writeCell(middleBusNum)
            .writeCell(num3wt)
            .writeCell(legVlNum)
            .writeCell(middleVlNum)
            .writeCell(r)
            .writeCell(x)
            .writeCell(g)
            .writeCell(0.0)
            .writeCell(b)
            .writeCell(0.0)
            .writeCell(ratio)
            .writeCell(rtc1Num)
            .writeCell(ptc1Num)
            .writeCell(terminal.getP())
            .writeCell(Double.NaN)
            .writeCell(terminal.getQ())
            .writeCell(Double.NaN)
            .writeCell(getPermanentLimit(twtLeg.getCurrentLimits().orElse(null)))
            .writeCell(Double.NaN)
            .writeCell(false)
            .writeCell(faultNum)
            .writeCell(actionNum)
            .writeCell(id)
            .writeCell("");
    }

    @Override
    public void writeTieLineVoltageLevelToFormatter(TableFormatter formatter, TieLine tieLine) throws IOException {
        String vlId = AmplUtil.getXnodeVoltageLevelId(tieLine);
        int num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vlId);
        formatter.writeCell(variantIndex)
            .writeCell(num)
            .writeCell("")
            .writeCell(0)
            .writeCell(tieLine.getDanglingLine1().getTerminal().getVoltageLevel().getNominalV())
            .writeCell(Float.NaN)
            .writeCell(Float.NaN)
            .writeCell(faultNum)
            .writeCell(actionNum)
            .writeCell(XNODE_COUNTRY_NAME)
            .writeCell(AmplUtil.getXnodeBusId(tieLine) + "_voltageLevel")
            .writeCell("");
    }

    @Override
    public void writeVoltageLevelToFormatter(TableFormatter formatter, VoltageLevel vl) throws IOException {
        int num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl.getId());
        double nomV = vl.getNominalV();
        double minV = vl.getLowVoltageLimit() / nomV;
        double maxV = vl.getHighVoltageLimit() / nomV;
        formatter.writeCell(variantIndex)
            .writeCell(num)
            .writeCell("")
            .writeCell(0)
            .writeCell(nomV)
            .writeCell(minV)
            .writeCell(maxV)
            .writeCell(faultNum)
            .writeCell(actionNum)
            .writeCell(vl.getSubstation().flatMap(Substation::getCountry).map(Enum::toString).orElse(""))
            .writeCell(vl.getId())
            .writeCell(vl.getNameOrId());
    }

    @Override
    public void writeDanglingLineLoadToFormatter(TableFormatter formatter, DanglingLine dl) throws IOException {
        String middleBusId = AmplUtil.getDanglingLineMiddleBusId(dl);
        String id = dl.getId();
        int num = mapper.getInt(AmplSubset.LOAD, id);
        int busNum = mapper.getInt(AmplSubset.BUS, middleBusId);
        String middleVlId = AmplUtil.getDanglingLineMiddleVoltageLevelId(dl);
        int vlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, middleVlId);
        formatter.writeCell(variantIndex)
            .writeCell(num)
            .writeCell(busNum)
            .writeCell(vlNum)
            .writeCell(dl.getP0())
            .writeCell(dl.getQ0())
            .writeCell(faultNum)
            .writeCell(actionNum)
            .writeCell(dl.getId() + "_load")
            .writeCell("")
            .writeCell(dl.getTerminal().getP())
            .writeCell(dl.getTerminal().getQ());
    }

    @Override
    public void writeLoadtoFormatter(TableFormatter formatter, Load l) throws IOException {
        String id = l.getId();
        Terminal t = l.getTerminal();
        Bus bus = AmplUtil.getBus(t);
        int busNum = bus == null ? -1 : mapper.getInt(AmplSubset.BUS, bus.getId());
        int num = mapper.getInt(AmplSubset.LOAD, id);
        int vlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, t.getVoltageLevel().getId());
        formatter.writeCell(variantIndex)
            .writeCell(num)
            .writeCell(busNum)
            .writeCell(vlNum)
            .writeCell(l.getP0())
            .writeCell(l.getQ0())
            .writeCell(faultNum)
            .writeCell(actionNum)
            .writeCell(id)
            .writeCell(l.getNameOrId())
            .writeCell(t.getP())
            .writeCell(t.getQ());
    }

    @Override
    public void writeShuntCompensatorToFormatter(TableFormatter formatter, ShuntCompensator sc) throws IOException {

        String id = sc.getId();
        Terminal t = sc.getTerminal();
        int num = mapper.getInt(AmplSubset.SHUNT, id);
        int vlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, t.getVoltageLevel().getId());
        int busNum = AmplUtil.getBusNum(mapper, t);
        int conBusNum = AmplUtil.getConnectableBusNum(mapper, t);

        double vb = t.getVoltageLevel().getNominalV();
        double zb = vb * vb / AmplConstants.SB;
        double b1 = 0;
        double b2;
        int points = 0;
        int sectionCount = 1;
        if (sc.getModelType() == ShuntCompensatorModelType.NON_LINEAR) {
            // TODO non linear shunt has to be converted as multiple sections shunt.
            if (sc.getSectionCount() > 1) {
                b1 = sc.getB(sc.getSectionCount() - 1) * zb;
            }
            b2 = sc.getB() * zb;
        } else {
            b2 = sc.getModel(ShuntCompensatorLinearModel.class).getBPerSection() * sc.getMaximumSectionCount() * zb;
            points = sc.getMaximumSectionCount() < 1 ? 0 : sc.getMaximumSectionCount() - 1;
            sectionCount = sc.getSectionCount();
        }
        double b = sc.getB() * zb;
        double minB = Math.min(b1, b2);
        double maxB = Math.max(b1, b2);
        formatter.writeCell(variantIndex)
            .writeCell(num)
            .writeCell(busNum)
            .writeCell(conBusNum != -1 ? conBusNum : busNum)
            .writeCell(vlNum)
            .writeCell(minB)
            .writeCell(maxB)
            .writeCell(points)
            .writeCell(b)
            .writeCell(faultNum)
            .writeCell(actionNum)
            .writeCell(id)
            .writeCell(sc.getNameOrId())
            .writeCell(t.getP())
            .writeCell(t.getQ())
            .writeCell(sectionCount);
    }

    @Override
    public void writeGeneratorToFormatter(TableFormatter formatter, Generator gen) throws IOException {
        String id = gen.getId();
        Terminal t = gen.getTerminal();
        int num = mapper.getInt(AmplSubset.GENERATOR, id);
        int vlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, t.getVoltageLevel().getId());
        int busNum = AmplUtil.getBusNum(mapper, t);
        int conBusNum = AmplUtil.getConnectableBusNum(mapper, t);
        double minP = gen.getMinP();
        double maxP = gen.getMaxP();
        double vb = gen.getRegulatingTerminal().getVoltageLevel().getNominalV();

        TableFormatterHelper formatterHelper = new TableFormatterHelper(formatter);
        formatterHelper.addCell(variantIndex)
            .addCell(num)
            .addCell(busNum)
            .addCell(conBusNum != -1 ? conBusNum : busNum)
            .addCell(vlNum)
            .addCell(minP)
            .addCell(maxP)
            .addCell(gen.getReactiveLimits().getMinQ(maxP))
            .addCell(gen.getReactiveLimits().getMinQ(0))
            .addCell(gen.getReactiveLimits().getMinQ(minP))
            .addCell(gen.getReactiveLimits().getMaxQ(maxP))
            .addCell(gen.getReactiveLimits().getMaxQ(0))
            .addCell(gen.getReactiveLimits().getMaxQ(minP))
            .addCell(gen.isVoltageRegulatorOn())
            .addCell(gen.getTargetV() / vb)
            .addCell(gen.getTargetP())
            .addCell(gen.getTargetQ())
            .addCell(faultNum)
            .addCell(actionNum)
            .addCell(id)
            .addCell(gen.getNameOrId())
            .addCell(t.getP())
            .addCell(t.getQ());

        // Add cells if necessary
        addAdditionalCellsGenerator(formatterHelper, gen);

        // Write the cells
        formatterHelper.write();
    }

    public void addAdditionalCellsGenerator(TableFormatterHelper formatterHelper, Generator gen) {
        // Nothing to do here
    }

    @Override
    public void writeBatteryToFormatter(TableFormatter formatter, Battery battery) throws IOException {
        String id = battery.getId();
        Terminal t = battery.getTerminal();
        int num = mapper.getInt(AmplSubset.BATTERY, id);
        int vlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, t.getVoltageLevel().getId());
        int busNum = AmplUtil.getBusNum(mapper, t);
        int conBusNum = AmplUtil.getConnectableBusNum(mapper, t);
        double minP = battery.getMinP();
        double maxP = battery.getMaxP();

        formatter.writeCell(variantIndex)
            .writeCell(num)
            .writeCell(busNum)
            .writeCell(conBusNum != -1 ? conBusNum : busNum)
            .writeCell(vlNum)
            .writeCell(battery.getTargetP())
            .writeCell(battery.getTargetQ())
            .writeCell(minP)
            .writeCell(maxP)
            .writeCell(battery.getReactiveLimits().getMinQ(maxP))
            .writeCell(battery.getReactiveLimits().getMinQ(0))
            .writeCell(battery.getReactiveLimits().getMinQ(minP))
            .writeCell(battery.getReactiveLimits().getMaxQ(maxP))
            .writeCell(battery.getReactiveLimits().getMaxQ(0))
            .writeCell(battery.getReactiveLimits().getMaxQ(minP))
            .writeCell(faultNum)
            .writeCell(actionNum)
            .writeCell(id)
            .writeCell(battery.getNameOrId())
            .writeCell(t.getP())
            .writeCell(t.getQ());
    }

    @Override
    public void writeStaticVarCompensatorToFormatter(TableFormatter formatter,
                                                     StaticVarCompensator svc) throws IOException {
        String id = svc.getId();
        int num = mapper.getInt(AmplSubset.STATIC_VAR_COMPENSATOR, id);

        Terminal t = svc.getTerminal();

        int busNum = AmplUtil.getBusNum(mapper, t);

        int conBusNum = AmplUtil.getConnectableBusNum(mapper, t);

        double vlSet = svc.getVoltageSetpoint();
        double vb = t.getVoltageLevel().getNominalV();
        double zb = vb * vb / AmplConstants.SB; // Base impedance

        int vlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, t.getVoltageLevel().getId());

        TableFormatterHelper formatterHelper = new TableFormatterHelper(formatter);
        formatterHelper.addCell(variantIndex)
            .addCell(num)
            .addCell(busNum)
            .addCell(conBusNum)
            .addCell(vlNum)
            .addCell(svc.getBmin() * zb)
            .addCell(svc.getBmax() * zb)
            .addCell(svc.getRegulationMode().equals(StaticVarCompensator.RegulationMode.VOLTAGE))
            .addCell(vlSet / vb)
            .addCell(svc.getReactivePowerSetpoint())
            .addCell(faultNum)
            .addCell(actionNum)
            .addCell(id)
            .addCell(svc.getNameOrId())
            .addCell(t.getP())
            .addCell(t.getQ());

        // Add cells if necessary
        addAdditionalCellsStaticVarCompensator(formatterHelper, svc);

        // Write the cells
        formatterHelper.write();
    }

    public void addAdditionalCellsStaticVarCompensator(TableFormatterHelper formatterHelper,
                                                       StaticVarCompensator svc) {
        // Nothing to do here
    }

    @Override
    public void writeDanglingLineVoltageLevelToFormatter(TableFormatter formatter,
                                                         DanglingLine dl) throws IOException {
        String vlId = AmplUtil.getDanglingLineMiddleVoltageLevelId(dl);
        int num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vlId);
        VoltageLevel vl = dl.getTerminal().getVoltageLevel();
        double nomV = vl.getNominalV();
        double minV = vl.getLowVoltageLimit() / nomV;
        double maxV = vl.getHighVoltageLimit() / nomV;

        TableFormatterHelper formatterHelper = new TableFormatterHelper(formatter);
        formatterHelper.addCell(variantIndex)
            .addCell(num)
            .addCell("")
            .addCell(0)
            .addCell(nomV)
            .addCell(minV)
            .addCell(maxV)
            .addCell(faultNum)
            .addCell(actionNum)
            .addCell(vl.getSubstation().flatMap(Substation::getCountry).map(Enum::toString).orElse(""))
            .addCell(dl.getId() + "_voltageLevel")
            .addCell("");

        // Add cells if necessary
        addAdditionalCellsDanglingLineVoltageLevel(formatterHelper, dl);

        // Write the cells
        formatterHelper.write();
    }

    public void addAdditionalCellsDanglingLineVoltageLevel(TableFormatterHelper formatterHelper,
                                                           DanglingLine dl) {
        // Nothing to do here
    }

    @Override
    public void writeThreeWindingsTransformerVoltageLevelToFormatter(TableFormatter formatter,
                                                                     ThreeWindingsTransformer twt) throws IOException {
        String vlId = AmplUtil.getThreeWindingsTransformerMiddleVoltageLevelId(twt);
        int num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vlId);

        TableFormatterHelper formatterHelper = new TableFormatterHelper(formatter);
        formatterHelper.addCell(variantIndex)
            .addCell(num)
            .addCell("")
            .addCell(0)
            .addCell(twt.getRatedU0())
            .addCell(Float.NaN)
            .addCell(Float.NaN)
            .addCell(faultNum)
            .addCell(actionNum)
            .addCell(twt.getLeg1()
                .getTerminal()
                .getVoltageLevel()
                .getSubstation()
                .flatMap(Substation::getCountry)
                .map(Enum::toString)
                .orElse(""))
            .addCell(vlId)
            .addCell("");

        // Add cells if necessary
        addAdditionalCellsThreeWindingsTransformerVoltageLevel(formatterHelper, twt);

        // Write the cells
        formatterHelper.write();
    }

    public void addAdditionalCellsThreeWindingsTransformerVoltageLevel(TableFormatterHelper formatterHelper,
                                                                       ThreeWindingsTransformer twt) {
        // Nothing to do here
    }

    private void writeRatioTapChanger(TableFormatter formatter, String id, double zb2, double reactance,
                                      RatioTapChanger rtc) throws IOException {
        int num = mapper.getInt(AmplSubset.TAP_CHANGER_TABLE, id);

        for (int position = rtc.getLowTapPosition(); position <= rtc.getHighTapPosition(); position++) {
            RatioTapChangerStep step = rtc.getStep(position);
            double x = reactance * (1 + step.getX() / 100) / zb2;
            TableFormatterHelper formatterHelper = new TableFormatterHelper(formatter);
            formatterHelper.addCell(variantIndex)
                .addCell(num)
                .addCell(position - rtc.getLowTapPosition() + 1)
                .addCell(step.getRho())
                .addCell(x)
                .addCell(0.0)
                .addCell(faultNum)
                .addCell(actionNum);

            // Add cells if necessary
            addAdditionalCellsRatioTapChangerStep(formatterHelper, id, zb2, reactance, rtc);

            // Write the cells
            formatterHelper.write();
        }
    }

    public void addAdditionalCellsRatioTapChangerStep(TableFormatterHelper formatterHelper,
                                                      String id, double zb2, double reactance,
                                                      RatioTapChanger rtc) {
        // Nothing to do here
    }

    private void writePhaseTapChanger(TableFormatter formatter, String id, double zb2, double reactance,
                                      PhaseTapChanger ptc) throws IOException {
        int num = mapper.getInt(AmplSubset.TAP_CHANGER_TABLE, id);

        for (int position = ptc.getLowTapPosition(); position <= ptc.getHighTapPosition(); position++) {
            PhaseTapChangerStep step = ptc.getStep(position);
            double x = reactance * (1 + step.getX() / 100) / zb2;
            TableFormatterHelper formatterHelper = new TableFormatterHelper(formatter);
            formatterHelper.addCell(variantIndex)
                .addCell(num)
                .addCell(position - ptc.getLowTapPosition() + 1)
                .addCell(step.getRho())
                .addCell(x)
                .addCell(Math.toRadians(step.getAlpha()))
                .addCell(faultNum)
                .addCell(actionNum);

            // Add cells if necessary
            addAdditionalCellsPhaseTapChangerStep(formatterHelper, id, zb2, reactance, ptc);

            // Write the cells
            formatterHelper.write();
        }
    }

    public void addAdditionalCellsPhaseTapChangerStep(TableFormatterHelper formatterHelper,
                                                      String id, double zb2, double reactance,
                                                      PhaseTapChanger ptc) {
        // Nothing to do here
    }

    private void writeTemporaryCurrentLimits(CurrentLimits limits, TableFormatter formatter, String branchId,
                                             boolean side1, String sideId) throws IOException {
        int branchNum = mapper.getInt(AmplSubset.BRANCH, branchId);
        for (LoadingLimits.TemporaryLimit tl : limits.getTemporaryLimits()) {
            String limitId = branchId + "_" + sideId + "_" + tl.getAcceptableDuration();
            int limitNum = mapper.getInt(AmplSubset.TEMPORARY_CURRENT_LIMIT, limitId);
            formatter.writeCell(variantIndex)
                .writeCell(limitNum)
                .writeCell(branchNum)
                .writeCell(side1 ? 1 : 2)
                .writeCell(tl.getValue())
                .writeCell(tl.getAcceptableDuration())
                .writeCell(faultNum)
                .writeCell(actionNum);
        }
    }

    /**
     * Cache the computation of the map.
     */
    private HashMap<String, HvdcLine> getHvdcLinesMap() {
        if (hvdcLinesMap == null) {
            hvdcLinesMap = computeHvdcLinesMap();
        }
        return hvdcLinesMap;
    }

    private HashMap<String, HvdcLine> computeHvdcLinesMap() {
        HashMap<String, HvdcLine> lineMap = new HashMap<>();
        network.getHvdcLines().forEach(line -> {
            if (line.getConverterStation1() != null) {
                lineMap.put(line.getConverterStation1().getId(), line);
            }
            if (line.getConverterStation2() != null) {
                lineMap.put(line.getConverterStation2().getId(), line);
            }
        });
        return lineMap;
    }

    private int getBusNum(Bus bus) {
        return bus == null ? -1 : mapper.getInt(AmplSubset.BUS, bus.getId());
    }

    private static double getPermanentLimit(CurrentLimits limits) {
        if (limits != null) {
            return limits.getPermanentLimit();
        }
        return Double.NaN;
    }

    public AmplExportConfig getConfig() {
        return config;
    }

    public Network getNetwork() {
        return network;
    }

    public StringToIntMapper<AmplSubset> getMapper() {
        return mapper;
    }

    public int getVariantIndex() {
        return variantIndex;
    }

    public int getFaultNum() {
        return faultNum;
    }

    public int getActionNum() {
        return actionNum;
    }
}
