/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.export;

import com.google.common.collect.Sets;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.AbstractOptions;
import com.powsybl.iidm.network.TopologyLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class ExportOptions extends AbstractOptions<ExportOptions> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportOptions.class);

    private boolean withBranchSV = true;

    private boolean indent = true;

    private boolean onlyMainCc = false;

    private boolean anonymized = false;

    private TopologyLevel topologyLevel = TopologyLevel.NODE_BREAKER;

    private boolean throwExceptionIfExtensionNotFound = false;

    private String version;

    private Map<String, String> extensionsVersions = new HashMap<>();

    public ExportOptions() {
    }

    public ExportOptions(boolean withBranchSV, boolean indent, boolean onlyMainCc, TopologyLevel topologyLevel, boolean throwExceptionIfExtensionNotFound) {
        this(withBranchSV, indent, onlyMainCc, topologyLevel, throwExceptionIfExtensionNotFound, null);
    }

    public ExportOptions(boolean withBranchSV, boolean indent, boolean onlyMainCc, TopologyLevel topologyLevel, boolean throwExceptionIfExtensionNotFound, String version) {
        this.withBranchSV = withBranchSV;
        this.indent = indent;
        this.onlyMainCc = onlyMainCc;
        this.topologyLevel = Objects.requireNonNull(topologyLevel);
        this.throwExceptionIfExtensionNotFound = throwExceptionIfExtensionNotFound;
        this.version = version;
    }

    @Override
    public ExportOptions addExtension(String extension) {
        if (extensions != null) {
            extensions.add(extension);
        } else {
            this.extensions = Sets.newHashSet(extension);
        }
        return this;
    }

    public boolean isWithBranchSV() {
        return withBranchSV;
    }

    public ExportOptions setWithBranchSV(boolean withBranchSV) {
        this.withBranchSV = withBranchSV;
        return this;
    }

    public boolean isIndent() {
        return indent;
    }

    public ExportOptions setIndent(boolean indent) {
        this.indent = indent;
        return this;
    }

    public boolean isOnlyMainCc() {
        return onlyMainCc;
    }

    public ExportOptions setOnlyMainCc(boolean onlyMainCc) {
        this.onlyMainCc = onlyMainCc;
        return this;
    }

    public boolean isAnonymized() {
        return anonymized;
    }

    public ExportOptions setAnonymized(boolean anonymized) {
        this.anonymized = anonymized;
        return this;
    }

    public TopologyLevel getTopologyLevel() {
        return topologyLevel;
    }

    public ExportOptions setTopologyLevel(TopologyLevel topologyLevel) {
        this.topologyLevel = Objects.requireNonNull(topologyLevel);
        return this;
    }

    @Override
    public ExportOptions setExtensions(Set<String> extensions) {
        // this warning is to prevent people to use setSkipExtensions and setExtensions at the same time
        Optional.ofNullable(this.extensions).ifPresent(e -> {
            if (e.isEmpty()) {
                LOGGER.warn("Extensions have already been set as empty. This call will override it.");
            }
        });
        this.extensions = extensions;
        return this;
    }

    public boolean isThrowExceptionIfExtensionNotFound() {
        return throwExceptionIfExtensionNotFound;
    }

    public ExportOptions setThrowExceptionIfExtensionNotFound(boolean throwException) {
        this.throwExceptionIfExtensionNotFound = throwException;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public ExportOptions setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * Add a given version in which the extension with the given name will be exported if
     * this version is supported by the extension's XML serializer and if it is compatible
     * with the IIDM version in which the network will be exported.
     * If the version is not added for an extension configured to be serialized, the extension will be serialized in the
     * most recent version compatible with the IIDM version in which the network will be exported.
     * If a version is added for an extension configured <b>not</b> to be serialized, the version will be ignored.
     * If a version has already been added for the extension, throw an exception.
     */
    public ExportOptions addExtensionVersion(String extensionName, String extensionVersion) {
        if (extensions != null && !extensions.contains(extensionName)) {
            throw new PowsyblException(extensionName + " is not an extension you have passed in the extensions list to export.");
        }
        if (extensionsVersions.putIfAbsent(extensionName, extensionVersion) != null) {
            throw new PowsyblException("The version of " + extensionName + "'s XML serializer has already been set.");
        }
        return this;
    }

    /**
     * Return an optional containing the version oin which the extension with the given name will be exported if it has previously been added.
     * If it has never been added, return an empty optional.
     */
    public Optional<String> getExtensionVersion(String extensionName) {
        return Optional.ofNullable(extensionsVersions.get(extensionName));
    }
}
