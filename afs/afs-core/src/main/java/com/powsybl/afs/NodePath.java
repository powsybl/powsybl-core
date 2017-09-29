/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodePath {

    private final List<String> path;

    private final Function<List<String>, String> toStringFct;

    public NodePath(List<String> path, Function<List<String>, String> toStringFct) {
        this.path = Objects.requireNonNull(path);
        this.toStringFct = Objects.requireNonNull(toStringFct);
    }

    public List<String> toList() {
        return path;
    }

    @Override
    public String toString() {
        return toStringFct.apply(path);
    }

    private static <FOLDER extends NODE, NODE extends AbstractNodeBase<FOLDER>> void addPath(NODE node, List<String> path) {
        if (node.getFolder() != null) {
            addPath(node.getFolder(), path);
        }
        path.add(node.getName());
    }

    public static <FOLDER extends NODE, NODE extends AbstractNodeBase<FOLDER>> NodePath find(NODE node, Function<List<String>, String> toStringFct) {
        Objects.requireNonNull(node);
        List<String> path = new ArrayList<>(1);
        addPath(node, path);
        return new NodePath(path, toStringFct);
    }
}
