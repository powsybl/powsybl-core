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
public class PsseTransformer {

    @Parsed
    private int i;

    @Parsed
    private int j;

    @Parsed
    private int k;

    @Parsed
    private String ckt;

    @Parsed
    private int cw;

    @Parsed
    private int cz;

    @Parsed
    private int cm;

    @Parsed
    private double mag1;

    @Parsed
    private double mag2;

    @Parsed
    private int nmetr;

    @Parsed
    private String name;

    @Parsed
    private int stat;

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
    private String vecgrp;

    @Parsed
    private double r12;

    @Parsed
    private double x12;

    @Parsed
    private double sbase12;

    @Parsed
    private double r23;

    @Parsed
    private double x23;

    @Parsed
    private double sbase23;

    @Parsed
    private double r31;

    @Parsed
    private double x31;

    @Parsed
    private double sbase31;

    @Parsed
    private double vmstar;

    @Parsed
    private double anstar;

    private PsseWinding winding1;

    private PsseWinding winding2;

    private PsseWinding winding3;
}
