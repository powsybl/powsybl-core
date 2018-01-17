/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.security.LimitViolation;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class ExtraValues extends AbstractExtension<LimitViolation> {

    private final float extraValue1;

    private final float extraValue2;

    public ExtraValues(float extraValue1, float extraValue2) {
        this.extraValue1 = extraValue1;
        this.extraValue2 = extraValue2;
    }

    @Override
    public String getName() {
        return "ExtraValues";
    }

    public float getExtraValue1() {
        return extraValue1;
    }

    public float getExtraValue2() {
        return extraValue2;
    }
}
