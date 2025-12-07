/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation.io;

import java.io.IOException;

import com.powsybl.iidm.network.TwoSides;

import com.powsybl.iidm.network.util.BranchData;
import com.powsybl.iidm.network.util.TwtData;
import com.powsybl.loadflow.validation.data.*;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.it>}
 */
public interface ValidationWriter extends AutoCloseable {

    void writeBranch(Validated<BranchData> validatedBranchData) throws IOException;

    void writeGenerator(Validated<GeneratorData> validatedGeneratorData) throws IOException;

    void writeBus(Validated<BusData> validatedBusData) throws IOException;

    void writeSvc(Validated<SvcData> validatedSvcData) throws IOException;

    void writeShunt(Validated<ShuntData> validatedShuntData) throws IOException;

    void writeT2wt(String twtId, double error, double upIncrement, double downIncrement, double rho, double rhoPreviousStep, double rhoNextStep,
                   int tapPosition, int lowTapPosition, int highTapPosition, double targetV, TwoSides regulatedSide, double v, boolean connected,
                   boolean mainComponent, boolean validated) throws IOException;

    void writeT3wt(String twtId, TwtData twtData, boolean validated) throws IOException;

    void setValidationCompleted();

    @Override
    void close() throws IOException;

}
