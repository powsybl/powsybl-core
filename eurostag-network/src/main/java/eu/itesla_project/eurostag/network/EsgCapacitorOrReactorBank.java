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
public class EsgCapacitorOrReactorBank {

    public enum RegulatingMode {
        NOT_REGULATING
    }

    private final Esg8charName znamba; // bank name
    private final Esg8charName znodba; // connection node name
    private final int ieleba; // number of steps in service
    private final float plosba; // active loss on each step [kW]
    private final float rcapba; // reactive power of each step [Mvar] positive for capacitors negative for reactors
    private final int imaxba; // maximum number of steps
    private final RegulatingMode xregba; // regulating mode
                                         // ‘N‘ 	: not regulating

    public EsgCapacitorOrReactorBank(Esg8charName znamba, Esg8charName znodba, int ieleba, float plosba, float rcapba, int imaxba, RegulatingMode xregba) {
        this.znamba = Objects.requireNonNull(znamba);
        this.znodba = Objects.requireNonNull(znodba);
        this.ieleba = ieleba;
        this.plosba = plosba;
        this.rcapba = rcapba;
        this.imaxba = imaxba;
        this.xregba = Objects.requireNonNull(xregba);
    }

    public Esg8charName getZnamba() {
        return znamba;
    }

    public Esg8charName getZnodba() {
        return znodba;
    }
    public int getIeleba() {
        return ieleba;
    }

    public int getImaxba() {
        return imaxba;
    }

    public float getPlosba() {
        return plosba;
    }

    public float getRcapba() {
        return rcapba;
    }

    public RegulatingMode getXregba() {
        return xregba;
    }

}
