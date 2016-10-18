/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.wca;

import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.util.ConnectedComponents;
import eu.itesla_project.iidm.network.util.Identifiables;
import eu.itesla_project.entsoe.util.BoundaryPoint;
import eu.itesla_project.entsoe.util.BoundaryPointXlsParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class StochasticInjection {

    public enum Type {
        GENERATOR('G'),
        LOAD('L'),
        DANGLING_LINE('L');

        private char c;

        Type(char c) {
            this.c = c;
        }

        public char toChar() {
            return c;
        }
    }

    private static class GeneratorWrapper extends StochasticInjection {

        private final Generator generator;

        private GeneratorWrapper(Generator generator) {
            this.generator = generator;
        }

        @Override
        public String getId() {
            return generator.getId();
        }

        @Override
        public float getP() {
            return generator.getTargetP();
        }

        @Override
        public Type getType() {
            return Type.GENERATOR;
        }

    }

    private static class LoadWrapper extends StochasticInjection {

        private final Load load;

        private LoadWrapper(Load load) {
            this.load = load;
        }

        @Override
        public String getId() {
            return load.getId();
        }

        @Override
        public float getP() {
            return load.getP0();
        }

        @Override
        public Type getType() {
            return Type.LOAD;
        }

    }

    private static class DanglingLineWrapper extends StochasticInjection {

        private final DanglingLine danglingLine;

        private DanglingLineWrapper(DanglingLine danglingLine) {
            this.danglingLine = danglingLine;
        }

        @Override
        public String getId() {
            return danglingLine.getId();
        }

        @Override
        public float getP() {
            return danglingLine.getP0();
        }

        @Override
        public Type getType() {
            return Type.DANGLING_LINE;
        }

    }

    public static List<StochasticInjection> create(Network network, boolean onlyMainCC, boolean onlyIntermittentGeneration,
                                                   boolean withBoundaries, Set<Country> boundariesFilter) throws IOException {
        List<StochasticInjection> injections = new ArrayList<>(network.getLoadCount());

        // all loads
        for (Load l : Identifiables.sort(network.getLoads())) {
            if (l.getLoadType() == LoadType.FICTITIOUS) { // skip fictitious loads
                continue;
            }
            if (onlyMainCC) {
                Terminal t = l.getTerminal();
                Bus bus = t.getBusView().getBus();
                int ccNum = ConnectedComponents.getCcNum(bus);
                // skip loads not in the main connected component
                if (ccNum != ConnectedComponent.MAIN_CC_NUM) {
                    continue;
                }
            }
            injections.add(new LoadWrapper(l));
        }

        if (withBoundaries) {
            Map<String, BoundaryPoint> boundaryPoints = null;

            // dangling lines
            for (DanglingLine dl : Identifiables.sort(network.getDanglingLines())) {
                if (onlyMainCC) {
                    Terminal t = dl.getTerminal();
                    Bus bus = t.getBusView().getBus();
                    int ccNum = ConnectedComponents.getCcNum(bus);
                    // skip loads not in the main connected component
                    if (ccNum != ConnectedComponent.MAIN_CC_NUM) {
                        continue;
                    }
                }
                boolean include = false;
                if (boundariesFilter == null) {
                    include = true;
                } else {
                    if (boundaryPoints == null) {
                        boundaryPoints = new BoundaryPointXlsParser().parseDefault();
                    }
                    BoundaryPoint boundaryPoint = boundaryPoints.get(dl.getUcteXnodeCode());
                    if (boundaryPoint != null && (boundariesFilter.contains(boundaryPoint.getBorderFrom()) || boundariesFilter.contains(boundaryPoint.getBorderTo()))) {
                        include = true;
                    }
                }
                if (include) {
                    injections.add(new DanglingLineWrapper(dl));
                }
            }
        }

        // just intermittent generation
        for (Generator g : Identifiables.sort(network.getGenerators())) {
            if (onlyIntermittentGeneration && !g.getEnergySource().isIntermittent()) {
                continue;
            }
            if (onlyMainCC) {
                Terminal t = g.getTerminal();
                Bus bus = t.getBusView().getBus();
                int ccNum = ConnectedComponents.getCcNum(bus);
                // skip generators not in the main connected component
                if (ccNum != ConnectedComponent.MAIN_CC_NUM) {
                    continue;
                }
            }
            injections.add(new GeneratorWrapper(g));
        }

        return injections;
    }

    public abstract String getId();

    public abstract float getP();

    public abstract Type getType();

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StochasticInjection && ((StochasticInjection) obj).getId().equals(getId());
    }

    @Override
    public String toString() {
        return getId();
    }

}
