/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag.network;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EsgBranchName {
    private final Esg8charName node1Name; // sending node name
    private final Esg8charName node2Name; // receiving node name
    private final char xpp; // parallel index

    public EsgBranchName(Esg8charName node1Name, Esg8charName node2Name, char xpp) {
        this.node1Name = Objects.requireNonNull(node1Name);
        this.node2Name = Objects.requireNonNull(node2Name);
        this.xpp = xpp;
    }

    public Esg8charName getNode1Name() {
        return node1Name;
    }

    public Esg8charName getNode2Name() {
        return node2Name;
    }

    public char getXpp() {
        return xpp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EsgBranchName) {
            EsgBranchName other = (EsgBranchName) obj;
            return node1Name.equals(other.node1Name) && node2Name.equals(other.node2Name) && xpp == other.xpp;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(node1Name, node2Name, xpp);
    }

    @Override
    public String toString() {
        return node1Name + "-" + node2Name + "-" + xpp;
    }

}
