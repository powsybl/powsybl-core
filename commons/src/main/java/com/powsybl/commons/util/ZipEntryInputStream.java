/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.util;

import java.io.IOException;
import java.io.InputStream;

import com.powsybl.commons.io.ForwardingInputStream;

import net.java.truevfs.comp.zip.ZipFile;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public final class ZipEntryInputStream extends ForwardingInputStream<InputStream> {

    private final ZipFile zipFile;

    public ZipEntryInputStream(ZipFile zipFile, String fileName) throws IOException {
        super(zipFile.getInputStream(fileName));
        this.zipFile = zipFile;
    }

    @Override
    public void close() throws IOException {
        super.close();

        zipFile.close();
    }
}
