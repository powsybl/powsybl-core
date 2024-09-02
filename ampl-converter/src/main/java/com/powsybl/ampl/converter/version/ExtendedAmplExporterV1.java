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

import java.io.IOException;
import java.util.List;

import static com.powsybl.ampl.converter.AmplConstants.*;

/**
 * 1st (V1) extension of BasicAmplExporter, associated with AMPL version 1.1 (exporter id)
 *
 * @author Nicolas PIERRE {@literal <nicolas.pierre at artelys.com>}
 */
public class ExtendedAmplExporterV1 extends BasicAmplExporter {

    public ExtendedAmplExporterV1(AmplExportConfig config,
                                  Network network,
                                  StringToIntMapper<AmplSubset> mapper,
                                  int variantIndex, int faultNum, int actionNum) {
        super(config, network, mapper, variantIndex, faultNum, actionNum);
    }

    private record ImpedanceAndAdmittance(double r, double x, double g, double b) { }

    @Override
    public List<Column> getBusesColumns() {
        super.getBusesColumns();

        return List.of(new Column(VARIANT),
            new Column(NUM),
            new Column(SUBSTATION),
            new Column("cc"),
            new Column("v (pu)"),
            new Column("theta (rad)"),
            new Column("p (MW)"),
            new Column("q (MVar)"),
            new Column("slack bus"),
            new Column(FAULT),
            new Column(getConfig().getActionType().getLabel()),
            new Column(ID));
    }

    @Override
    public List<Column> getTapChangerTableColumns() {
        return List.of(new Column(VARIANT),
            new Column(NUM),
            new Column("tap"),
            new Column("var ratio"),
            new Column("r (pu)"),
            new Column("x (pu)"),
            new Column("g (pu)"),
            new Column("b (pu)"),
            new Column("angle (rad)"),
            new Column(FAULT),
            new Column(getConfig().getActionType().getLabel()));
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
            new Column(V_REGUL_BUS),
            new Column(TARGET_V),
            new Column("targetP (MW)"),
            new Column(TARGET_Q),
            new Column(FAULT),
            new Column(getConfig().getActionType().getLabel()),
            new Column(ID),
            new Column(DESCRIPTION),
            new Column(ACTIVE_POWER),
            new Column(REACTIVE_POWER)
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
            new Column(V_REGUL_BUS),
            new Column(TARGET_V),
            new Column(TARGET_Q),
            new Column(FAULT),
            new Column(getConfig().getActionType().getLabel()),
            new Column(ID),
            new Column(DESCRIPTION),
            new Column(ACTIVE_POWER),
            new Column(REACTIVE_POWER)
        );
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

        int lowTapPosition = rtc.getLowTapPosition();
        for (int position = lowTapPosition; position <= rtc.getHighTapPosition(); position++) {
            RatioTapChangerStep step = rtc.getStep(position);
            writeTapChanger(formatter, zb2,
                transformerZandY, num, rtc.getLowTapPosition(),
                new ImpedanceAndAdmittance(step.getR(), step.getX(), step.getG(), step.getB()), position, step.getRho(),
                0);
        }
    }

    private void writePhaseTapChanger(TableFormatter formatter, String id, double zb2,
                                      ImpedanceAndAdmittance transformerZandY,
                                      PhaseTapChanger ptc) throws IOException {
        int num = getMapper().getInt(AmplSubset.TAP_CHANGER_TABLE, id);

        for (int position = ptc.getLowTapPosition(); position <= ptc.getHighTapPosition(); position++) {
            PhaseTapChangerStep step = ptc.getStep(position);
            writeTapChanger(formatter, zb2,
                transformerZandY, num, ptc.getLowTapPosition(),
                new ImpedanceAndAdmittance(step.getR(), step.getX(), step.getG(), step.getB()), position, step.getRho(),
                Math.toRadians(step.getAlpha()));
        }
    }

    private void writeTapChanger(TableFormatter formatter, double zb2, ImpedanceAndAdmittance transformer, int num,
                                 int lowTapPosition, ImpedanceAndAdmittance step, int position, double rho,
                                 double alpha) throws IOException {
        double rNorm = transformer.r * (1 + step.r / 100) / zb2;
        double xNorm = transformer.x * (1 + step.x / 100) / zb2;
        double gNorm = transformer.g * (1 + step.g / 100) / zb2;
        double bNorm = transformer.b * (1 + step.b / 100) / zb2;
        formatter.writeCell(getVariantIndex())
            .writeCell(num)
            .writeCell(position - lowTapPosition + 1)
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
