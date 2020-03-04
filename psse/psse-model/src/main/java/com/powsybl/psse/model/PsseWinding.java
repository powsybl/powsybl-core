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
public class PsseWinding {

    @Parsed
    private double windv;

    @Parsed
    private double nomv;

    @Parsed
    private double ang;

    @Parsed
    private double rata;

    @Parsed
    private double ratb;

    @Parsed
    private double ratc;

    @Parsed
    private int cod;

    @Parsed
    private int cont;

    @Parsed
    private double rma;

    @Parsed
    private double rmi;

    @Parsed
    private double vma;

    @Parsed
    private double vmi;

    @Parsed
    private int ntp;

    @Parsed
    private int tab;

    @Parsed
    private double cr;

    @Parsed
    private double cx;

    @Parsed
    private double cnxa;
}
