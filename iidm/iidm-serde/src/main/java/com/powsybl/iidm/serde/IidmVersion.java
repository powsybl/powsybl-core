/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.powsybl.iidm.serde.IidmSerDeConstants.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public enum IidmVersion {
    V_1_0(ITESLA_DOMAIN, List.of(1, 0)),
    V_1_1(POWSYBL_DOMAIN, List.of(1, 1)),
    V_1_2(POWSYBL_DOMAIN, List.of(1, 2)),
    V_1_3(POWSYBL_DOMAIN, List.of(1, 3)),
    V_1_4(POWSYBL_DOMAIN, List.of(1, 4)),
    V_1_5(POWSYBL_DOMAIN, List.of(1, 5)),
    V_1_6(POWSYBL_DOMAIN, List.of(1, 6)),
    V_1_7(POWSYBL_DOMAIN, List.of(1, 7)),
    V_1_8(POWSYBL_DOMAIN, List.of(1, 8)),
    V_1_9(POWSYBL_DOMAIN, List.of(1, 9)),
    V_1_10(POWSYBL_DOMAIN, List.of(1, 10)),
    V_1_11(POWSYBL_DOMAIN, List.of(1, 11)),
    V_1_12(POWSYBL_DOMAIN, List.of(1, 12)),
    V_1_13(POWSYBL_DOMAIN, List.of(1, 13)),
    V_1_14(POWSYBL_DOMAIN, List.of(1, 14));

    private final String domain;
    private final List<Integer> versionArray;

    IidmVersion(String domain, List<Integer> versionArray) {
        this.domain = domain;
        this.versionArray = versionArray;
    }

    public String toString(String separator) {
        Objects.requireNonNull(separator);
        return versionArray.stream().map(Object::toString).collect(Collectors.joining(separator));
    }

    public boolean supportEquipmentValidationLevel() {
        return this.compareTo(V_1_7) >= 0;
    }

    public String getNamespaceURI() {
        return "http://www." + domain + "/schema/iidm/" + toString("_");
    }

    public String getNamespaceURI(boolean valid) {
        if (valid) {
            return getNamespaceURI();
        }
        if (this.compareTo(V_1_7) < 0) {
            throw new PowsyblException("Network in Equipment mode not supported for XIIDM version < 1.7");
        }
        return "http://www." + domain + "/schema/iidm/equipment/" + toString("_");
    }

    public String getXsd() {
        return "iidm_V" + toString("_") + ".xsd";
    }

    public String getXsd(boolean valid) {
        if (valid) {
            return getXsd();
        }
        if (this.compareTo(V_1_7) < 0) {
            throw new PowsyblException("Invalid network not supported for XIIDM version < 1.7");
        }
        return "iidm_equipment_V" + toString("_") + ".xsd";
    }

    public static IidmVersion of(String version, String separator) {
        Objects.requireNonNull(version);
        return Stream.of(IidmVersion.values())
                .filter(v -> version.equals(v.toString(separator)))
                .findFirst() // there can only be 0 or exactly 1 match
                .orElseThrow(() -> new PowsyblException("Version " + version + " is not supported."));
    }

    public static int compareVersions(String v1, String v2) {
        int[] version1 = parseVersion(v1);
        int[] version2 = parseVersion(v2);

        if (version1[0] != version2[0]) {
            return Integer.compare(version1[0], version2[0]);
        }
        int result = Integer.compare(version1[1], version2[1]);
        return result;
    }

    private static int[] parseVersion(String v) {
        String version = v.startsWith("V_") ? v.substring(2) : v;
        String[] parts = version.split("_");
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        return new int[]{major, minor};
    }

}
