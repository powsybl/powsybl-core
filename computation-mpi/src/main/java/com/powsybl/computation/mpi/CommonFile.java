/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.mpi;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class CommonFile {

    private final String name;

    private final byte[] data;

    private final int chunk;

    private final boolean last;

    CommonFile(String name, byte[] data, int chunk, boolean last) {
        this.name = name;
        this.data = data;
        this.chunk = chunk;
        this.last = last;
    }

    String getName() {
        return name;
    }

    byte[] getData() {
        return data;
    }

    public int getChunk() {
        return chunk;
    }

    public boolean isLast() {
        return last;
    }

}
