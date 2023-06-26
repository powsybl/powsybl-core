/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class SeriesMetadata {

    private final boolean index;
    private final String name;
    private final boolean modifiable;
    private final SeriesDataType type;
    private final boolean defaultAttribute;

    public SeriesMetadata(boolean index, String name, boolean modifiable, SeriesDataType type,
                          boolean defaultAttribute) {
        this.index = index;
        this.name = name;
        this.modifiable = modifiable;
        this.type = type;
        this.defaultAttribute = defaultAttribute;
    }

    public boolean isIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public boolean isModifiable() {
        return modifiable;
    }

    public SeriesDataType getType() {
        return type;
    }

    public static SeriesMetadata strings(String name) {
        return new SeriesMetadata(false, name, false, SeriesDataType.STRING, false);
    }

    public static SeriesMetadata ints(String name) {
        return new SeriesMetadata(false, name, false, SeriesDataType.INT, false);
    }

    public static SeriesMetadata booleans(String name) {
        return new SeriesMetadata(false, name, false, SeriesDataType.BOOLEAN, false);
    }

    public static SeriesMetadata doubles(String name) {
        return new SeriesMetadata(false, name, false, SeriesDataType.DOUBLE, false);
    }

    public static SeriesMetadata stringIndex(String name) {
        return new SeriesMetadata(true, name, false, SeriesDataType.STRING, false);
    }

    public static SeriesMetadata intIndex(String name) {
        return new SeriesMetadata(true, name, false, SeriesDataType.INT, false);
    }

    public boolean isDefaultAttribute() {
        return defaultAttribute;
    }
}
