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
import com.powsybl.ampl.converter.util.NetworkUtil;
import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.ConnectedComponents;
import com.powsybl.iidm.network.util.SV;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.powsybl.ampl.converter.AmplConstants.*;

/**
 * 1st (V1) extension of BasicAmplExporter, associated with AMPL version 1.1 (exporter id)
 *
 * @author Nicolas PIERRE {@literal <nicolas.pierre at artelys.com>}
 */
public class ExtendedAmplExporterV1 extends BasicAmplExporter {

    private static final int SLACK_BUS_COLUMN_INDEX = 8;
    private static final int TAP_CHANGER_R_COLUMN_INDEX = 4;
    private static final int TAP_CHANGER_G_COLUMN_INDEX = 6;
    private static final int TAP_CHANGER_B_COLUMN_INDEX = 7;
    private static final int GENERATOR_V_REGUL_BUS_COLUMN_INDEX = 14;
    private static final int STATIC_VAR_COMPENSATOR_V_REGUL_BUS_COLUMN_INDEX = 8;

    public ExtendedAmplExporterV1(AmplExportConfig config,
                                  Network network,
                                  StringToIntMapper<AmplSubset> mapper,
                                  int variantIndex, int faultNum, int actionNum) {
        super(config, network, mapper, variantIndex, faultNum, actionNum);
    }

    private record ImpedanceAndAdmittance(double r, double x, double g, double b) { }

    @Override
    public List<Column> getBusesColumns() {
        List<Column> busesColumns = new ArrayList<>(super.getBusesColumns());
        // add slack bus column
        busesColumns.add(SLACK_BUS_COLUMN_INDEX, new Column("slack bus"));
        return busesColumns;
    }

    @Override
    public List<Column> getTapChangerTableColumns() {
        List<Column> tapChangerTableColumns = new ArrayList<>(super.getTapChangerTableColumns());
        // add r, g and b columns
        tapChangerTableColumns.add(TAP_CHANGER_R_COLUMN_INDEX, new Column("r (pu)"));
        tapChangerTableColumns.add(TAP_CHANGER_G_COLUMN_INDEX, new Column("g (pu)"));
        tapChangerTableColumns.add(TAP_CHANGER_B_COLUMN_INDEX, new Column("b (pu)"));
        return tapChangerTableColumns;
    }

    @Override
    public List<Column> getGeneratorsColumns() {
        List<Column> generatorsColumns = new ArrayList<>(super.getGeneratorsColumns());
        // add column for voltage regulated bus
        generatorsColumns.add(GENERATOR_V_REGUL_BUS_COLUMN_INDEX, new Column(V_REGUL_BUS));
        return generatorsColumns;
    }

    @Override
    public List<Column> getStaticVarCompensatorColumns() {
        List<Column> svcColumns = new ArrayList<>(super.getStaticVarCompensatorColumns());
        // add column for voltage regulated bus
        svcColumns.add(STATIC_VAR_COMPENSATOR_V_REGUL_BUS_COLUMN_INDEX, new Column(V_REGUL_BUS));
        return svcColumns;
    }

    @Override
    public void writeBusesColumnsToFormatter(TableFormatter formatter, Bus b) throws IOException {
        int ccNum = ConnectedComponents.getCcNum(b);
        String id = b.getId();
        VoltageLevel vl = b.getVoltageLevel();
        int num = getMapper().getInt(AmplSubset.BUS, id);
        int vlNum = getMapper().getInt(AmplSubset.VOLTAGE_LEVEL, vl.getId());
        double nomV = vl.getNominalV();
        double v = b.getV() / nomV;
        double theta = Math.toRadians(b.getAngle());
        formatter.writeCell(getVariantIndex())
                .writeCell(num)
                .writeCell(vlNum)
                .writeCell(ccNum)
                .writeCell(v)
                .writeCell(theta)
                .writeCell(b.getP())
                .writeCell(b.getQ())
                .writeCell(NetworkUtil.isSlackBus(b))
                .writeCell(getFaultNum())
                .writeCell(getActionNum())
                .writeCell(id);
    }

