/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.reporter.Reporter;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class RemoveFeederBayBuilder {

    private String connectableId = null;
    private Reporter reporter = Reporter.NO_OP;

    public RemoveFeederBay build() {
        return new RemoveFeederBay(connectableId, reporter);
    }

    /**
     * @param connectableId the non-null ID of the connectable
     */
    public RemoveFeederBayBuilder withConnectableId(String connectableId) {
        this.connectableId = connectableId;
        return this;
    }

    public RemoveFeederBayBuilder withReporter(Reporter reporter) {
        this.reporter = reporter;
        return this;
    }
}
