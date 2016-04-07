/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag.network;

import com.google.common.base.Strings;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EsgName {

    private final String name;

    public EsgName(String name, int maxLength) {
        if (name.length() > maxLength) {
            throw new RuntimeException("Invalid id '" + name + "', expected to be less or equal to "
                    + maxLength + " characters");
        }
        this.name = Strings.padEnd(name, maxLength, ' ');
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof  EsgName) {
            return ((EsgName) obj).name.equals(name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

}
