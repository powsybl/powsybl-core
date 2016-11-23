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
public class EsgGenerator {

    private final Esg8charName znamge; // generator name
    private final EsgConnectionStatus xgenest; // status
                                            // ‘Y‘ 	: connected
                                            // ‘N’ 	: not connected
    private final Esg8charName znodge; // connection node name
    private final float pgmin; // minimum active power [MW]
    private final float pgen; // active power [MW]
    private final float pgmax; // maximum active power [MW]
    private final float qgmin; // minimum reactive power [Mvar]
    private final float qgen; // reactive power [Mvar]
    private final float qgmax; // maximum reactive power [Mvar]
    private EsgRegulatingMode xregge; // regulating mode
    private float vregge; // voltage target
    private final Esg8charName zregnoge; // regulated node (= ZNODGE if blank)
    private final float qgensh; // Reactive sharing coefficient [%]

    public EsgGenerator(Esg8charName znamge, Esg8charName znodge, float pgmin, float pgen, float pgmax, float qgmin, float qgen, float qgmax, EsgRegulatingMode xregge, float vregge, Esg8charName zregnoge, float qgensh, EsgConnectionStatus xgenest) {
        this.znamge = Objects.requireNonNull(znamge);
        this.znodge = Objects.requireNonNull(znodge);
        this.pgmin = pgmin;
        this.pgen = pgen;
        this.pgmax = pgmax;
        this.qgmin = qgmin;
        this.qgen = qgen;
        this.qgmax = qgmax;
        this.xregge = Objects.requireNonNull(xregge);
        this.vregge = vregge;
        this.zregnoge = zregnoge;
        this.qgensh = qgensh;
        this.xgenest = xgenest;
    }

    public float getPgen() {
        return pgen;
    }

    public float getPgmax() {
        return pgmax;
    }

    public float getPgmin() {
        return pgmin;
    }

    public float getQgen() {
        return qgen;
    }

    public float getQgensh() {
        return qgensh;
    }

    public float getQgmax() {
        return qgmax;
    }

    public float getQgmin() {
        return qgmin;
    }

    public float getVregge() {
        return vregge;
    }

    public void setVregge(float vregge) {
        this.vregge = vregge;
    }

    public EsgConnectionStatus getXgenest() {
        return xgenest;
    }

    public EsgRegulatingMode getXregge() {
        return xregge;
    }

    public void setXregge(EsgRegulatingMode xregge) {
        this.xregge = xregge;
    }

    public Esg8charName getZnamge() {
        return znamge;
    }

    public Esg8charName getZnodge() {
        return znodge;
    }

    public Esg8charName getZregnoge() {
        return zregnoge;
    }
}
