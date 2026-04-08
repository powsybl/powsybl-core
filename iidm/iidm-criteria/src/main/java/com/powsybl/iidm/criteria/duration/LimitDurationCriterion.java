/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.duration;

/**
 * Interface for limit duration criteria (both permanent and temporary)
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public interface LimitDurationCriterion {

    enum LimitDurationType {
        PERMANENT,
        TEMPORARY
    }

    LimitDurationType getType();

    String VERSION = "1.0";

    static String getVersion() {
        return VERSION;
    }
}
