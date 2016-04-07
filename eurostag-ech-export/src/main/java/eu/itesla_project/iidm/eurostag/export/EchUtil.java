/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.eurostag.export;

import eu.itesla_project.iidm.network.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.StreamSupport;

/**
 *
 * @author pihan
 *
 */
public class EchUtil {

    private EchUtil() {
    }

    public static final String FAKE_AREA = "FA";

    public static final String FAKE_NODE_NAME1 = "FAKENOD1";

    public static final String FAKE_NODE_NAME2 = "FAKENOD2";

    public static String getBusId(DanglingLine dl) {
        return dl.getId() + "_BUS";
    }

    public static String getLoadId(DanglingLine dl) {
        return dl.getId() + "_LOAD";
    }

    public static Bus getBus(Terminal t, EurostagEchExportConfig config) {
        if (config.isNoSwitch()) {
            return t.getBusView().getBus();
        } else {
            return t.getBusBreakerView().getBus();
        }
    }

    public static Iterable<Bus> getBuses(Network n, EurostagEchExportConfig config) {
        if (config.isNoSwitch()) {
            return n.getBusView().getBuses();
        } else {
            return n.getBusBreakerView().getBuses();
        }
    }

    public static Iterable<Bus> getBuses(VoltageLevel vl, EurostagEchExportConfig config) {
        if (config.isNoSwitch()) {
            return vl.getBusView().getBuses();
        } else {
            return vl.getBusBreakerView().getBuses();
        }
    }

    public static Bus getBus1(VoltageLevel vl, String switchId, EurostagEchExportConfig config) {
        if (config.isNoSwitch()) {
            throw new AssertionError("Should not happen");
        } else {
            return vl.getBusBreakerView().getBus1(switchId);
        }
    }

    public static Bus getBus2(VoltageLevel vl, String switchId, EurostagEchExportConfig config) {
        if (config.isNoSwitch()) {
            throw new AssertionError("Should not happen");
        } else {
            return vl.getBusBreakerView().getBus2(switchId);
        }
    }

    public static Iterable<Switch> getSwitches(VoltageLevel vl, EurostagEchExportConfig config) {
        if (config.isNoSwitch()) {
            return Collections.emptyList();
        } else {
            return vl.getBusBreakerView().getSwitches();
        }
    }

    private static class DecoratedBus {
        Bus bus;
        int branch = 0;
        int regulatingGenerator = 0;
        float maxP = 0;
        float minP = 0;
        float targetP = 0;
        private DecoratedBus(Bus bus) {
            this.bus = bus;
        }
    }

    private static DecoratedBus decorate(Bus b) {
        final DecoratedBus decoratedBus = new DecoratedBus(b);
        b.visitConnectedEquipments(new AbstractTopologyVisitor() {
            @Override
            public void visitLine(Line line, Line.Side side) {
                decoratedBus.branch++;
            }

            @Override
            public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, TwoWindingsTransformer.Side side) {
                decoratedBus.branch++;
            }

            @Override
            public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side) {
                decoratedBus.branch++;
            }

            @Override
            public void visitDanglingLine(DanglingLine danglingLine) {
                decoratedBus.branch++;
            }

            @Override
            public void visitGenerator(Generator generator) {
                if (generator.isVoltageRegulatorOn()) {
                    decoratedBus.regulatingGenerator++;
                    decoratedBus.maxP += generator.getMaxP();
                    decoratedBus.minP += generator.getMinP();
                    decoratedBus.targetP += generator.getTargetP();
                }
            }
        });
        return decoratedBus;
    }

    /**
     * Automatically find the best slack bus list
     */
    public static Bus selectSlackbus(Network network, EurostagEchExportConfig config) {
        // avoid buses connected to a switch because of Eurostag LF crash (LU factorisation issue)
        Set<String> busesToAvoid = new HashSet<>();
        for (VoltageLevel vl : network.getVoltageLevels()) {
            for (Switch s : EchUtil.getSwitches(vl, config)) {
                busesToAvoid.add(EchUtil.getBus1(vl, s.getId(), config).getId());
                busesToAvoid.add(EchUtil.getBus2(vl, s.getId(), config).getId());
            }
        }
        Bus bus = selectSlackbusCriteria1(network, config, busesToAvoid);
        if (bus == null) {
            bus = selectSlackbusCriteria1(network, config, Collections.emptySet());
        }
        return bus;
    }

    private static Bus selectSlackbusCriteria1(Network network, EurostagEchExportConfig config, Set<String> busesToAvoid) {
         return StreamSupport.stream(EchUtil.getBuses(network, config).spliterator(), false)
                .sorted((b1, b2) -> b1.getId().compareTo(b2.getId()))
                .filter(b -> !busesToAvoid.contains(b.getId())
                        && b.getConnectedComponent() != null && b.getConnectedComponent().getNum() == ConnectedComponent.MAIN_CC_NUM)
                 .map(b -> decorate(b))
                 .filter(db -> db.regulatingGenerator > 0 && db.maxP > 100) // only keep bus with a regulating generator and a pmax > 100 MW
                 .sorted((db1, db2) -> Float.compare((db1.maxP - db1.minP) / 2 - db1.targetP, (db2.maxP - db2.minP) / 2 - db2.targetP)) // select first bus with a high margin
                 .limit(1)
                .map(f -> f.bus)
                .findFirst()
                .get();
    }

}
