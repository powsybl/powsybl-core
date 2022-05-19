/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.converter;

import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.matpower.model.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Exporter.class)
public class MatpowerExporter implements Exporter {

    private static final double BASE_MVA = 100;
    private static final String FORMAT_VERSION = "2";

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

    private static MBus.Type getType(Bus bus) {
        if (!bus.isInMainConnectedComponent()) {
            return MBus.Type.ISOLATED;
        }
        VoltageLevel vl = bus.getVoltageLevel();
        SlackTerminal slackTerminal = vl.getExtension(SlackTerminal.class);
        if (slackTerminal != null) {
            Terminal terminal = slackTerminal.getTerminal();
            if (terminal.getBusView().getBus() == bus) {
                return MBus.Type.REF;
            }
        }
        for (Generator g : bus.getGenerators()) {
            if (g.isVoltageRegulatorOn()) {
                return MBus.Type.PV;
            }
        }
        return MBus.Type.PQ;
    }

    static class Context {

        final Map<String, MBus> mBusesByIds = new HashMap<>();
    }

    private static void createBuses(Network network, MatpowerModel model, Context context) {
        int num = 1;
        for (Bus bus : network.getBusView().getBuses()) {
            VoltageLevel vl = bus.getVoltageLevel();
            MBus mBus = new MBus();
            mBus.setNumber(num++);
            mBus.setType(getType(bus));
            mBus.setAreaNumber(1);
            mBus.setLossZone(1);
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
            double b = 0;
            double zb = vl.getNominalV() * vl.getNominalV() / BASE_MVA;
            for (ShuntCompensator sc : bus.getShuntCompensators()) {
                b += sc.getB() * zb * BASE_MVA;
            }
            mBus.setShuntConductance(0d);
            mBus.setShuntSusceptance(b);
            mBus.setVoltageMagnitude(Double.isNaN(bus.getV()) ? 0 : bus.getV());
            mBus.setVoltageAngle(Double.isNaN(bus.getAngle()) ? 0 : bus.getAngle());
            mBus.setMinimumVoltageMagnitude(Double.isNaN(vl.getLowVoltageLimit()) ? 0 : vl.getLowVoltageLimit());
            mBus.setMaximumVoltageMagnitude(Double.isNaN(vl.getHighVoltageLimit()) ? 0 : vl.getHighVoltageLimit());
            model.addBus(mBus);
            context.mBusesByIds.put(bus.getId(), mBus);
        }
    }

    private void createBranches(Network network, MatpowerModel model, Context context) {
        for (Line l : network.getLines()) {
            Terminal t1 = l.getTerminal1();
            Terminal t2 = l.getTerminal2();
            Bus bus1 = t1.getBusView().getBus();
            Bus bus2 = t2.getBusView().getBus();
            if (bus1 != null && bus2 != null) {
                VoltageLevel vl2 = t2.getVoltageLevel();
                MBranch mBranch = new MBranch();
                mBranch.setFrom(context.mBusesByIds.get(bus1.getId()).getNumber());
                mBranch.setTo(context.mBusesByIds.get(bus2.getId()).getNumber());
                mBranch.setStatus(1);
                double zb = vl2.getNominalV() * vl2.getNominalV() / BASE_MVA;
                mBranch.setR(l.getR() / zb);
                mBranch.setX(l.getX() / zb);
                mBranch.setB((l.getB1() + l.getB2()) * zb);
                model.addBranch(mBranch);
            }
        }
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            Terminal t1 = twt.getTerminal1();
            Terminal t2 = twt.getTerminal2();
            Bus bus1 = t1.getBusView().getBus();
            Bus bus2 = t2.getBusView().getBus();
            if (bus1 != null && bus2 != null) {
                VoltageLevel vl1 = t1.getVoltageLevel();
                VoltageLevel vl2 = t2.getVoltageLevel();
                MBranch mBranch = new MBranch();
                mBranch.setFrom(context.mBusesByIds.get(bus1.getId()).getNumber());
                mBranch.setTo(context.mBusesByIds.get(bus2.getId()).getNumber());
                mBranch.setStatus(1);
                double zb = vl2.getNominalV() * vl2.getNominalV() / BASE_MVA;
                mBranch.setR(twt.getR() / zb);
                mBranch.setX(twt.getX() / zb);
                mBranch.setB(twt.getB() * zb);
                var rtc = twt.getRatioTapChanger();
                double rho = (twt.getRatedU2() / vl2.getNominalV()) / (twt.getRatedU1() / vl1.getNominalV())
                        * (1 + (rtc != null ? rtc.getCurrentStep().getRho() / 100 : 0));
                mBranch.setRatio(1 / rho);
                var ptc = twt.getPhaseTapChanger();
                mBranch.setPhaseShiftAngle(ptc != null ? -ptc.getCurrentStep().getAlpha() : 0);
                model.addBranch(mBranch);
            }
        }
    }

    private void createGenerators(Network network, MatpowerModel model, Context context) {
        for (Generator g : network.getGenerators()) {
            Bus bus = g.getTerminal().getBusView().getBus();
            if (bus != null) {
                MGen mGen = new MGen();
                mGen.setNumber(context.mBusesByIds.get(bus.getId()).getNumber());
                mGen.setStatus(1);
                mGen.setRealPowerOutput(g.getTargetP());
                mGen.setReactivePowerOutput(g.getTargetQ());
                mGen.setVoltageMagnitudeSetpoint(g.isVoltageRegulatorOn() ? g.getTargetV() : 0);
                mGen.setMinimumRealPowerOutput(g.getMinP());
                mGen.setMaximumRealPowerOutput(g.getMaxP());
                mGen.setMinimumReactivePowerOutput(g.getReactiveLimits().getMinQ(g.getTargetP()));
                mGen.setMaximumReactivePowerOutput(g.getReactiveLimits().getMaxQ(g.getTargetP()));
                model.addGenerator(mGen);
            }
        }
    }

    @Override
    public void export(Network network, Properties parameters, DataSource dataSource, Reporter reporter) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(reporter);

        MatpowerModel model = new MatpowerModel(network.getId());
        model.setBaseMva(BASE_MVA);
        model.setVersion(FORMAT_VERSION);

        Context context = new Context();
        createBuses(network, model, context);
        createBranches(network, model, context);
        createGenerators(network, model, context);

        try (OutputStream os = dataSource.newOutputStream(null, MatpowerConstants.EXT, false)) {
            MatpowerWriter.write(model, os);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
