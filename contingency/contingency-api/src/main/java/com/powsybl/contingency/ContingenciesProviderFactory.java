/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ContingenciesProviderFactory {

    <T extends ContingenciesProvider> T create();

    default <T extends ContingenciesProvider> T create(Path contingenciesFile) {
        return create();
    }

    default <T extends ContingenciesProvider> T create(InputStream data) {
        return create();
    }

}
