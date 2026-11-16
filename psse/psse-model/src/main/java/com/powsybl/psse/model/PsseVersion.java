/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Mathieu BAGUE {@literal <mathieu.bague at rte-france.com>}
 */
public final class PsseVersion {
    private static final int MAJOR_FACTOR = 100;
    static final PsseVersion MAX_VERSION = PsseVersion.fromRevision(Revision.MAX_REVISION);
    private static final PsseVersion VERSION_32 = new PsseVersion(32 * MAJOR_FACTOR);
    private static final PsseVersion VERSION_33 = new PsseVersion(33 * MAJOR_FACTOR);
    private static final PsseVersion VERSION_35 = new PsseVersion(35 * MAJOR_FACTOR);
    private static final List<PsseVersion> SUPPORTED_VERSIONS = Arrays.asList(VERSION_32, VERSION_33, VERSION_35);
    private static final Set<Integer> SUPPORTED_MAJORS = SUPPORTED_VERSIONS.stream().map(PsseVersion::getMajorNumber).collect(Collectors.toSet());
    private static final String STR_SUPPORTED_MAJORS = SUPPORTED_MAJORS.stream()
        .sorted()
        .map(Object::toString)
        .collect(Collectors.joining(", "));

    private final int number;

    private PsseVersion(int number) {
        this.number = number;
    }

    private PsseVersion(float revision) {
        number = (int) (revision * 100);
    }

    public int getMajorNumber() {
        return number / MAJOR_FACTOR;
    }

    public int getMinorNumber() {
        return number % MAJOR_FACTOR;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return getMinorNumber() != 0 ? String.format("%d.%d", getMajorNumber(), getMinorNumber()) : String.format("%d", getMajorNumber());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PsseVersion that = (PsseVersion) o;
        return number == that.number;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    public static PsseVersion fromRevision(float revisionNumber) {
        return new PsseVersion(revisionNumber);
    }

    public static String supportedVersions() {
        return STR_SUPPORTED_MAJORS;
    }

    public boolean isSupported() {
        return SUPPORTED_MAJORS.contains(getMajorNumber());
    }

    public Major major() {
        return Major.fromNumber(getMajorNumber());
    }

    public enum Major {
        V32(32),
        V33(33),
        V35(35);

        private static final Map<Integer, Major> BY_NUMBER = Arrays.stream(values())
            .collect(Collectors.toMap(Major::getNumber, Function.identity()));

        private static Major fromNumber(int major) {
            return BY_NUMBER.get(major);
        }

        Major(int major) {
            this.number = major;
        }

        private int getNumber() {
            return number;
        }

        int number;
    }
}
