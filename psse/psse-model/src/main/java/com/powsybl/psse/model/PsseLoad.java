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
public class PsseLoad {

    @Parsed
    private int i;

    @Parsed
    private String id;

    @Parsed
    private int status;

    @Parsed
    private int area;

    @Parsed
    private int zone;

    @Parsed
    private double pl;

    @Parsed
    private double ql;

    @Parsed
    private double ip;

    @Parsed
    private double iq;

    @Parsed
    private double yp;

    @Parsed
    private double yq;

    @Parsed
    private double owner;

    @Parsed
    private double scale;

    @Parsed
    private double intrpt;
}