    @Override
    public void writeThreeWindingsTranformersMiddleBusesColumnsToFormatter(TableFormatter formatter,
                                                                           ThreeWindingsTransformer twt,
                                                                           int middleCcNum) throws IOException {
        String middleBusId = AmplUtil.getThreeWindingsTransformerMiddleBusId(twt);
        String middleVlId = AmplUtil.getThreeWindingsTransformerMiddleVoltageLevelId(twt);
        int middleBusNum = getMapper().getInt(AmplSubset.BUS, middleBusId);
        int middleVlNum = getMapper().getInt(AmplSubset.VOLTAGE_LEVEL, middleVlId);

        double v = twt.getProperty("v") == null ? Double.NaN :
                Double.parseDouble(twt.getProperty("v")) / twt.getRatedU0();
        double angle = twt.getProperty("angle") == null ? Double.NaN :
                Math.toRadians(Double.parseDouble(twt.getProperty("angle")));

        formatter.writeCell(getVariantIndex())
                .writeCell(middleBusNum)
                .writeCell(middleVlNum)
                .writeCell(middleCcNum)
                .writeCell(v)
                .writeCell(angle)
                .writeCell(0.0)
                .writeCell(0.0)
                .writeCell(false)
                .writeCell(getFaultNum())
                .writeCell(getActionNum())
                .writeCell(middleBusId);
    }

    @Override
    public void writeDanglingLineMiddleBusesToFormatter(TableFormatter formatter, DanglingLine dl,
                                                        int middleCcNum) throws IOException {
        Terminal t = dl.getTerminal();
        Bus b = AmplUtil.getBus(dl.getTerminal());
        String middleBusId = AmplUtil.getDanglingLineMiddleBusId(dl);
        String middleVlId = AmplUtil.getDanglingLineMiddleVoltageLevelId(dl);
        int middleBusNum = getMapper().getInt(AmplSubset.BUS, middleBusId);
        int middleVlNum = getMapper().getInt(AmplSubset.VOLTAGE_LEVEL, middleVlId);
        SV sv = new SV(t.getP(), t.getQ(), b != null ? b.getV() : Double.NaN, b != null ? b.getAngle() : Double.NaN,
                TwoSides.ONE).otherSide(
                dl, true);
        double nomV = t.getVoltageLevel().getNominalV();
        double v = sv.getU() / nomV;
        double theta = Math.toRadians(sv.getA());
        formatter.writeCell(getVariantIndex())
                .writeCell(middleBusNum)
                .writeCell(middleVlNum)
                .writeCell(middleCcNum)
                .writeCell(v)
                .writeCell(theta)
                .writeCell(0.0) // 0 MW injected at dangling line internal bus
                .writeCell(0.0) // 0 MVar injected at dangling line internal bus
                .writeCell(false)
                .writeCell(getFaultNum())
                .writeCell(getActionNum())
                .writeCell(middleBusId);
    }

    @Override
    public void writeTieLineMiddleBusesToFormatter(TableFormatter formatter, TieLine tieLine,
                                                   int xNodeCcNum) throws IOException {
        String xNodeBusId = AmplUtil.getXnodeBusId(tieLine);
        int xNodeBusNum = getMapper().getInt(AmplSubset.BUS, xNodeBusId);
        int xNodeVlNum = getMapper().getInt(AmplSubset.VOLTAGE_LEVEL, AmplUtil.getXnodeVoltageLevelId(tieLine));
        formatter.writeCell(getVariantIndex())
                .writeCell(xNodeBusNum)
                .writeCell(xNodeVlNum)
                .writeCell(xNodeCcNum)
                .writeCell(Float.NaN)
                .writeCell(Double.NaN)
                .writeCell(0.0)
                .writeCell(0.0)
                .writeCell(false)
                .writeCell(getFaultNum())
                .writeCell(getActionNum())
                .writeCell(xNodeBusId);
    }

