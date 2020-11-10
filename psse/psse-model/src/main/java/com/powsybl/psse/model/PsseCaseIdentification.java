/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;

import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PsseCaseIdentification {

    @Parsed
    private int ic = 0;

    @Parsed
    private double sbase = 100;

    @Parsed
    private int rev = 33;

    @Parsed
    private double xfrrat = Double.NaN;

    @Parsed
    private double nxfrat = Double.NaN;

    @Parsed
    private double basfrq = Double.NaN;

    @Parsed(defaultNullRead = "")
    private String title1;

    @Parsed(defaultNullRead = "")
    private String title2;

    public int getIc() {
        return ic;
    }

    public void setIc(int ic) {
        this.ic = ic;
    }

    public double getSbase() {
        return sbase;
    }

    public void setSbase(double sbase) {
        this.sbase = sbase;
    }

    public int getRev() {
        return rev;
    }

    public void setRev(int rev) {
        this.rev = rev;
    }

    public double getXfrrat() {
        return xfrrat;
    }

    public void setXfrrat(double xfrrat) {
        this.xfrrat = xfrrat;
    }

    public double getNxfrat() {
        return nxfrat;
    }

    public void setNxfrat(double nxfrat) {
        this.nxfrat = nxfrat;
    }

    public double getBasfrq() {
        return basfrq;
    }

    public void setBasfrq(double basfrq) {
        this.basfrq = basfrq;
    }

    public String getTitle1() {
        return title1;
    }

    public void setTitle1(String title1) {
        this.title1 = title1;
    }

    public String getTitle2() {
        return title2;
    }

    public void setTitle2(String title2) {
        this.title2 = title2;
    }

    public void check() {
        if (ic == 1) {
            throw new PsseException("Incremental load of PSS/E data option (IC = 1) not supported");
        }
        int[] supportedVersionsInt = createSupportedVersions();
        if (!ArrayUtils.contains(supportedVersionsInt, rev)) {
            String supportedVersions = IntStream.of(supportedVersionsInt).mapToObj(String::valueOf).collect(Collectors.joining(", "));
            throw new PsseException("PSS/E version " + rev + " not supported. Supported Versions are: " + supportedVersions + ".");
        }
        if (sbase <= 0.) {
            throw new PsseException("PSS/E Unexpected System MVA base " + sbase);
        }
        if (basfrq <= 0.) {
            throw new PsseException("PSS/E Unexpected System base frequency " + basfrq);
        }
    }

    private static int[] createSupportedVersions() {
        int[] sv = new int[PsseVersion.values().length];

        int index = 0;
        for (PsseVersion e : PsseVersion.values()) {
            if (e == PsseVersion.VERSION_35) {
                sv[index] = 35;
            } else {
                sv[index] = 33;
            }
            index++;
        }
        return sv;
    }
}
