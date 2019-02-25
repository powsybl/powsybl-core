/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.export;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.AbstractOptions;
import com.powsybl.iidm.IidmImportExportMode;
import com.powsybl.iidm.network.TopologyLevel;

import java.util.Objects;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class ExportOptions extends AbstractOptions<ExportOptions> {

    private boolean withBranchSV = true;

    private boolean indent = true;

    private boolean onlyMainCc = false;

    private boolean anonymized = false;

    private boolean extensionsExplicitlySet = false;

    private TopologyLevel topologyLevel = TopologyLevel.NODE_BREAKER;

    private boolean throwExceptionIfExtensionNotFound = false;

    public ExportOptions() {
    }

    public ExportOptions(boolean withBranchSV, boolean indent, boolean onlyMainCc, TopologyLevel topologyLevel, boolean throwExceptionIfExtensionNotFound) {
        this.withBranchSV = withBranchSV;
        this.indent = indent;
        this.onlyMainCc = onlyMainCc;
        this.topologyLevel = Objects.requireNonNull(topologyLevel);
        this.throwExceptionIfExtensionNotFound = throwExceptionIfExtensionNotFound;
    }

    @Override
    public IidmImportExportMode getMode() {
        return mode;
    }

    @Override
    public ExportOptions setMode(IidmImportExportMode mode) {
        this.mode = mode;
        return this;
    }

    @Override
    public ExportOptions addExtension(String extension) {
        this.extensions.add(extension);
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
        if (this.extensions.isEmpty()) {
            throw new PowsyblException("Contradictory behavior: you have already set skipExtensions to true");
        }
        this.extensions = extensions;
        this.extensionsExplicitlySet = Boolean.TRUE;
        return this;
    }

    public boolean isThrowExceptionIfExtensionNotFound() {
        return throwExceptionIfExtensionNotFound;
    }

    public ExportOptions setThrowExceptionIfExtensionNotFound(boolean throwException) {
        this.throwExceptionIfExtensionNotFound = throwException;
        return this;
    }

    /**
     * @deprecated Use {@link #withNoExtension()} instead.
     */
    @Deprecated
    public boolean isSkipExtensions() {
        return this.extensions.isEmpty();
    }

    /**
     * @deprecated Use {@link #setExtensions(Set<String>)} instead.
     * Pass an empty Set as parameter
     */
    @Deprecated
    public ExportOptions setSkipExtensions(boolean skipExtensions) {
        if (skipExtensions && extensionsExplicitlySet) {
            throw new PowsyblException("Contradictory behavior: you have already pass an extensions list");
        }
        if (skipExtensions) {
            this.extensions.clear();
        }
        return this;
    }

}
