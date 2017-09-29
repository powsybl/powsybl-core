/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Forwards calls to a given output stream. Subclasses of this class might
 * override some of these methods.
 *
 * @param <O> the kind of output stream forwarded
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ForwardingOutputStream<O extends OutputStream> extends OutputStream {

    protected final O os;

    public ForwardingOutputStream(O os) {
        this.os = os;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        os.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        os.write(b);
    }

    @Override
    public void write(int b) throws IOException {
        os.write(b);
    }

    @Override
    public void flush() throws IOException {
        os.flush();
    }

    @Override
    public void close() throws IOException {
        os.close();
    }

}
