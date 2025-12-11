/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.TopologyLevel;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.powsybl.iidm.serde.ExportOptions.IidmVersionIncompatibilityBehavior.THROW_EXCEPTION;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class ExportOptions extends AbstractOptions<ExportOptions> {

    public enum IidmVersionIncompatibilityBehavior {
        THROW_EXCEPTION,
        LOG_ERROR
    }

    public enum BusBranchVoltageLevelIncompatibilityBehavior {
        THROW_EXCEPTION,
        KEEP_ORIGINAL_TOPOLOGY
    }

    private boolean withBranchSV = true;

    private boolean indent = true;

    private boolean onlyMainCc = false;

    private boolean anonymized = false;

    private TopologyLevel topologyLevel = TopologyLevel.NODE_BREAKER;

    private boolean throwExceptionIfExtensionNotFound = false;

    private BusBranchVoltageLevelIncompatibilityBehavior busBranchVoltageLevelIncompatibilityBehavior = BusBranchVoltageLevelIncompatibilityBehavior.THROW_EXCEPTION;

    private String version;

    private IidmVersionIncompatibilityBehavior iidmVersionIncompatibilityBehavior = IidmVersionIncompatibilityBehavior.THROW_EXCEPTION;

    private final Map<String, String> extensionsVersions = new HashMap<>();

    private final Map<String, TopologyLevel> voltageLevelTopologyLevel = new HashMap<>();

    private Charset charset = StandardCharsets.UTF_8;

    /**
     * Sort IIDM objects so that generated XML does not depend on data model object order. Depending on object types the
     * following sorting key has been chosen:
     * - the id for identifiables
     * - the name for extensions
     * - the name for temporary limits
     * - node1 then node2 for internal connections
     * - the name for properties of an identifiable
     */
    private boolean sorted = false;

    private boolean withAutomationSystems = true;

    public ExportOptions() {
    }

    public ExportOptions(boolean withBranchSV, boolean indent, boolean onlyMainCc, TopologyLevel topologyLevel, boolean throwExceptionIfExtensionNotFound) {
        this(withBranchSV, indent, onlyMainCc, topologyLevel, throwExceptionIfExtensionNotFound, false);
    }

    public ExportOptions(boolean withBranchSV, boolean indent, boolean onlyMainCc, TopologyLevel topologyLevel, boolean throwExceptionIfExtensionNotFound, boolean sorted) {
        this(withBranchSV, indent, onlyMainCc, topologyLevel, throwExceptionIfExtensionNotFound, sorted, null);
    }

    public ExportOptions(boolean withBranchSV, boolean indent, boolean onlyMainCc, TopologyLevel topologyLevel, boolean throwExceptionIfExtensionNotFound, String version) {
        this(withBranchSV, indent, onlyMainCc, topologyLevel, throwExceptionIfExtensionNotFound, false, version);
    }

    public ExportOptions(boolean withBranchSV, boolean indent, boolean onlyMainCc, TopologyLevel topologyLevel, boolean throwExceptionIfExtensionNotFound, boolean sorted, String version) {
        this(withBranchSV, indent, onlyMainCc, topologyLevel, throwExceptionIfExtensionNotFound, sorted, version, THROW_EXCEPTION);
    }

    public ExportOptions(boolean withBranchSV, boolean indent, boolean onlyMainCc, TopologyLevel topologyLevel, boolean throwExceptionIfExtensionNotFound, boolean sorted, String version,
                         IidmVersionIncompatibilityBehavior iidmVersionIncompatibilityBehavior) {
        this.withBranchSV = withBranchSV;
        this.indent = indent;
        this.onlyMainCc = onlyMainCc;
        this.topologyLevel = Objects.requireNonNull(topologyLevel);
        this.throwExceptionIfExtensionNotFound = throwExceptionIfExtensionNotFound;
        this.sorted = sorted;
        this.version = version;
        this.iidmVersionIncompatibilityBehavior = Objects.requireNonNull(iidmVersionIncompatibilityBehavior);
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
    public boolean isThrowExceptionIfExtensionNotFound() {
        return throwExceptionIfExtensionNotFound;
    }

    public ExportOptions setThrowExceptionIfExtensionNotFound(boolean throwException) {
        this.throwExceptionIfExtensionNotFound = throwException;
        return this;
    }

    public IidmVersion getVersion() {
        return version != null ? IidmVersion.of(version, ".") : IidmSerDeConstants.CURRENT_IIDM_VERSION;
    }

    public ExportOptions setVersion(String version) {
        this.version = version;
        return this;
    }

    public IidmVersionIncompatibilityBehavior getIidmVersionIncompatibilityBehavior() {
        return iidmVersionIncompatibilityBehavior;
    }

    public ExportOptions setIidmVersionIncompatibilityBehavior(IidmVersionIncompatibilityBehavior iidmVersionIncompatibilityBehavior) {
        this.iidmVersionIncompatibilityBehavior = Objects.requireNonNull(iidmVersionIncompatibilityBehavior);
        return this;
    }

    public Charset getCharset() {
        return charset;
    }

    public ExportOptions setCharset(Charset charset) {
        Objects.requireNonNull(charset);
        this.charset = charset;
        return this;
    }

    public ExportOptions setBusBranchVoltageLevelIncompatibilityBehavior(
            BusBranchVoltageLevelIncompatibilityBehavior busBranchVoltageLevelIncompatibilityBehavior) {
        this.busBranchVoltageLevelIncompatibilityBehavior = busBranchVoltageLevelIncompatibilityBehavior;
        return this;
    }

    public BusBranchVoltageLevelIncompatibilityBehavior getBusBranchVoltageLevelIncompatibilityBehavior() {
        return busBranchVoltageLevelIncompatibilityBehavior;
    }

    /**
     * <p>Add a given version in which the extension with the given name will be exported if
     * this version is supported by the extension's XML serializer and if it is compatible
     * with the IIDM version in which the network will be exported.</p>
     * <ul>
     * <li>If the version is not added for an extension configured to be serialized, the extension will be serialized in the
     * most recent version compatible with the IIDM version in which the network will be exported.</li>
     * <li>If a version is added for an extension configured <b>not</b> to be serialized, the version will be ignored.</li>
     * <li>If a version has already been added for the extension, throw an exception.</li>
     * </ul>
     * <p>For extensions having multiple serialization names, only the real extension name is expected. If an alternative
     * serialization name is used for the <code>extensionName</code> parameter, the method will have no effect.</p>
     */
    public ExportOptions addExtensionVersion(String extensionName, String extensionVersion) {
        if (includedExtensions != null && !includedExtensions.contains(extensionName)) {
            throw new PowsyblException(extensionName + " is not an extension you have included in the extensions inclusion list to export.");
        }
        if (excludedExtensions != null && excludedExtensions.contains(extensionName)) {
            throw new PowsyblException(extensionName + " is an extension you have excluded in the extensions exclusion list to export.");
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

    public ExportOptions addVoltageLevelTopologyLevel(String voltageLevelId, TopologyLevel topologyLevel) {
        if (!voltageLevelId.isEmpty()) {
            voltageLevelTopologyLevel.put(voltageLevelId, topologyLevel);
        }
        return this;
    }

    public TopologyLevel getVoltageLevelTopologyLevel(String voltageLevelId) {
        return voltageLevelTopologyLevel.get(voltageLevelId);
    }

    public boolean isSorted() {
        return sorted;
    }

    public ExportOptions setSorted(boolean sorted) {
        this.sorted = sorted;
        return this;
    }

    public boolean isWithAutomationSystems() {
        return withAutomationSystems;
    }

    public ExportOptions setWithAutomationSystems(boolean withAutomationSystems) {
        this.withAutomationSystems = withAutomationSystems;
        return this;
    }
}
