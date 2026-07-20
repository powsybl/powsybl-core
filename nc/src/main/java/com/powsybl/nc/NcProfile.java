/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public enum NcProfile {
    ASSESSED_ELEMENT("AE", "AssessedElement"),
    CONTINGENCY("CO", "Contingency"),
    REMEDIAL_ACTION("RA", "RemedialAction");

    private final String keyword;
    private final String fullName;

    NcProfile(String keyword, String fullName) {
        this.keyword = keyword;
        this.fullName = fullName;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getFullName() {
        return fullName;
    }

    public static Optional<NcProfile> fromKeyword(String keyword) {
        return Arrays.stream(NcProfile.values()).filter(ncProfile -> keyword.equals(ncProfile.getKeyword())).findFirst();
    }
}
