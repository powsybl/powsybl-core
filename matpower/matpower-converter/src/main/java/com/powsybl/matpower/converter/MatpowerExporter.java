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
import com.powsybl.matpower.model.MBus;
import com.powsybl.matpower.model.MatpowerModel;
import com.powsybl.matpower.model.MatpowerWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedReader;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Exporter.class)
public class MatpowerExporter implements Exporter {

    private static final double BASE_MVA = 100;

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

    private static void createBuses(Network network, MatpowerModel model) {
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
            model.addBus(mBus);
        }
    }

    @Override
    public void export(Network network, Properties parameters, DataSource dataSource, Reporter reporter) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(reporter);

        MatpowerModel model = new MatpowerModel(network.getId());
        model.setBaseMva(BASE_MVA);
        model.setVersion("2");

        createBuses(network, model);

        try (OutputStream os = dataSource.newOutputStream(null, MatpowerConstants.EXT, false)) {
            MatpowerWriter.write(model, os);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
