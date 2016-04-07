/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.sampling;

import eu.itesla_project.iidm.network.DanglingLine;
import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.iidm.network.Load;
import eu.itesla_project.iidm.network.Network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SampleCharacteritics {

    private final float loadPositiveP;
    private final float loadPositiveQ;
    private final float loadNegativeP;
    private final float loadNegativeQ;
    private final float generationP;
    private final float generationQ;
    private final float boundariesP;
    private final float boundariesQ;

    public static SampleCharacteritics fromNetwork(Network network, boolean generationSampled, boolean boundariesSampled) {
        float loadPositiveP = 0;
        float loadPositiveQ = 0;
        float loadNegativeP = 0;
        float loadNegativeQ = 0;
        float generationP = 0;
        float generationQ = 0;
        float boundariesP = 0;
        float boundariesQ = 0;
        for (Load l : network.getLoads()) {
            if (l.getP0() > 0) {
                loadPositiveP += l.getP0();
            } else {
                loadNegativeP += l.getP0();
            }
            if (l.getQ0() > 0) {
                loadPositiveQ += l.getQ0();
            } else {
                loadNegativeQ += l.getQ0();
            }
        }
        if (generationSampled) {
            for (Generator g : network.getGenerators()) {
                if (g.getEnergySource().isIntermittent()) {
                    generationP += g.getTargetP();
                    if (!g.isVoltageRegulatorOn()) {
                        generationQ += g.getTargetQ();
                    }
                }
            }
        }
        if (boundariesSampled) {
            for (DanglingLine dl : network.getDanglingLines()) {
                boundariesP += dl.getP0();
                boundariesQ += dl.getQ0();
            }
        }
        return new SampleCharacteritics(loadPositiveP, loadPositiveQ, loadNegativeP, loadNegativeQ, generationP, generationQ, boundariesP, boundariesQ);
    }

    public SampleCharacteritics(float loadPositiveP, float loadPositiveQ, float loadNegativeP, float loadNegativeQ,
                                float generationP, float generationQ, float boundariesP, float boundariesQ) {
        this.loadPositiveP = loadPositiveP;
        this.loadPositiveQ = loadPositiveQ;
        this.loadNegativeP = loadNegativeP;
        this.loadNegativeQ = loadNegativeQ;
        this.generationP = generationP;
        this.generationQ = generationQ;
        this.boundariesP = boundariesP;
        this.boundariesQ = boundariesQ;
    }

    public float getLoadPositiveP() {
        return loadPositiveP;
    }

    public float getLoadPositiveQ() {
        return loadPositiveQ;
    }

    public float getLoadNegativeP() {
        return loadNegativeP;
    }

    public float getLoadNegativeQ() {
        return loadNegativeQ;
    }

    public float getGenerationP() {
        return generationP;
    }

    public float getGenerationQ() {
        return generationQ;
    }

    public float getBoundariesP() {
        return boundariesP;
    }

    public float getBoundariesQ() {
        return boundariesQ;
    }

    private static final float EPSILON = 1f;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SampleCharacteritics) {
            SampleCharacteritics c = (SampleCharacteritics) obj;
            return Math.abs(loadPositiveP - c.loadPositiveP) < EPSILON
                    && Math.abs(loadNegativeP - c.loadNegativeP) < EPSILON
                    && Math.abs(loadPositiveQ - c.loadPositiveQ) < EPSILON
                    && Math.abs(loadNegativeQ - c.loadNegativeQ) < EPSILON
                    && Math.abs(generationP - c.generationP) < EPSILON
                    && Math.abs(generationQ - c.generationQ) < EPSILON
                    && Math.abs(boundariesP - c.boundariesP) < EPSILON
                    && Math.abs(boundariesQ - c.boundariesQ) < EPSILON;
        }
        return false;
    }

    @Override
    public String toString() {
        return "loadP=" + loadPositiveP + " + " + loadNegativeP + " MW"
                + " , loadQ=" + loadPositiveQ + " + " + loadNegativeQ + " MVar"
                + ", generationP=" + generationP + " MW"
                + ", generationQ=" + generationQ + " MVar"
                + ", boundariesP=" + boundariesP + " MW"
                + ", boundariesQ=" + boundariesQ + " MVar";
    }

}