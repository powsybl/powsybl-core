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

    private boolean withBranchSV = true;

    private boolean forceBusBranchTopo = false;

    private boolean indent = true;

    private boolean onlyMainCc = false;

    private boolean anonymized = false;

    private boolean skipExtensions = false;

    public XMLExportOptions() {
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

    public XMLExportOptions setWithBranchSV(boolean withBranchSV) {
        this.withBranchSV = withBranchSV;
        return this;
    }

    public boolean isForceBusBranchTopo() {
        return forceBusBranchTopo;
    }

    public XMLExportOptions setForceBusBranchTopo(boolean forceBusBranchTopo) {
        this.forceBusBranchTopo = forceBusBranchTopo;
        return this;
    }

    public boolean isIndent() {
        return indent;
    }

    public XMLExportOptions setIndent(boolean indent) {
        this.indent = indent;
        return this;
    }

    public boolean isOnlyMainCc() {
        return onlyMainCc;
    }

    public XMLExportOptions setOnlyMainCc(boolean onlyMainCc) {
        this.onlyMainCc = onlyMainCc;
        return this;
    }

    public boolean isAnonymized() {
        return anonymized;
    }

    public XMLExportOptions setAnonymized(boolean anonymized) {
        this.anonymized = anonymized;
        return this;
    }

    public boolean isSkipExtensions() {
        return skipExtensions;
    }

    public XMLExportOptions setSkipExtensions(boolean skipExtensions) {
        this.skipExtensions = skipExtensions;
        return this;
    }
}
