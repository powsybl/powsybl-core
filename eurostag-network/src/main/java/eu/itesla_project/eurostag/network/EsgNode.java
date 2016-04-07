/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag.network;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EsgNode {

    private final Esg2charName area; // zone identifier
    private final Esg8charName name; // node name
    private final float vbase; // base voltage [kV]
    private final float vinit; // Initial voltage [p.u.]
    private final float vangl; // Initial angle of the voltage [deg]
    private final boolean slackBus;

    public EsgNode(Esg2charName area, Esg8charName name, float vbase, float vinit, float vangl, boolean slackBus) {
        this.area = Objects.requireNonNull(area);
        this.name = Objects.requireNonNull(name);
        this.vbase = vbase;
        this.vinit = vinit;
        this.vangl = vangl;
        this.slackBus = slackBus;
    }

    public Esg2charName getArea() {
        return area;
    }

    public Esg8charName getName() {
        return name;
    }

    public float getVangl() {
        return vangl;
    }

    public float getVbase() {
        return vbase;
    }

    public float getVinit() {
        return vinit;
    }

    public boolean isSlackBus() {
        return slackBus;
    }
}
