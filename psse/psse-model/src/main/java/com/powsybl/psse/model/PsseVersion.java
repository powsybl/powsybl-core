/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
    private static final int MINOR_FACTOR = 100;

    public static final PsseVersion VERSION_33 = new PsseVersion(33 * MAJOR_FACTOR);
    public static final PsseVersion VERSION_35 = new PsseVersion(35 * MAJOR_FACTOR);
    static final PsseVersion MAX_VERSION = PsseVersion.fromRevision(Revision.MAX_REVISION);

    private static final List<PsseVersion> SUPPORTED_VERSIONS = Arrays.asList(VERSION_33, VERSION_35);
    private static final Set<Integer> SUPPORTED_MAJORS = SUPPORTED_VERSIONS.stream().map(PsseVersion::getMajor).collect(Collectors.toSet());
    private static final String STR_SUPPORTED_MAJORS = SUPPORTED_MAJORS.stream()
        .sorted()
        .map(v -> v.toString())
        .collect(Collectors.joining(", "));

    private final int number;

    private PsseVersion(int number) {
        this.number = number;
    }

    public int getMajor() {
        return number / MAJOR_FACTOR;
    }

    public int getNumber() {
        return number;
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
        return new PsseVersion(numberFromRevision(revisionNumber));
    }

    public static String supportedVersions() {
        return STR_SUPPORTED_MAJORS;
    }

    public boolean isSupported() {
        return SUPPORTED_MAJORS.contains(getMajor());
    }

    private static int numberFromRevision(float revision) {
        int major = (int) Math.floor(revision);
        int minor = ((int) revision - major) * MINOR_FACTOR;
        return major * MAJOR_FACTOR + minor;
    }

    // XXX(Luma) Temporal solution while removing too many references to versions

    public Major major() {
        return Major.fromNumber(getMajor());
    }

    public enum Major {
        V33(33),
        V35(35);
        int number;

        private static final Map<Integer, Major> BY_NUMBER = Arrays.stream(values())
            .collect(Collectors.toMap(Major::getNumber, Function.identity()));

        private static final Major fromNumber(int major) {
            return BY_NUMBER.get(major);
        }

        private Major(int major) {
            this.number = major;
        }

        private int getNumber() {
            return number;
        }
    }
}
