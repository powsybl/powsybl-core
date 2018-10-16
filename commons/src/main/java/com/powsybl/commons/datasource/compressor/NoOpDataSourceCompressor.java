/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource.compressor;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NoOpDataSourceCompressor implements DataSourceCompressor {

    @Override
    public String getExtension() {
        return "";
    }

    @Override
    public InputStream uncompress(InputStream is) {
        return is;
    }

    @Override
    public OutputStream compress(OutputStream os) {
        return os;
    }
}
