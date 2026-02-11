/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com}
 */
public interface SensitivityResultWriter {

    /**
     * In the case of multi-component computation
     * A factor has a variable that is linked to en equipement that is in a single component
     * Called once per factor and contingency in the factor context that belongs to the same component as the factor equipment
     * Nan for invalid factors whose equipement does not belongs to a component that has been run
     *
     * @param factorIndex the factor index
     * @param contingencyIndex the contingency index
     * @param value teh sensitivity valuue
     * @param functionReference the function reference
     */
    void writeSensitivityValue(int factorIndex, int contingencyIndex, double value, double functionReference);

    /**
     * Called for every contingency and every numCC/numCS where the contingency has an impact
     * For contingencies that are never run, called once in the end with no IMPACT and numCC and numCs set to -1
     * @param contingencyIndex the contingency index
     * @param status the sensitivity analysis status
     * @param loadFlowStatus the loadflow status for this component
     * @param numCC index of connected component
     * @param numCs index of synchronous component
     */
    void writeContingencyStatus(int contingencyIndex, SensitivityAnalysisResult.Status status, SensitivityAnalysisResult.LoadFlowStatus loadFlowStatus, int numCC, int numCs);

    // Why this new method ???
    // void writeSynchronousComponentStatus(int numCC, int numCS, SensitivityAnalysisResult.LoadFlowStatus  loadFlowStatus);

    /**
     * Called at the end of the computation if the computation has not been interrupted
     */
    void computationComplete();

}
