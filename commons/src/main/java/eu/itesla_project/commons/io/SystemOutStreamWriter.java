/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.io;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * A System.out to writer adapter that does not close the stream.
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SystemOutStreamWriter extends OutputStreamWriter {

    public SystemOutStreamWriter() {
        super(System.out);
    }
    @Override
    public void close() throws IOException {
        flush();
    }
}
