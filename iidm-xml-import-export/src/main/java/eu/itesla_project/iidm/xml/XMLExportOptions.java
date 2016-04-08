/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class XMLExportOptions {

    private final boolean withBranchSV;

    private final boolean forceBusBranchTopo;

    private final boolean indent;

    private final boolean onlyMainCc;

    public XMLExportOptions() {
        this(true, false, true, false);
    }

    public XMLExportOptions(boolean withBranchSV, boolean forceBusBranchTopo, boolean indent, boolean onlyMainCc) {
        this.withBranchSV = withBranchSV;
        this.forceBusBranchTopo = forceBusBranchTopo;
        this.indent = indent;
        this.onlyMainCc = onlyMainCc;
    }

    public boolean isWithBranchSV() {
        return withBranchSV;
    }

    public boolean isForceBusBranchTopo() {
        return forceBusBranchTopo;
    }

    public boolean isIndent() {
        return indent;
    }

    public boolean isOnlyMainCc() {
        return onlyMainCc;
    }
}
