/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.converter;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.matpower.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Exporter.class)
public class MatpowerExporter implements Exporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatpowerExporter.class);

    private static final double BASE_MVA = 100;
    private static final String FORMAT_VERSION = "2";
    private static final int AREA_NUMBER = 1;
    private static final int LOSS_ZONE = 1;
    private static final int CONNECTED_STATUS = 1;
    private static final String V_PROP = "v";
    private static final String ANGLE_PROP = "angle";

    @Override
    public String getFormat() {
        return MatpowerConstants.FORMAT;
    }

    @Override
    public String getComment() {
        return "IIDM to MATPOWER format converter";
    }

    @Override
    public List<Parameter> getParameters() {
        return Collections.emptyList();
    }

    private static boolean hasSlackExtension(Bus bus) {
        VoltageLevel vl = bus.getVoltageLevel();
        SlackTerminal slackTerminal = vl.getExtension(SlackTerminal.class);
        if (slackTerminal != null) {
            Terminal terminal = slackTerminal.getTerminal();
            return terminal.getBusView().getBus() == bus;
        }
        return false;
    }

    private static MBus.Type getType(Bus bus, Context context) {
        if ((context.refBusId != null && context.refBusId.equals(bus.getId())) || hasSlackExtension(bus)) {
            return MBus.Type.REF;
        }
        for (Generator g : bus.getGenerators()) {
            if (g.isVoltageRegulatorOn()) {
                return MBus.Type.PV;
            }
        }
        return MBus.Type.PQ;
    }

    static class Context {

        String refBusId;

        int num = 1;

        final Map<String, Integer> mBusesNumbersByIds = new HashMap<>();
    }

    private static boolean isConnectedToMainCc(Bus bus) {
        return bus != null && bus.isInMainConnectedComponent();
    }

    private static void createTransformerStarBuses(Network network, MatpowerModel model, Context context) {
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            Bus bus1 = twt.getLeg1().getTerminal().getBusView().getBus();
            Bus bus2 = twt.getLeg2().getTerminal().getBusView().getBus();
            Bus bus3 = twt.getLeg3().getTerminal().getBusView().getBus();
            if (isConnectedToMainCc(bus1) && isConnectedToMainCc(bus2) && isConnectedToMainCc(bus3)) {
                MBus mBus = new MBus();
                mBus.setNumber(context.num++);
                mBus.setName(twt.getNameOrId());
                mBus.setType(MBus.Type.PQ);
                mBus.setAreaNumber(AREA_NUMBER);
                mBus.setLossZone(LOSS_ZONE);
                mBus.setBaseVoltage(twt.getRatedU0());
                mBus.setMinimumVoltageMagnitude(0d);
                mBus.setMaximumVoltageMagnitude(0d);
                mBus.setRealPowerDemand(0d);
                mBus.setReactivePowerDemand(0d);
                mBus.setShuntConductance(0d);
                mBus.setShuntSusceptance(0d);
                mBus.setVoltageMagnitude(twt.hasProperty(V_PROP) ? Double.parseDouble(twt.getProperty(V_PROP)) / twt.getRatedU0() : 1d);
                mBus.setVoltageAngle(twt.hasProperty(ANGLE_PROP) ? Double.parseDouble(twt.getProperty(ANGLE_PROP)) : 0d);
                mBus.setMinimumVoltageMagnitude(0d);
                mBus.setMaximumVoltageMagnitude(0d);
                model.addBus(mBus);
                context.mBusesNumbersByIds.put(twt.getId(), mBus.getNumber());
            }
        }
    }

    private static void createDanglingLineBuses(Network network, MatpowerModel model, Context context) {
        for (DanglingLine dl : network.getDanglingLines()) {
            Terminal t = dl.getTerminal();
            Bus bus = t.getBusView().getBus();
            if (isConnectedToMainCc(bus)) {
                VoltageLevel vl = t.getVoltageLevel();
                MBus mBus = new MBus();
                mBus.setNumber(context.num++);
                mBus.setName(dl.getNameOrId());
                mBus.setType(MBus.Type.PQ);
                mBus.setAreaNumber(AREA_NUMBER);
                mBus.setLossZone(LOSS_ZONE);
                mBus.setBaseVoltage(dl.getTerminal().getVoltageLevel().getNominalV());
                mBus.setMinimumVoltageMagnitude(0d);
                mBus.setMaximumVoltageMagnitude(0d);
                mBus.setRealPowerDemand(dl.getP0());
                mBus.setReactivePowerDemand(dl.getQ0());
                mBus.setShuntConductance(0d);
                mBus.setShuntSusceptance(0d);
                mBus.setVoltageMagnitude(dl.getBoundary().getV() / vl.getNominalV());
                mBus.setVoltageAngle(dl.getBoundary().getAngle());
                mBus.setMinimumVoltageMagnitude(0d);
                mBus.setMaximumVoltageMagnitude(0d);
                model.addBus(mBus);
                context.mBusesNumbersByIds.put(dl.getId(), mBus.getNumber());
            }
        }
    }

    private static void createBuses(Network network, MatpowerModel model, Context context) {
        for (Bus bus : network.getBusView().getBuses()) {
            if (bus.isInMainConnectedComponent()) {
                VoltageLevel vl = bus.getVoltageLevel();
                MBus mBus = new MBus();
                mBus.setNumber(context.num++);
                mBus.setName(bus.getNameOrId());
                mBus.setType(getType(bus, context));
                mBus.setAreaNumber(AREA_NUMBER);
                mBus.setLossZone(LOSS_ZONE);
                mBus.setBaseVoltage(vl.getNominalV());
                mBus.setMinimumVoltageMagnitude(vl.getLowVoltageLimit());
                mBus.setMaximumVoltageMagnitude(vl.getHighVoltageLimit());
                double pDemand = 0;
                double qDemand = 0;
                for (Load l : bus.getLoads()) {
                    pDemand += l.getP0();
                    qDemand += l.getQ0();
                }
                mBus.setRealPowerDemand(pDemand);
                mBus.setReactivePowerDemand(qDemand);
                double bSum = 0;
                double zb = vl.getNominalV() * vl.getNominalV() / BASE_MVA;
                for (ShuntCompensator sc : bus.getShuntCompensators()) {
                    bSum += sc.getB() * zb * BASE_MVA;
                }
                mBus.setShuntConductance(0d);
                mBus.setShuntSusceptance(bSum);
                mBus.setVoltageMagnitude(Double.isNaN(bus.getV()) ? 1 : bus.getV() / vl.getNominalV());
                mBus.setVoltageAngle(Double.isNaN(bus.getAngle()) ? 0 : bus.getAngle());
                mBus.setMinimumVoltageMagnitude(Double.isNaN(vl.getLowVoltageLimit()) ? 0 : vl.getLowVoltageLimit());
                mBus.setMaximumVoltageMagnitude(Double.isNaN(vl.getHighVoltageLimit()) ? 0 : vl.getHighVoltageLimit());
                model.addBus(mBus);
                context.mBusesNumbersByIds.put(bus.getId(), mBus.getNumber());
            }
        }

        createDanglingLineBuses(network, model, context);
        createTransformerStarBuses(network, model, context);
    }

    private void createLines(Network network, MatpowerModel model, Context context) {
        for (Line l : network.getLines()) {
            Terminal t1 = l.getTerminal1();
            Terminal t2 = l.getTerminal2();
            Bus bus1 = t1.getBusView().getBus();
            Bus bus2 = t2.getBusView().getBus();
            if (isConnectedToMainCc(bus1) && isConnectedToMainCc(bus2)) {
                VoltageLevel vl2 = t2.getVoltageLevel();
                MBranch mBranch = new MBranch();
                mBranch.setFrom(context.mBusesNumbersByIds.get(bus1.getId()));
                mBranch.setTo(context.mBusesNumbersByIds.get(bus2.getId()));
                mBranch.setStatus(CONNECTED_STATUS);
                double zb = vl2.getNominalV() * vl2.getNominalV() / BASE_MVA;
                mBranch.setR(l.getR() / zb);
                mBranch.setX(l.getX() / zb);
                mBranch.setB((l.getB1() + l.getB2()) * zb);
                model.addBranch(mBranch);
            }
        }
    }

    private void createTransformers2(Network network, MatpowerModel model, Context context) {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            Terminal t1 = twt.getTerminal1();
            Terminal t2 = twt.getTerminal2();
            Bus bus1 = t1.getBusView().getBus();
            Bus bus2 = t2.getBusView().getBus();
            if (isConnectedToMainCc(bus1) && isConnectedToMainCc(bus2)) {
                VoltageLevel vl1 = t1.getVoltageLevel();
                VoltageLevel vl2 = t2.getVoltageLevel();
                MBranch mBranch = new MBranch();
                mBranch.setFrom(context.mBusesNumbersByIds.get(bus1.getId()));
                mBranch.setTo(context.mBusesNumbersByIds.get(bus2.getId()));
                mBranch.setStatus(CONNECTED_STATUS);
                double zb = vl2.getNominalV() * vl2.getNominalV() / BASE_MVA;
                mBranch.setR(twt.getR() / zb);
                mBranch.setX(twt.getX() / zb);
                mBranch.setB(twt.getB() * zb);
                var rtc = twt.getRatioTapChanger();
                double rho = (twt.getRatedU2() / vl2.getNominalV()) / (twt.getRatedU1() / vl1.getNominalV())
                        * (1 + (rtc != null ? rtc.getCurrentStep().getRho() / 100 : 0));
                mBranch.setRatio(1d / rho);
                var ptc = twt.getPhaseTapChanger();
                mBranch.setPhaseShiftAngle(ptc != null ? -ptc.getCurrentStep().getAlpha() : 0);
                model.addBranch(mBranch);
            }
        }
    }

    private void createDanglingLineBranches(Network network, MatpowerModel model, Context context) {
        for (DanglingLine dl : network.getDanglingLines()) {
            Terminal t = dl.getTerminal();
            Bus bus = t.getBusView().getBus();
            if (isConnectedToMainCc(bus)) {
                VoltageLevel vl = t.getVoltageLevel();
                MBranch mBranch = new MBranch();
                mBranch.setFrom(context.mBusesNumbersByIds.get(bus.getId()));
                mBranch.setTo(context.mBusesNumbersByIds.get(dl.getId()));
                mBranch.setStatus(CONNECTED_STATUS);
                double zb = vl.getNominalV() * vl.getNominalV() / BASE_MVA;
                mBranch.setR(dl.getR() / zb);
                mBranch.setX(dl.getX() / zb);
                mBranch.setB(dl.getB() * zb);
                model.addBranch(mBranch);
            }
        }
    }

    private void createTransformerLegs(Network network, MatpowerModel model, Context context) {
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            var leg1 = twt.getLeg1();
            var leg2 = twt.getLeg2();
            var leg3 = twt.getLeg3();
            Terminal t1 = leg1.getTerminal();
            Terminal t2 = leg2.getTerminal();
            Terminal t3 = leg3.getTerminal();
            Bus bus1 = t1.getBusView().getBus();
            Bus bus2 = t2.getBusView().getBus();
            Bus bus3 = t3.getBusView().getBus();
            if (isConnectedToMainCc(bus1) && isConnectedToMainCc(bus2) && isConnectedToMainCc(bus3)) {
                model.addBranch(createTransformerLeg(twt, leg1, bus1, context));
                model.addBranch(createTransformerLeg(twt, leg2, bus2, context));
                model.addBranch(createTransformerLeg(twt, leg3, bus3, context));
            }
        }
    }

    private static MBranch createTransformerLeg(ThreeWindingsTransformer twt, ThreeWindingsTransformer.Leg leg, Bus bus, Context context) {
        MBranch mBranch = new MBranch();
        mBranch.setFrom(context.mBusesNumbersByIds.get(bus.getId()));
        mBranch.setTo(context.mBusesNumbersByIds.get(twt.getId()));
        mBranch.setStatus(CONNECTED_STATUS);
        double zb = Math.pow(twt.getRatedU0(), 2) / BASE_MVA;
        mBranch.setR(leg.getR() / zb);
        mBranch.setX(leg.getX() / zb);
        mBranch.setB(leg.getB() * zb);
        var rtc = leg.getRatioTapChanger();
        double rho = 1d / (leg.getRatedU() / leg.getTerminal().getVoltageLevel().getNominalV())
                * (1d + (rtc != null ? rtc.getCurrentStep().getRho() / 100 : 0));
        mBranch.setRatio(1d / rho);
        var ptc = leg.getPhaseTapChanger();
        mBranch.setPhaseShiftAngle(ptc != null ? -ptc.getCurrentStep().getAlpha() : 0);
        return mBranch;
    }

    private void createBranches(Network network, MatpowerModel model, Context context) {
        createLines(network, model, context);
        createTransformers2(network, model, context);
        createDanglingLineBranches(network, model, context);
        createTransformerLegs(network, model, context);
    }

    private void createDanglingLineGenerators(Network network, MatpowerModel model, Context context) {
        for (DanglingLine dl : network.getDanglingLines()) {
            Terminal t = dl.getTerminal();
            Bus bus = t.getBusView().getBus();
            if (isConnectedToMainCc(bus)) {
                var g = dl.getGeneration();
                if (g != null) {
                    VoltageLevel vl = t.getVoltageLevel();
                    MGen mGen = new MGen();
                    mGen.setNumber(context.mBusesNumbersByIds.get(dl.getId()));
                    mGen.setStatus(CONNECTED_STATUS);
                    mGen.setRealPowerOutput(g.getTargetP());
                    mGen.setReactivePowerOutput(g.getTargetQ());
                    mGen.setVoltageMagnitudeSetpoint(g.isVoltageRegulationOn() ? g.getTargetV() / vl.getNominalV() : 0);
                    mGen.setMinimumRealPowerOutput(g.getMinP());
                    mGen.setMaximumRealPowerOutput(g.getMaxP());
                    mGen.setMinimumReactivePowerOutput(g.getReactiveLimits().getMinQ(g.getTargetP()));
                    mGen.setMaximumReactivePowerOutput(g.getReactiveLimits().getMaxQ(g.getTargetP()));
                    model.addGenerator(mGen);
                }
            }
        }
    }

    private void createGenerators(Network network, MatpowerModel model, Context context) {
        for (Generator g : network.getGenerators()) {
            Terminal t = g.getTerminal();
            Bus bus = t.getBusView().getBus();
            if (isConnectedToMainCc(bus)) {
                VoltageLevel vl = t.getVoltageLevel();
                MGen mGen = new MGen();
                mGen.setNumber(context.mBusesNumbersByIds.get(bus.getId()));
                mGen.setStatus(CONNECTED_STATUS);
                mGen.setRealPowerOutput(g.getTargetP());
                mGen.setReactivePowerOutput(g.getTargetQ());
                Bus regulatedBus = g.getRegulatingTerminal().getBusView().getBus();
                if (g.isVoltageRegulatorOn() && regulatedBus != null) {
                    double targetV = g.getTargetV() / vl.getNominalV();
                    if (!regulatedBus.getId().equals(bus.getId())) {
                        double oldTargetV = targetV;
                        targetV *= vl.getNominalV() / regulatedBus.getVoltageLevel().getNominalV();
                        LOGGER.warn("Generator remote voltage control not supported in Matpower model, rescale targetV of '{}' from {} to {}",
                                g.getId(), oldTargetV, targetV);
                    }
                    mGen.setVoltageMagnitudeSetpoint(targetV);
                } else {
                    mGen.setVoltageMagnitudeSetpoint(0);
                }
                mGen.setMinimumRealPowerOutput(g.getMinP());
                mGen.setMaximumRealPowerOutput(g.getMaxP());
                mGen.setMinimumReactivePowerOutput(g.getReactiveLimits().getMinQ(g.getTargetP()));
                mGen.setMaximumReactivePowerOutput(g.getReactiveLimits().getMaxQ(g.getTargetP()));
                model.addGenerator(mGen);
            }
        }

        createDanglingLineGenerators(network, model, context);
    }

    private static int getBranchCount(Bus bus) {
        int[] branchCount = new int[1];
        bus.visitConnectedEquipments(new DefaultTopologyVisitor() {
            @Override
            public void visitLine(Line line, Branch.Side side) {
                branchCount[0]++;
            }

            @Override
            public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, Branch.Side side) {
                branchCount[0]++;
            }

            @Override
            public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side) {
                branchCount[0]++;
            }

            @Override
            public void visitDanglingLine(DanglingLine danglingLine) {
                branchCount[0]++;
            }
        });
        return branchCount[0];
    }

    @Override
    public void export(Network network, Properties parameters, DataSource dataSource, Reporter reporter) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(reporter);
        if (network.getHvdcLineCount() > 0) {
            throw new PowsyblException("HVDC line conversion not supported");
        }
        if (network.getStaticVarCompensatorCount() > 0) {
            throw new PowsyblException("Static var compensator conversion not supported");
        }
        if (network.getBatteryCount() > 0) {
            throw new PowsyblException("Battery conversion not supported");
        }

        MatpowerModel model = new MatpowerModel(network.getId());
        model.setBaseMva(BASE_MVA);
        model.setVersion(FORMAT_VERSION);

        Context context = new Context();
        boolean hasSlack = network.getBusView().getBusStream().anyMatch(MatpowerExporter::hasSlackExtension);
        if (!hasSlack) {
            context.refBusId = network.getBusView().getBusStream()
                    .filter(Bus::isInMainConnectedComponent)
                    .max(Comparator.comparingInt(MatpowerExporter::getBranchCount))
                    .orElseThrow()
                    .getId();
            LOGGER.debug("Matpower reference bus automatically selected: {}", context.refBusId);
        }
        createBuses(network, model, context);
        createBranches(network, model, context);
        createGenerators(network, model, context);

        try (OutputStream os = dataSource.newOutputStream(null, MatpowerConstants.EXT, false)) {
            MatpowerWriter.write(model, os);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        LOGGER.info("Matpower export of '{}' done", network.getId());
    }
}
