/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.export;

import com.powsybl.iidm.AbstractOptions;
import com.powsybl.iidm.ImportExportTypes;
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

    private TopologyLevel topologyLevel = TopologyLevel.NODE_BREAKER;

    private boolean throwExceptionIfExtensionNotFound = false;

    private ImportExportTypes mode = ImportExportTypes.BASE_AND_EXTENSIONS_IN_ONE_SINGLE_FILE;

    public ExportOptions() {
    }

    public ExportOptions(boolean withBranchSV, boolean indent, boolean onlyMainCc, TopologyLevel topologyLevel, boolean throwExceptionIfExtensionNotFound, Set<String> extensions) {
        super(extensions);
        this.withBranchSV = withBranchSV;
        this.indent = indent;
        this.onlyMainCc = onlyMainCc;
        this.topologyLevel = Objects.requireNonNull(topologyLevel);
        this.throwExceptionIfExtensionNotFound = throwExceptionIfExtensionNotFound;
    }

    public ImportExportTypes getMode() {
        return mode;
    }

    @Override
    public ExportOptions setMode(ImportExportTypes mode) {
        this.mode = mode;
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

    public boolean isOneFilePerExtensionType() {
        return this.mode == ImportExportTypes.BASE_AND_ONE_FILE_PER_EXTENSION_TYPE;
    }

    public boolean isSeparateBaseAndExtensions() {
        return this.mode == ImportExportTypes.BASE_AND_EXTENSIONS_FILES;
    }

}
