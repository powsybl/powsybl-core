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
public class CaseIdentificationData {

    @Parsed
    private int ic;

    @Parsed
    private double sbase;

    @Parsed
    private int rev;

    @Parsed
    private double xfrrat;

    @Parsed
    private double nxfrat;

    @Parsed
    private double basfrq;

    @Parsed
    private String title1;

    @Parsed
    private String title2;
}
