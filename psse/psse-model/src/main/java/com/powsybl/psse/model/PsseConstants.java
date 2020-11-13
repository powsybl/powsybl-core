/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        private static final Map<Integer, PsseVersion> BY_NUMBER = new HashMap<>();

        static {
            for (PsseVersion v : PsseVersion.values()) {
                BY_NUMBER.put(v.getNumber(), v);
            }
        }

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
