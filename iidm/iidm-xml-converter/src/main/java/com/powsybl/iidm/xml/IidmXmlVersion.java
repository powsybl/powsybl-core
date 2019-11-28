/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.google.common.collect.ImmutableList;
import com.powsybl.commons.PowsyblException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public enum IidmXmlVersion {
    V_1_0(ImmutableList.of(1, 0)),
    V_1_1(ImmutableList.of(1, 1));

    private final List<Integer> versionArray;

    IidmXmlVersion(List<Integer> versionArray) {
        this.versionArray = versionArray;
    }

    public String toString(String separator) {
        return versionArray.stream().map(Object::toString).collect(Collectors.joining(separator));
    }

    public String getXsd() {
        return "iidm_V" + toString("_") + ".xsd";
    }

    public static IidmXmlVersion of(String version) {
        return Stream.of(IidmXmlVersion.values())
                .filter(v -> version.equals(v.toString("_")))
                .findFirst()
                .orElseThrow(() -> new PowsyblException("Version " + version + " is not supported."));
    }
}
