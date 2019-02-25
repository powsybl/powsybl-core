/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import com.powsybl.iidm.AbstractOptions;
import com.powsybl.iidm.IidmImportExportMode;

import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class ImportOptions extends AbstractOptions<ImportOptions> {

    private boolean throwExceptionIfExtensionNotFound = false;

    public ImportOptions() {
        super();
    }

    public ImportOptions(boolean throwExceptionIfExtensionNotFound) {
        this.throwExceptionIfExtensionNotFound = throwExceptionIfExtensionNotFound;
    }

    @Override
    public ImportOptions setExtensions(Set<String> extensions) {
        this.extensions = extensions;
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
        this.extensions.add(extension);
        return this;
    }


    public boolean isThrowExceptionIfExtensionNotFound() {
        return throwExceptionIfExtensionNotFound;
    }

    public boolean isImportFromBaseAndExtensionsFiles() {
        return this.mode == IidmImportExportMode.EXTENSIONS_IN_ONE_SEPARATED_FILE;
    }

    public boolean isImportFromBaseAndMultipleExtensionFiles() {
        return this.mode == IidmImportExportMode.ONE_SEPARATED_FILE_PER_EXTENSION_TYPE;
    }






}
