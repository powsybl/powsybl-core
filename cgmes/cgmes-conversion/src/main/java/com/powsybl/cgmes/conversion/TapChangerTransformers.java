/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TapChangerTransformers {

    public TapChangerTransformers() {
        transformers2 = new HashMap<>();
        transformers3 = new HashMap<>();
        sides = new HashMap<>();
    }

    // The transformer could be 2 or 3 windings
    // The common class is Connectable
    // But we do not like to use Connectable<?>

    public TwoWindingsTransformer transformer2(String tc) {
        return transformers2.get(tc);
    }

    public ThreeWindingsTransformer transformer3(String tc) {
        return transformers3.get(tc);
    }

    public void add(String tc, TwoWindingsTransformer tx, int side) {
        transformers2.put(tc, tx);
        sides.put(tc, side);
    }

    public void add(String tc, ThreeWindingsTransformer tx, int side) {
        transformers3.put(tc, tx);
        sides.put(tc, side);
    }

    public int whichSide(String tc) {
        return sides.get(tc);
    }

    private final Map<String, TwoWindingsTransformer> transformers2;
    private final Map<String, ThreeWindingsTransformer> transformers3;
    private final Map<String, Integer> sides;
}
