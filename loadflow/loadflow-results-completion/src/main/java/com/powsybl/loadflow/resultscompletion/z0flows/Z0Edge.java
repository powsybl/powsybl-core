/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.resultscompletion.z0flows;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

import org.jgrapht.graph.DefaultWeightedEdge;

import com.powsybl.iidm.network.Line;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class Z0Edge extends DefaultWeightedEdge {

    public Z0Edge(Line line) {
        this.line = Objects.requireNonNull(line);
    }

    public Line getLine() {
        return line;
    }

    @Override
    protected double getWeight() {
        return line.getX();
    }

    // No serialization
    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new NotSerializableException();
    }

    private void readObject(ObjectInputStream in) throws IOException {
        throw new NotSerializableException();
    }

    private final transient Line line;
}
