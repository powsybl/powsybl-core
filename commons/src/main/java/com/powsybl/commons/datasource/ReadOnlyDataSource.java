/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface ReadOnlyDataSource {

    String getBaseName();

    boolean exists(String suffix, String ext) throws IOException;

    boolean exists(String fileName) throws IOException;

    boolean existsStrict(String suffix, String ext) throws IOException;

    InputStream newInputStream(String suffix, String ext) throws IOException;

    InputStream newInputStream(String fileName) throws IOException;

    Set<String> listNames(String regex) throws IOException;
}
