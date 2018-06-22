/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Scalable {

    double initialValue(Network n);

    void reset(Network n);

    double maximumValue(Network n);

    void listGenerators(Network n, List<Generator> generators, List<String> notFoundGenerators);

    List<Generator> listGenerators(Network n, List<String> notFoundGenerators);

    List<Generator> listGenerators(Network n);

    double scale(Network n, double asked);

    static GeneratorScalable gen(String id) {
        return new GeneratorScalable(id);
    }

    static Scalable scalable(String id) {
        return new ScalableAdapter(id);
    }

    static List<Scalable> scalables(String... ids) {
        return Arrays.stream(ids).map(ScalableAdapter::new).collect(Collectors.toList());
    }

    static ProportionalScalable proportional(List<Float> percentages, List<Scalable> scalables) {
        return new ProportionalScalable(percentages, scalables);
    }

    static ProportionalScalable proportional(float percentage, Scalable scalable) {
        return new ProportionalScalable(Collections.singletonList(percentage), Collections.singletonList(scalable));
    }

    static ProportionalScalable proportional(float percentage1, Scalable scalable1, float percentage2, Scalable scalable2) {
        return new ProportionalScalable(Arrays.asList(percentage1, percentage2),
                                        Arrays.asList(scalable1, scalable2));
    }

    static ProportionalScalable proportional(float percentage1, Scalable scalable1, float percentage2, Scalable scalable2, float percentage3, Scalable scalable3) {
        return new ProportionalScalable(Arrays.asList(percentage1, percentage2, percentage3),
                                        Arrays.asList(scalable1, scalable2, scalable3));
    }

    static ProportionalScalable proportional(float percentage1, Scalable scalable1, float percentage2, Scalable scalable2, float percentage3, Scalable scalable3, float percentage4, Scalable scalable4) {
        return new ProportionalScalable(Arrays.asList(percentage1, percentage2, percentage3, percentage4),
                                        Arrays.asList(scalable1, scalable2, scalable3, scalable4));
    }

    static ProportionalScalable proportional(float percentage1, Scalable scalable1, float percentage2, Scalable scalable2, float percentage3, Scalable scalable3, float percentage4, Scalable scalable4, float percentage5, Scalable scalable5) {
        return new ProportionalScalable(Arrays.asList(percentage1, percentage2, percentage3, percentage4, percentage5),
                                        Arrays.asList(scalable1, scalable2, scalable3, scalable4, scalable5));
    }

    static StackScalable stack(Scalable... scalables) {
        return new StackScalable(scalables);
    }

    static StackScalable stack(String... ids) {
        List<Scalable> identifierScalables = Arrays.stream(ids).map(ScalableAdapter::new).collect(Collectors.toList());
        return new StackScalable(identifierScalables);
    }
}
