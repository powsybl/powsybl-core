/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.mock.LoadFlowFactoryMock;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractValidationTest {

    protected final ValidationConfig looseConfig = new ValidationConfig(0.1, true, LoadFlowFactoryMock.class, ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT,
                                                                        ValidationConfig.EPSILON_X_DEFAULT, ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT,
                                                                        ValidationOutputWriter.CSV_MULTILINE, new LoadFlowParameters(), ValidationConfig.OK_MISSING_VALUES_DEFAULT,
                                                                        ValidationConfig.NO_REQUIREMENT_IF_REACTIVE_BOUND_INVERSION_DEFAULT, ValidationConfig.COMPARE_RESULTS_DEFAULT,
                                                                        ValidationConfig.CHECK_MAIN_COMPONENT_ONLY_DEFAULT, ValidationConfig.NO_REQUIREMENT_IF_SETPOINT_OUTSIDE_POWERS_BOUNDS);
    protected final ValidationConfig strictConfig = new ValidationConfig(0.01, false, LoadFlowFactoryMock.class, ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT,
                                                                         ValidationConfig.EPSILON_X_DEFAULT, ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT,
                                                                         ValidationOutputWriter.CSV_MULTILINE, new LoadFlowParameters(), ValidationConfig.OK_MISSING_VALUES_DEFAULT,
                                                                         ValidationConfig.NO_REQUIREMENT_IF_REACTIVE_BOUND_INVERSION_DEFAULT, ValidationConfig.COMPARE_RESULTS_DEFAULT,
                                                                         ValidationConfig.CHECK_MAIN_COMPONENT_ONLY_DEFAULT, ValidationConfig.NO_REQUIREMENT_IF_SETPOINT_OUTSIDE_POWERS_BOUNDS);

}
