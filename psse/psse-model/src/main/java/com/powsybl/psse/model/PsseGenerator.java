/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PsseGenerator {

    @Parsed
    private int i;

    @Parsed
    private String id;

    @Parsed
    private double pg;

    @Parsed
    private double qg;

    @Parsed
    private double qt;

    @Parsed
    private double qb;

    @Parsed
    private double vs;

    @Parsed
    private int ireg;

    @Parsed
    private double mbase;

    @Parsed
    private double zr;

    @Parsed
    private double zx;

    @Parsed
    private double rt;

    @Parsed
    private double xt;

    @Parsed
    private int gtap;

    @Parsed
    private int stat;

    @Parsed
    private double rmpct;

    @Parsed
    private double pt;

    @Parsed
    private double pb;

    @Parsed
    private int o1;

    @Parsed
    private double f1;

    @Parsed
    private int o2;

    @Parsed
    private double f2;

    @Parsed
    private int o3;

    @Parsed
    private double f3;

    @Parsed
    private int o4;

    @Parsed
    private double f4;

    @Parsed
    private int wmod;

    @Parsed
    private double wpf;
}
