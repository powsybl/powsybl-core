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
public class BusData {

    @Parsed
    private int i;

    @Parsed
    private String name;

    @Parsed
    private double baskv;

    @Parsed
    private int ide;

    @Parsed
    private int area;

    @Parsed
    private int zone;

    @Parsed
    private int owner;

    @Parsed
    private double vm;

    @Parsed
    private double va;

    @Parsed
    private double nvhi;

    @Parsed
    private double nvlo;

    @Parsed
    private double evhi;

    @Parsed
    private double evlo;
}
