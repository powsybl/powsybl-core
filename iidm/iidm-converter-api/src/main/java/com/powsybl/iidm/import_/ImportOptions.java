/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class ImportOptions {

    private boolean throwExceptionIfExtensionNotFound = false;

    private boolean importFromBaseAndExtensionsFiles = false;

    private boolean importFromBaseAndMultipleExtensionFiles = false;

    private List<String> extensions = new ArrayList<>();

    public ImportOptions(boolean throwExceptionIfExtensionNotFound, boolean importFromBaseAndExtensionsFiles, boolean importFromBaseAndMultipleExtensionFiles, List<String> extensions) {
        this.throwExceptionIfExtensionNotFound = throwExceptionIfExtensionNotFound;
        this.importFromBaseAndExtensionsFiles = importFromBaseAndExtensionsFiles;
        this.importFromBaseAndMultipleExtensionFiles = importFromBaseAndMultipleExtensionFiles;
        this.extensions = extensions;
    }

    public ImportOptions() {
    }

    public ImportOptions setExtensions(List<String> extensions) {
        this.extensions = extensions;
        return this;
    }

    public ImportOptions setImportFromBaseAndMultipleExtensionFiles(boolean importFromBaseAndMultipleExtensionFiles) {
        this.importFromBaseAndMultipleExtensionFiles = importFromBaseAndMultipleExtensionFiles;
        return this;
    }

    public ImportOptions(boolean throwExceptionIfExtensionNotFound) {
        this.throwExceptionIfExtensionNotFound = throwExceptionIfExtensionNotFound;
    }

    public ImportOptions setImportFromBaseAndExtensionsFiles(boolean importFromBaseAndExtensionsFiles) {
        this.importFromBaseAndExtensionsFiles = importFromBaseAndExtensionsFiles;
        return this;
    }

    public boolean isThrowExceptionIfExtensionNotFound() {
        return throwExceptionIfExtensionNotFound;
    }

    public boolean isImportFromBaseAndExtensionsFiles() {
        return this.importFromBaseAndExtensionsFiles;
    }

    public boolean isImportFromBaseAndMultipleExtensionFiles() {
        return this.importFromBaseAndMultipleExtensionFiles;
    }

    public ImportOptions setThrowExceptionIfExtensionNotFound(boolean throwExceptionIfExtensionNotFound) {
        this.throwExceptionIfExtensionNotFound = throwExceptionIfExtensionNotFound;
        return this;
    }

    public boolean isExtensionsEmpty() {
        return extensions.isEmpty();
    }

}
