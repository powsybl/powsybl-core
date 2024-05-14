/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.solver;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class KinsolParameters {

    private int maxIters = 200;

    private int msbset = 0; // means default value

    private int msbsetsub = 0; // means default value

    private double fnormtol = 0; // means default value

    private double scsteptol = 0; // means default value

    private boolean lineSearch = false;

    public int getMaxIters() {
        return maxIters;
    }

    public KinsolParameters setMaxIters(int maxIters) {
        this.maxIters = maxIters;
        return this;
    }

    public int getMsbset() {
        return msbset;
    }

    public KinsolParameters setMsbset(int msbset) {
        this.msbset = msbset;
        return this;
    }

    public int getMsbsetsub() {
        return msbsetsub;
    }

    public KinsolParameters setMsbsetsub(int msbsetsub) {
        this.msbsetsub = msbsetsub;
        return this;
    }

    public double getFnormtol() {
        return fnormtol;
    }

    public KinsolParameters setFnormtol(double fnormtol) {
        this.fnormtol = fnormtol;
        return this;
    }

    public double getScsteptol() {
        return scsteptol;
    }

    public KinsolParameters setScsteptol(double scsteptol) {
        this.scsteptol = scsteptol;
        return this;
    }

    public boolean isLineSearch() {
        return lineSearch;
    }

    public KinsolParameters setLineSearch(boolean lineSearch) {
        this.lineSearch = lineSearch;
        return this;
    }
}
