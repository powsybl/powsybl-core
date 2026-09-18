/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.model;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
public final class NcVersion implements Comparable<NcVersion> {

    public static final NcVersion UNKNOWN = new NcVersion(0, 0);
    public static final NcVersion V2_2 = new NcVersion(2, 2);
    public static final NcVersion V2_3 = new NcVersion(2, 3);
    public static final NcVersion V2_4 = new NcVersion(2, 4);

    private static final Pattern PROFILE_VERSION_PATTERN = Pattern.compile(".*/(\\d+)\\.(\\d+)$");

    private final int major;
    private final int minor;

    private NcVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    public static NcVersion of(int major, int minor) {
        if (major < 0 || minor < 0) {
            throw new NcException("NC version numbers must be positive");
        }
        return new NcVersion(major, minor);
    }

    public static NcVersion fromProfileUri(String profileUri) {
        if (profileUri == null) {
            return UNKNOWN;
        }
        Matcher matcher = PROFILE_VERSION_PATTERN.matcher(profileUri);
        return matcher.matches()
            ? of(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)))
            : UNKNOWN;
    }

    public boolean isKnown() {
        return !UNKNOWN.equals(this);
    }

    public boolean isSupported() {
        return V2_2.equals(this) || V2_3.equals(this) || V2_4.equals(this);
    }

    @Override
    public int compareTo(NcVersion other) {
        int majorComparison = Integer.compare(major, other.major);
        return majorComparison != 0 ? majorComparison : Integer.compare(minor, other.minor);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof NcVersion version && major == version.major && minor == version.minor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor);
    }

    @Override
    public String toString() {
        return isKnown() ? major + "." + minor : "unknown";
    }
}
