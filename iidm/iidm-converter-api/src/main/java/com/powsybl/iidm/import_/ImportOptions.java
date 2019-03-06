/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import com.google.common.collect.Sets;
import com.powsybl.iidm.AbstractOptions;
import com.powsybl.iidm.IidmImportExportMode;
import com.powsybl.iidm.IidmImportExportType;

import java.util.Optional;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class ImportOptions extends AbstractOptions<ImportOptions> {

    private boolean throwExceptionIfExtensionNotFound = false;

    public ImportOptions() {
    }

    @Override
    public ImportOptions setImportExportType(IidmImportExportType importExportType) {
        this.importExportType = importExportType;
        return this;
    }

    @Override
    public ImportOptions setTopo(boolean topo) {
        this.topo = topo;
        return this;
    }

    @Override
    public ImportOptions setState(boolean state) {
        this.state = state;
        return this;
    }

    @Override
    public ImportOptions setControl(boolean control) {
        this.control = control;
        return this;
    }

    public ImportOptions(boolean throwExceptionIfExtensionNotFound) {
        this.throwExceptionIfExtensionNotFound = throwExceptionIfExtensionNotFound;
    }

    @Override
    public ImportOptions setExtensions(Set<String> extensions) {
        this.extensions = Optional.ofNullable(extensions);
        return this;
    }

    public ImportOptions setThrowExceptionIfExtensionNotFound(boolean throwExceptionIfExtensionNotFound) {
        this.throwExceptionIfExtensionNotFound = throwExceptionIfExtensionNotFound;
        return this;
    }

    @Override
    public ImportOptions setMode(IidmImportExportMode mode) {
        this.mode = mode;
        return this;
    }

    @Override
    public ImportOptions addExtension(String extension) {
        if (extensions.isPresent()) {
            extensions.get().add(extension);
        } else {
            this.extensions = Optional.of(Sets.newHashSet(extension));
        }
        return this;
    }

    public boolean isThrowExceptionIfExtensionNotFound() {
        return throwExceptionIfExtensionNotFound;
    }
}