    @Override
    public void writeTwoWindingsTransformerTapChangerTableToFormatter(TableFormatter formatter,
                                                                      TwoWindingsTransformer twt) throws IOException {
        Terminal t2 = twt.getTerminal2();
        double vb2 = t2.getVoltageLevel().getNominalV();
        double zb2 = vb2 * vb2 / AmplConstants.SB;
        ImpedanceAndAdmittance transformer = new ImpedanceAndAdmittance(twt.getR(), twt.getX(),
            twt.getG(), twt.getB());
        RatioTapChanger rtc = twt.getRatioTapChanger();
        if (rtc != null) {
            String id = twt.getId() + RATIO_TABLE_SUFFIX;
            writeRatioTapChanger(formatter, id, zb2, transformer, rtc);
        }

        PhaseTapChanger ptc = twt.getPhaseTapChanger();
        if (ptc != null) {
            String id = twt.getId() + PHASE_TABLE_SUFFIX;
            writePhaseTapChanger(formatter, id, zb2, transformer, ptc);
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
            ImpedanceAndAdmittance transformer = new ImpedanceAndAdmittance(leg.getR(), leg.getX(),
                leg.getG(), leg.getB());
            if (rtc != null) {
                String id = twt.getId() + "_leg" + legNumber + RATIO_TABLE_SUFFIX;
                writeRatioTapChanger(formatter, id, zb, transformer, rtc);
            }
            PhaseTapChanger ptc = leg.getPhaseTapChanger();
            if (ptc != null) {
                String id = twt.getId() + "_leg" + legNumber + PHASE_TABLE_SUFFIX;
                writePhaseTapChanger(formatter, id, zb, transformer, ptc);
            }
        }
    }

    private void writeRatioTapChanger(TableFormatter formatter, String id, double zb2,
                                      ImpedanceAndAdmittance transformerZandY,
                                      RatioTapChanger rtc) throws IOException {
        int num = getMapper().getInt(AmplSubset.TAP_CHANGER_TABLE, id);

        for (int position = rtc.getLowTapPosition(); position <= rtc.getHighTapPosition(); position++) {
            RatioTapChangerStep step = rtc.getStep(position);
            ImpedanceAndAdmittance stepCharacteristics = new ImpedanceAndAdmittance(step.getR(), step.getX(), step.getG(), step.getB());
            writeTapChanger(formatter, num, position, rtc.getLowTapPosition(), zb2, transformerZandY, stepCharacteristics, step.getRho(), 0);
        }
    }

    private void writePhaseTapChanger(TableFormatter formatter, String id, double zb2,
                                      ImpedanceAndAdmittance transformerZandY,
                                      PhaseTapChanger ptc) throws IOException {
        int num = getMapper().getInt(AmplSubset.TAP_CHANGER_TABLE, id);

        for (int position = ptc.getLowTapPosition(); position <= ptc.getHighTapPosition(); position++) {
            PhaseTapChangerStep step = ptc.getStep(position);
            ImpedanceAndAdmittance stepCharacteristics = new ImpedanceAndAdmittance(step.getR(), step.getX(), step.getG(), step.getB());
            writeTapChanger(formatter, num, position, ptc.getLowTapPosition(), zb2, transformerZandY, stepCharacteristics, step.getRho(), Math.toRadians(step.getAlpha()));
        }
    }

    private void writeTapChanger(TableFormatter formatter, int num, int stepPosition, int lowTapPosition, double zb2,
                                 ImpedanceAndAdmittance transformer, ImpedanceAndAdmittance step, double rho, double alpha) throws IOException {
        double rNorm = transformer.r * (1 + step.r / 100) / zb2;
        double xNorm = transformer.x * (1 + step.x / 100) / zb2;
        double gNorm = transformer.g * (1 + step.g / 100) * zb2;
        double bNorm = transformer.b * (1 + step.b / 100) * zb2;
        formatter.writeCell(getVariantIndex())
            .writeCell(num)
            .writeCell(stepPosition - lowTapPosition + 1)
            .writeCell(rho)
            .writeCell(rNorm)
            .writeCell(xNorm)
            .writeCell(gNorm)
            .writeCell(bNorm)
            .writeCell(alpha)
            .writeCell(getFaultNum())
            .writeCell(getActionNum());
    }

