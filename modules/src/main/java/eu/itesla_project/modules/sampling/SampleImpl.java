/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.sampling;

import eu.itesla_project.iidm.network.*;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SampleImpl implements Sample {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleImpl.class);

    public static class InjectionSample {
        public String id;
        public float p;
        public float q;
        public InjectionSample(String id, float p, float q) {
            this.id = id;
            this.p = p;
            this.q = q;
        }
    }

    private final int id;
    private final List<InjectionSample> generators = new ArrayList<>();
    private final List<InjectionSample> loads = new ArrayList<>();
    private final List<InjectionSample> danglingLines = new ArrayList<>();

    public SampleImpl(int id) {
        this.id = id;
    }

    public void addGenerator(String id, float p, float q) {
        generators.add(new InjectionSample(id, p, q));
    }

    public void addLoad(String id, float p, float q) {
        loads.add(new InjectionSample(id, p, q));
    }

    public void addDanglingLine(String id, float p, float q) {
        danglingLines.add(new InjectionSample(id, p, q));
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void apply(Network network) {
        for (InjectionSample gs : generators) {
            Generator g = network.getGenerator(gs.id);
            if (g == null) {
                throw new RuntimeException("Generator '" + gs.id + "' not found");
            }
            // 2 cases
            //   - the generator is controlling voltage, we just sample P and
            //     V will be set by WP4.2 optimizer
            //   - the generator is not controlling voltage, we sample P and Q
            //     and WP4.2 optimizer won't touch it
            if (g.isVoltageRegulatorOn()) {
                float oldP = g.getTargetP();
                LOGGER.trace(" gen {} - P:{} -> P:{}", g.getId(), oldP, gs.p);
                g.setTargetP(-gs.p);
                g.getTerminal().setP(gs.p);
            } else {
                float oldP = g.getTargetP();
                float oldQ = g.getTargetQ();
                LOGGER.trace(" gen {} - P:{}, Q:{} -> P:{}, Q:{} ", g.getId(), oldP, oldQ, gs.p, gs.q);
                g.setTargetP(-gs.p)
                 .setTargetQ(-gs.q);
                g.getTerminal().setP(gs.p)
                               .setQ(gs.q);
            }
        }
        for (InjectionSample ls : loads) {
            Load l = network.getLoad(ls.id);
            if (l == null) {
                throw new RuntimeException("Load '" + ls.id + "' not found");
            }
            float oldP0 = l.getP0();
            float oldQ0 = l.getQ0();
            LOGGER.trace(" load {} - P:{}, Q:{} -> P:{}, Q:{} ", l.getId(), oldP0, oldQ0, ls.p, ls.q);
            l.setP0(ls.p).setQ0(ls.q);
            l.getTerminal().setP(ls.p).setQ(ls.q);
        }
        for (InjectionSample ls : danglingLines) {
            DanglingLine dl = network.getDanglingLine(ls.id);
            if (dl == null) {
                throw new RuntimeException("Dangling line '" + ls.id + "' not found");
            }
            float oldP0 = dl.getP0();
            float oldQ0 = dl.getQ0();
            LOGGER.trace(" dangling line {} - P:{}, Q:{} -> P:{}, Q:{} ", dl.getId(), oldP0, oldQ0, ls.p, ls.q);
            dl.setP0(ls.p).setQ0(ls.q);
        }
    }

    @Override
    public SampleCharacteritics getCharacteritics() {
        float loadPositiveP = 0;
        float loadPositiveQ = 0;
        float loadNegativeP = 0;
        float loadNegativeQ = 0;
        float generationP = 0;
        float generationQ = 0;
        float boundariesP = 0;
        float boundariesQ = 0;
        for (InjectionSample ls : loads) {
            if (ls.p > 0) {
                loadPositiveP += ls.p;
            } else if (ls.p < 0) {
                loadNegativeP += ls.p;
            }
            if (ls.q > 0) {
                loadPositiveQ += ls.q;
            } else if (ls.q < 0) {
                loadNegativeQ += ls.q;
            }
        }
        for (InjectionSample gs : generators) {
            generationP += gs.p;
            generationQ += gs.q;
        }
        for (InjectionSample dls : danglingLines) {
            boundariesP += dls.p;
            boundariesQ += dls.q;
        }
        return new SampleCharacteritics(loadPositiveP, loadPositiveQ, loadNegativeP, loadNegativeQ,
                                         generationP, generationQ, boundariesP, boundariesQ);
    }

}

