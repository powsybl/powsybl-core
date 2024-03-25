/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
final class Substations {

    private Substations() {
    }

    static Iterable<Substation> filter(Iterable<Substation> substations,
                                       final String country,
                                       final String tso,
                                       final String... geographicalTags) {
        return StreamSupport.stream(substations.spliterator(), false).filter(substation -> {
            if (country != null && !country.equals(substation.getCountry().map(Country::getName).orElse(""))) {
                return false;
            }
            if (tso != null && !tso.equals(substation.getTso())) {
                return false;
            }
            for (String tag : geographicalTags) {
                if (!substation.getGeographicalTags().contains(tag)) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    }

    /**
     * Throw a {@link com.powsybl.commons.PowsyblException} if this substation contains at least one {@link Branch} or
     * one {@link ThreeWindingsTransformer} or one {@link HvdcConverterStation} linked to a voltage level outside this
     * substation.
     */
    static void checkRemovability(Substation substation) {
        for (VoltageLevel vl : substation.getVoltageLevels()) {
            for (Connectable connectable : vl.getConnectables()) {
                if (connectable instanceof Branch<?> branch) {
                    checkRemovability(substation, branch);
                } else if (connectable instanceof ThreeWindingsTransformer twt) {
                    checkRemovability(substation, twt);
                } else if (connectable instanceof HvdcConverterStation<?> hvdcConverterStation) {
                    checkRemovability(substation, hvdcConverterStation);
                }
            }
        }
    }

    private static void checkRemovability(Substation substation, Branch branch) {
        Substation s1 = branch.getTerminal1().getVoltageLevel().getSubstation().orElse(null);
        Substation s2 = branch.getTerminal2().getVoltageLevel().getSubstation().orElse(null);
        if (s1 != substation || s2 != substation) {
            throw createIsolationException(substation);
        }
    }

    private static void checkRemovability(Substation substation, ThreeWindingsTransformer twt) {
        Substation s1 = twt.getLeg1().getTerminal().getVoltageLevel().getSubstation().orElse(null);
        Substation s2 = twt.getLeg2().getTerminal().getVoltageLevel().getSubstation().orElse(null);
        Substation s3 = twt.getLeg3().getTerminal().getVoltageLevel().getSubstation().orElse(null);
        if (s1 != substation || s2 != substation || s3 != substation) {
            throw createIsolationException(substation);
        }
    }

    private static void checkRemovability(Substation substation, HvdcConverterStation station) {
        HvdcLine hvdcLine = substation.getNetwork().getHvdcLine(station);
        if (hvdcLine != null) {
            Substation s1 = hvdcLine.getConverterStation1().getTerminal().getVoltageLevel().getSubstation().orElse(null);
            Substation s2 = hvdcLine.getConverterStation2().getTerminal().getVoltageLevel().getSubstation().orElse(null);
            if (s1 != substation || s2 != substation) {
                throw createIsolationException(substation);
            }
        }
    }

    private static PowsyblException createIsolationException(Substation substation) {
        Objects.requireNonNull(substation);
        return new PowsyblException("The substation " + substation.getId() + " is still connected to another substation");
    }
}
