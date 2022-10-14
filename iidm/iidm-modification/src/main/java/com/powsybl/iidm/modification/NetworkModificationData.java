/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.Network;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public interface NetworkModificationData<D extends NetworkModificationData<D, M>, M extends NetworkModification> {

    String getName();

    void write(Path path);

    void write(OutputStream os);

    void update(Path path);

    void update(InputStream is);

    void copy(D data);

    M toModification();

    M toModification(Network network);
}
