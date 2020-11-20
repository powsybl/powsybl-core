/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class PsseConstants {

    private PsseConstants() {
    }

    public enum PsseVersion {
        VERSION_33(33),
        VERSION_35(35);

        private final int number;
        private static final Map<Integer, PsseVersion> BY_NUMBER = Arrays.stream(values()).collect(Collectors.toMap(PsseVersion::getNumber, Function.identity()));

        private PsseVersion(int number) {
            this.number = number;
        }

        public static PsseVersion fromNumber(int number) {
            return BY_NUMBER.get(number);
        }

        public static Set<Integer> supportedVersions() {
            return BY_NUMBER.keySet();
        }

        public int getNumber() {
            return number;
        }
    }
}
