/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag;

import eu.itesla_project.simulation.SimulationState;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class EurostagState implements SimulationState {

    private final String name;

    private final byte[] sacGz;

    private final byte[] dictGensCsv;

    EurostagState(String name, byte[] sacGz, byte[] dictGensCsv) {
        this.name = name;
        this.sacGz = sacGz;
        this.dictGensCsv = dictGensCsv;
    }

    @Override
    public String getName() {
        return name;
    }

    byte[] getSacGz() {
        return sacGz;
    }

    public byte[] getDictGensCsv() {
        return dictGensCsv;
    }
}
