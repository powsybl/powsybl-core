/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.results;

import com.powsybl.contingency.Contingency;
import com.powsybl.security.strategy.OperatorStrategy;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public interface SecurityAnalysisResultHandler {

    void writeBranchResult(Contingency contingency, OperatorStrategy operatorStrategy, BranchResult branchResult);

    void writeThreeWindingsTransformerResult(Contingency contingency, OperatorStrategy operatorStrategy, ThreeWindingsTransformerResult branchResult);

    void writeBusResult(Contingency contingency, OperatorStrategy operatorStrategy, BusResult busResult);

}
