/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.model.io;

import java.util.Objects;

/**
 * Identifies a single overridable NC property. An object may expose several overridable properties,
 * so the effective field name is part of the key.
 *
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
public record NcOverrideKey(String mrid, String effectiveFieldName) {

    public NcOverrideKey {
        Objects.requireNonNull(mrid);
        Objects.requireNonNull(effectiveFieldName);
    }

    public static NcOverrideKey of(String mrid, NcOverridingObjectsFields fields) {
        return new NcOverrideKey(mrid, fields.getEffectiveFieldName());
    }
}
