/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.histo;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum HistoDbEquip {
    gen,
    loads,
    shunts,
    stations,
    _2wt("2wt"),
    _3wt("3wt"),
    lines,
    dangling;

    private final String label;

    private HistoDbEquip() {
        this(null);
    }

    private HistoDbEquip(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label != null ? label : super.toString();
    }

}
