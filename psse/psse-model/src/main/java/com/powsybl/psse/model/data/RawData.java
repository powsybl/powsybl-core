/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.psse.model.PsseRawModel;
import com.powsybl.psse.model.PsseVersion;

import java.io.IOException;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public interface RawData {

    boolean isValidFile(ReadOnlyDataSource dataSource, String extension) throws IOException;

    PsseVersion readVersion(ReadOnlyDataSource dataSource, String extension) throws IOException;

    PsseRawModel read(ReadOnlyDataSource dataSource, String ext, Context context) throws IOException;
}
