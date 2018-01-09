/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodePath {

    private final List<String> path;

    private final Function<List<String>, String> pathToString;

    public NodePath(List<String> path, Function<List<String>, String> pathToString) {
        this.path = Objects.requireNonNull(path);
        this.pathToString = Objects.requireNonNull(pathToString);
    }

    public List<String> toList() {
        return path;
    }

    @Override
    public String toString() {
        return pathToString.apply(path);
    }

    private static <F extends N, N extends AbstractNodeBase<F>> void addPath(N node, Predicate<N> pathStop, List<String> path) {
        if (!pathStop.test(node)) {
            addPath(node.getParent().orElse(null), pathStop, path);
        }
        path.add(node.getName());
    }

    public static <F extends N, N extends AbstractNodeBase<F>> NodePath find(N node, Predicate<N> pathStop, Function<List<String>, String> pathToString) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(pathStop);
        Objects.requireNonNull(pathToString);
        List<String> path = new ArrayList<>(1);
        addPath(node, pathStop, path);
        return new NodePath(path, pathToString);
    }
}
