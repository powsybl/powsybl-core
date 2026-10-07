/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.powsybl.loadflow.LoadFlowResult;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com}
 */
public interface SensitivityResultWriter {

    /**
     * In the case of multi-component computation, a factor has a variable
     * that is linked to an equipment that belongs to a single component.
     * Called once per factor and contingency in the factor context that
     * belongs to the same component as the factor equipment.
     * NaN for invalid factors whose equipment does not belong to a component
     * that has been run.
     *
     * @param factorIndex the factor index
     * @param contingencyIndex the contingency index, -1 for pre-contingency state
     * @param operatorStrategyIndex the operator strategy index, -1 if none
     * @param value the sensitivity value
     * @param functionReference the function reference
     */
    void writeSensitivityValue(int factorIndex, int contingencyIndex, int operatorStrategyIndex, double value, double functionReference);

    /**
     * Reports the status for a given state (contingency + optional operator strategy)
     * and a given component (connectedComponentNum/synchronousComponentNum).
     * <p>
     * Called for every state and every connectedComponentNum/synchronousComponentNum where the state has an impact.
     * For pre-contingency reporting, both {@code contingencyIndex} and
     * {@code operatorStrategyIndex} are -1.
     * For contingencies that are never run, called once in the end with NO_IMPACT
     * and connectedComponentNum and synchronousComponentNum set to -1.
     *
     * @param contingencyIndex        the contingency index, -1 for pre-contingency state
     * @param operatorStrategyIndex   the operator strategy index, -1 if none
     * @param connectedComponentNum   index of connected component, -1 if not applicable
     * @param synchronousComponentNum index of synchronous component, -1 if not applicable
     * @param status          the load flow status for this component
     * @param statusText      the load flow status text for this component
     */
    void writeStateStatus(int contingencyIndex, int operatorStrategyIndex,
                          int connectedComponentNum, int synchronousComponentNum,
                          LoadFlowResult.ComponentResult.Status status, String statusText);

    /**
     * Called at the end of the computation if the computation has not been interrupted.
     */
    void computationComplete();

    /**
     * @return true if the computation was complete, false otherwise
     */
    boolean isComputationComplete();
}
