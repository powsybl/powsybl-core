/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MemDataSource extends ReadOnlyMemDataSource implements DataSource {

    @Override
    public OutputStream newOutputStream(final String suffix, final String ext, boolean append) throws IOException {
        return newOutputStream(DataSourceUtil.getFileName("", suffix, ext), append);
    }

    @Override
    public OutputStream newOutputStream(String fileName, boolean append) throws IOException {
        Objects.requireNonNull(fileName);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        if (append) {
            byte[] ba = getData(fileName);
            if (ba != null) {
                os.write(ba, 0, ba.length);
            }
        }
        return new ObservableOutputStream(os, fileName, new DefaultDataSourceObserver() {
            @Override
            public void closed(String streamName) {
                putData(fileName, os.toByteArray());
            }
        });
    }


}
