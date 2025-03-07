/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.serde.IidmVersion;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface SerDeVersion<V extends SerDeVersion<V>> {

    VersionInfo getVersionInfo();

    default boolean supports(IidmVersion networkVersion) {
        return getMinIidmVersionIncluded().compareTo(networkVersion) <= 0
                && (getMaxIidmVersionExcluded() == null || getMaxIidmVersionExcluded().compareTo(networkVersion) > 0);
    }

    default VersionNumbers getVersionNumbers() {
        return getVersionInfo().versionNumbers();
    }

    default String getNamespaceUri() {
        return getVersionInfo().namespaceUri();
    }

    default IidmVersion getMinIidmVersionIncluded() {
        return getVersionInfo().minIncluded();
    }

    default IidmVersion getMaxIidmVersionExcluded() {
        return getVersionInfo().maxExcluded();
    }

    default String getVersionString() {
        return getVersionNumbers().toString();
    }

    default String getNamespacePrefix() {
        return getVersionInfo().namespacePrefix();
    }

    default String getSerializationName() {
        return getVersionInfo().serializationName();
    }

    default boolean isGreaterThan(V version) {
        return getVersionNumbers().compareTo(version.getVersionNumbers()) > 0;
    }

    default boolean isLessThan(V version) {
        return getVersionNumbers().compareTo(version.getVersionNumbers()) < 0;
    }

    default String getXsdResourcePath() {
        return getVersionInfo().xsdResourcePath();
    }

    record VersionInfo(String xsdResourcePath, String namespaceUri, String namespacePrefix, VersionNumbers versionNumbers,
                       IidmVersion minIncluded, IidmVersion maxExcluded, String serializationName
    ) {
    }

    record VersionNumbers(int major, int minor, String suffix) implements Comparable<VersionNumbers> {
        public VersionNumbers(int major, int minor) {
            this(major, minor, null);
        }

        @Override
        public String toString() {
            return major + "." + minor + (suffix == null ? "" : "-" + suffix);
        }

        @Override
        public int compareTo(VersionNumbers other) {
            if (major != other.major) {
                return major - other.major;
            }
            if (minor != other.minor) {
                return minor - other.minor;
            }
            if (suffix == null && other.suffix == null) {
                return 0;
            }
            if (suffix == null || other.suffix == null) {
                return suffix != null ? -1 : 1;
            }
            return suffix.compareTo(other.suffix);
        }
    }
}
