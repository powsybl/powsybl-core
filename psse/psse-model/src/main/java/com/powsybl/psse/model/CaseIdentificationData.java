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
    int ic;

    @Parsed
    double sbase;

    @Parsed
    int rev;

    @Parsed
    double xfrrat;

    @Parsed
    double nxfrat;

    @Parsed
    double basfrq;

    @Parsed
    String title1;

    @Parsed
    String title2;
}
