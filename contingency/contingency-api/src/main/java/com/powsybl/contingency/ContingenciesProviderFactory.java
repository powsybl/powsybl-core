/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface ContingenciesProviderFactory {

    ContingenciesProvider create();

    default ContingenciesProvider create(Path contingenciesFile) {
        return create();
    }

    default ContingenciesProvider create(InputStream data) {
        return create();
    }

}
