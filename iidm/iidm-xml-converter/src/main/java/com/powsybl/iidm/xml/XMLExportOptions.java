/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.TopologyLevel;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class XMLExportOptions {

    private boolean withBranchSV = true;

    @Deprecated
    private boolean forceBusBranchTopo = false;

    private boolean indent = true;

    private boolean onlyMainCc = false;

    private boolean anonymized = false;

    private boolean skipExtensions = false;

    private TopologyLevel topologyLevel = TopologyLevel.NODE_BREAKER;

    public XMLExportOptions() {
    }

    public XMLExportOptions(boolean withBranchSV, boolean forceBusBranchTopo, boolean indent, boolean onlyMainCc, TopologyLevel topologyLevel) {
        this.withBranchSV = withBranchSV;
        this.forceBusBranchTopo = forceBusBranchTopo;
        this.indent = indent;
        this.onlyMainCc = onlyMainCc;
        this.topologyLevel = topologyLevel;
    }

    public boolean isWithBranchSV() {
        return withBranchSV;
    }

    public XMLExportOptions setWithBranchSV(boolean withBranchSV) {
        this.withBranchSV = withBranchSV;
        return this;
    }

    /**
     * @deprecated  forceBusBranchTopo functionality has been merged to convert-network</br>
     *              functionality with topology-level as parameter</br>
     *              use {@link #getTopologyLevel()}
     */
    @Deprecated
    public boolean isForceBusBranchTopo() {
        return forceBusBranchTopo;
    }

    /**
     * @param forceBusBranchTopo
     *
     * @deprecated  forceBusBranchTopo functionality has been merged to convert-network</br>
     *              functionality with topology-level as parameter</br>
     *              use {@link #setTopologyLevel()}
     */
    @Deprecated
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

    public TopologyLevel getTopologyLevel() {
        return topologyLevel;
    }

    public void setTopologyLevel(TopologyLevel topologyLevel) {
        this.topologyLevel = topologyLevel;
    }
}
