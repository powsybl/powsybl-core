/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.simulation.securityindexes;

import java.io.Serializable;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityIndexId implements Serializable, Comparable<SecurityIndexId> {

    public static final String SIMULATION_PREFIX = "SIM_";
    public static final String SECURITY_INDEX_SEPARATOR = "__IDX__";

    private final String contingencyId;

    private final SecurityIndexType securityIndexType;

    public SecurityIndexId(String contingencyId, SecurityIndexType securityIndexType) {
        Objects.requireNonNull(contingencyId);
        Objects.requireNonNull(securityIndexType);
        this.contingencyId = contingencyId;
        this.securityIndexType = securityIndexType;
    }

    public String getContingencyId() {
        return contingencyId;
    }

    public SecurityIndexType getSecurityIndexType() {
        return securityIndexType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SecurityIndexId) {
            SecurityIndexId other = (SecurityIndexId) obj;
            return contingencyId.equals(other.contingencyId)
                    && securityIndexType == other.securityIndexType;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(contingencyId, securityIndexType);
    }

    public static String toString(String contingencyId, SecurityIndexType securityIndexType) {
        try {
            String enc = StandardCharsets.UTF_8.toString();
            return SIMULATION_PREFIX + URLEncoder.encode(contingencyId, enc)
                    + SECURITY_INDEX_SEPARATOR + URLEncoder.encode(securityIndexType.getLabel(), enc);
        } catch (UnsupportedEncodingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static SecurityIndexId fromString(String str) {
        try {
            String enc = StandardCharsets.UTF_8.toString();
            int sepIdx = str.indexOf(SECURITY_INDEX_SEPARATOR);
            String contingencyId = URLDecoder.decode(str.substring(SIMULATION_PREFIX.length(), sepIdx), enc);
            String securityIndexLabel = URLDecoder.decode(str.substring(sepIdx + SECURITY_INDEX_SEPARATOR.length()), enc);
            return new SecurityIndexId(contingencyId, SecurityIndexType.fromLabel(securityIndexLabel));
        } catch (UnsupportedEncodingException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String toString() {
        return toString(contingencyId, securityIndexType);
    }

    @Override
    public int compareTo(SecurityIndexId o) {
        int c = contingencyId.compareTo(o.contingencyId);
        if (c == 0) {
            c = securityIndexType.compareTo(o.securityIndexType);
        }
        return c;
    }

}
