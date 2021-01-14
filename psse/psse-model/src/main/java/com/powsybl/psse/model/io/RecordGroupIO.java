/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
interface RecordGroupIO<T> {
    List<T> read(BufferedReader reader, Context context) throws IOException;

    void write(List<T> psseObjects, Context context, OutputStream outputStream);

    T readHead(BufferedReader reader, Context context) throws IOException;

    void writeHead(T psseObject, Context context, OutputStream outputStream);
}
