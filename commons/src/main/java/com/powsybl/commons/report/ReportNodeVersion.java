/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import com.powsybl.commons.PowsyblException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public enum ReportNodeVersion {
    V_1_0(List.of(1, 0)),
    V_2_0(List.of(2, 0)),
    V_2_1(List.of(2, 1));

    private final List<Integer> versionArray;

    ReportNodeVersion(List<Integer> versionArray) {
        this.versionArray = versionArray;
    }

    @Override
    public String toString() {
        return toString(".");
    }

    public String toString(String separator) {
        Objects.requireNonNull(separator);
        return versionArray.stream().map(Object::toString).collect(Collectors.joining(separator));
    }

    public static ReportNodeVersion of(String version) {
        return of(version, ".");
    }

    public static ReportNodeVersion of(String version, String separator) {
        Objects.requireNonNull(version);
        return Stream.of(ReportNodeVersion.values())
                .filter(v -> version.equals(v.toString(separator)))
                .findFirst() // there can only be 0 or exactly 1 match
                .orElseThrow(() -> new PowsyblException("Version " + version + " is not supported."));
    }
}
