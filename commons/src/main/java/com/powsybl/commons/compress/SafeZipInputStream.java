/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.compress;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.powsybl.commons.io.ForwardingInputStream;

public class SafeZipInputStream extends ForwardingInputStream<ZipInputStream> {

    private long bytesRead;
    private int entriesRead;
    private final long maxBytesToRead;
    private final int maxEntriesToRead;

    public SafeZipInputStream(ZipInputStream in, int maxEntriesToRead, long maxBytesToRead) {
        super(in);
        this.maxEntriesToRead = maxEntriesToRead;
        this.maxBytesToRead = maxBytesToRead;
    }

    public ZipEntry getNextEntry() throws IOException {
        if (++this.entriesRead > this.maxEntriesToRead) {
            throw new IOException("Max entries to read exceeded");
        }
        return this.getDelegate().getNextEntry();
    }

    @Override
    public int read() throws IOException {
        int byteRead = super.read();
        if (byteRead != -1 && ++this.bytesRead > this.maxBytesToRead) {
            throw new IOException("Max bytes to read exceeded");
        }
        return byteRead;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int byteRead = super.read(b, off, len);
        if (byteRead != -1) {
            this.bytesRead += byteRead;
            if (this.bytesRead > this.maxBytesToRead) {
                throw new IOException("Max bytes to read exceeded");
            }
        }
        return byteRead;
    }
}
