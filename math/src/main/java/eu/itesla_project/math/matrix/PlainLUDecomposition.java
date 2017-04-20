/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.math.matrix;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PlainLUDecomposition implements LUDecomposition {

    private final Jama.LUDecomposition decomposition;

    public PlainLUDecomposition(Jama.LUDecomposition decomposition) {
        this.decomposition = Objects.requireNonNull(decomposition);
    }

    @Override
    public void solve(double[] b) {
        Jama.Matrix x = decomposition.solve(new Jama.Matrix(b, b.length));
        System.arraycopy(x.getColumnPackedCopy(), 0, b, 0, b.length);
    }

    @Override
    public void solve(PlainMatrix b) {
        Jama.Matrix x = decomposition.solve(b.toJamaMatrix());
        System.arraycopy(x.getColumnPackedCopy(), 0, b.getValues(), 0, b.getValues().length);
    }

    @Override
    public void close() {
    }
}
