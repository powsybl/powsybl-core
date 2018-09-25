/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class ImportOptions {

    private boolean throwExceptionIfExtensionNotFound;

    public ImportOptions() {
        this(false);
    }

    public ImportOptions(boolean throwExceptionIfExtensionNotFound) {
        this.throwExceptionIfExtensionNotFound = throwExceptionIfExtensionNotFound;
    }

    public boolean isThrowExceptionIfExtensionNotFound() {
        return throwExceptionIfExtensionNotFound;
    }

    public void setThrowExceptionIfExtensionNotFound(boolean throwExceptionIfExtensionNotFound) {
        this.throwExceptionIfExtensionNotFound = throwExceptionIfExtensionNotFound;
    }

}
