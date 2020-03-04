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
public class PsseNonTransformerBranch {

    @Parsed
    private int i;

    @Parsed
    private int j;

    @Parsed
    private String ckt;

    @Parsed
    private double r;

    @Parsed
    private double x;

    @Parsed
    private double b;

    @Parsed
    private double ratea;

    @Parsed
    private double rateb;

    @Parsed
    private double ratec;

    @Parsed
    private double gi;

    @Parsed
    private double bi;

    @Parsed
    private double gj;

    @Parsed
    private double bj;

    @Parsed
    private int st;

    @Parsed
    private int met;

    @Parsed
    private double len;

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
}
