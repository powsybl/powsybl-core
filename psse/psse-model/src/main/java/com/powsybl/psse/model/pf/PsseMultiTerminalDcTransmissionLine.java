/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.powsybl.psse.model.pf.internal.PsseMultiTerminalDcBus;
import com.powsybl.psse.model.pf.internal.PsseMultiTerminalDcConverter;
import com.powsybl.psse.model.pf.internal.PsseMultiTerminalDcLink;
import com.powsybl.psse.model.pf.internal.PsseMultiTerminalDcMain;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

@JsonIgnoreProperties({"main"})
@JsonPropertyOrder({"name", "nconv", "ndcbs", "ndcln", "mdc", "vconv", "vcmod", "vconvn"})

public class PsseMultiTerminalDcTransmissionLine {

    public PsseMultiTerminalDcTransmissionLine(PsseMultiTerminalDcMain main) {
        this.main = main;
        dcConverters = new ArrayList<>();
        dcBuses = new ArrayList<>();
        dcLinks = new ArrayList<>();
    }

    public PsseMultiTerminalDcTransmissionLine(PsseMultiTerminalDcMain main,
        List<PsseMultiTerminalDcConverter> dcConverters, List<PsseMultiTerminalDcBus> dcBuses,
        List<PsseMultiTerminalDcLink> dcLinks) {
        this.main = main;
        this.dcConverters = dcConverters;
        this.dcBuses = dcBuses;
        this.dcLinks = dcLinks;
    }

    private final PsseMultiTerminalDcMain main;
    private final List<PsseMultiTerminalDcConverter> dcConverters;
    private final List<PsseMultiTerminalDcBus> dcBuses;
    private final List<PsseMultiTerminalDcLink> dcLinks;

    public String getName() {
        return main.getName();
    }

    public int getNconv() {
        return main.getNconv();
    }

    public int getNdcbs() {
        return main.getNdcbs();
    }

    public int getNdcln() {
        return main.getNdcln();
    }

    public int getMdc() {
        return main.getMdc();
    }

    public int getVconv() {
        return main.getVconv();
    }

    public double getVcmod() {
        return main.getVcmod();
    }

    public int getVconvn() {
        return main.getVconvn();
    }

    public PsseMultiTerminalDcMain getMain() {
        return main;
    }

    public List<PsseMultiTerminalDcConverter> getDcConverters() {
        return dcConverters;
    }

    public List<PsseMultiTerminalDcBus> getDcBuses() {
        return dcBuses;
    }

    public List<PsseMultiTerminalDcLink> getDcLinks() {
        return dcLinks;
    }
}
