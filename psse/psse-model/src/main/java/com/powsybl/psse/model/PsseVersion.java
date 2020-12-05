/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Mathieu BAGUE {@literal <mathieu.bague at rte-france.com>}
 */
public enum PsseVersion {
    VERSION_33(33),
    VERSION_35(35);

    private static final Map<Integer, PsseVersion> BY_NUMBER = Arrays.stream(values())
            .collect(Collectors.toMap(PsseVersion::getNumber, Function.identity()));

    private static final String ALL_VERSIONS = Arrays.stream(values())
            .map(PsseVersion::getNumber)
            .sorted()
            .map(String::valueOf)
            .collect(Collectors.joining(", "));

    private final int number;

    PsseVersion(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public static PsseVersion fromNumber(int number) {
        return BY_NUMBER.get(number);
    }

    public static String supportedVersions() {
        return ALL_VERSIONS;
    }

    public static boolean isSupported(int rev) {
        return BY_NUMBER.containsKey(rev);
    }
}
