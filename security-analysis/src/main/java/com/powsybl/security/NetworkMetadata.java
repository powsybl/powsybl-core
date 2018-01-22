/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.iidm.network.Network;
import org.joda.time.DateTime;

import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class NetworkMetadata extends AbstractExtendable<NetworkMetadata> {

    private final String id;

    private final String sourceFormat;

    private final DateTime caseDate;

    private final int forecastDistance;

    public NetworkMetadata(Network network) {
        Objects.requireNonNull(network);
        this.id = network.getId();
        this.sourceFormat = network.getSourceFormat();
        this.caseDate = network.getCaseDate();
        this.forecastDistance = network.getForecastDistance();
    }

    public NetworkMetadata(String id, String sourceFormat, DateTime caseDate, int forecastDistance) {
        this.id = Objects.requireNonNull(id);
        this.sourceFormat = Objects.requireNonNull(sourceFormat);
        this.caseDate = Objects.requireNonNull(caseDate);
        this.forecastDistance = forecastDistance;
    }

    public String getId() {
        return id;
    }

    public String getSourceFormat() {
        return sourceFormat;
    }

    public DateTime getCaseDate() {
        return caseDate;
    }

    public int getForecastDistance() {
        return forecastDistance;
    }
}