    @Override
    public void writeGeneratorToFormatter(TableFormatter formatter, Generator gen) throws IOException {
        String id = gen.getId();
        Terminal t = gen.getTerminal();
        int num = getMapper().getInt(AmplSubset.GENERATOR, id);
        int vlNum = getMapper().getInt(AmplSubset.VOLTAGE_LEVEL, t.getVoltageLevel().getId());
        int busNum = AmplUtil.getBusNum(getMapper(), t);
        int conBusNum = AmplUtil.getConnectableBusNum(getMapper(), t);
        double minP = gen.getMinP();
        double maxP = gen.getMaxP();
        double vb = gen.getRegulatingTerminal().getVoltageLevel().getNominalV();
        int regulatingBusNum = gen.isVoltageRegulatorOn() ?
            getMapper().getInt(AmplSubset.BUS, gen.getRegulatingTerminal().getBusView().getBus().getId()) : -1;

        formatter.writeCell(getVariantIndex())
            .writeCell(num)
            .writeCell(busNum)
            .writeCell(conBusNum != -1 ? conBusNum : busNum)
            .writeCell(vlNum)
            .writeCell(minP)
            .writeCell(maxP)
            .writeCell(gen.getReactiveLimits().getMinQ(maxP))
            .writeCell(gen.getReactiveLimits().getMinQ(0))
            .writeCell(gen.getReactiveLimits().getMinQ(minP))
            .writeCell(gen.getReactiveLimits().getMaxQ(maxP))
            .writeCell(gen.getReactiveLimits().getMaxQ(0))
            .writeCell(gen.getReactiveLimits().getMaxQ(minP))
            .writeCell(gen.isVoltageRegulatorOn())
            .writeCell(regulatingBusNum)
            .writeCell(gen.getTargetV() / vb)
            .writeCell(gen.getTargetP())
            .writeCell(gen.getTargetQ())
            .writeCell(getFaultNum())
            .writeCell(getActionNum())
            .writeCell(id)
            .writeCell(gen.getNameOrId())
            .writeCell(t.getP())
            .writeCell(t.getQ());
    }

    @Override
    public void writeStaticVarCompensatorToFormatter(TableFormatter formatter,
                                                     StaticVarCompensator svc) throws IOException {
        String id = svc.getId();
        int num = getMapper().getInt(AmplSubset.STATIC_VAR_COMPENSATOR, id);

        Terminal t = svc.getTerminal();

        int busNum = AmplUtil.getBusNum(getMapper(), t);
        int conBusNum = AmplUtil.getConnectableBusNum(getMapper(), t);

        boolean voltageRegulation = svc.getRegulationMode().equals(StaticVarCompensator.RegulationMode.VOLTAGE);
        int regulatingBusNum = voltageRegulation ?
            getMapper().getInt(AmplSubset.BUS, svc.getRegulatingTerminal().getBusView().getBus().getId()) : -1;

        double vlSet = svc.getVoltageSetpoint();
        double vb = t.getVoltageLevel().getNominalV();
        double zb = vb * vb / AmplConstants.SB; // Base impedance

        int vlNum = getMapper().getInt(AmplSubset.VOLTAGE_LEVEL, t.getVoltageLevel().getId());
        formatter.writeCell(getVariantIndex())
            .writeCell(num)
            .writeCell(busNum)
            .writeCell(conBusNum)
            .writeCell(vlNum)
            .writeCell(svc.getBmin() * zb)
            .writeCell(svc.getBmax() * zb)
            .writeCell(voltageRegulation)
            .writeCell(regulatingBusNum)
            .writeCell(vlSet / vb)
            .writeCell(svc.getReactivePowerSetpoint())
            .writeCell(getFaultNum())
            .writeCell(getActionNum())
            .writeCell(id)
            .writeCell(svc.getNameOrId())
            .writeCell(t.getP())
            .writeCell(t.getQ());
    }

}
