/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
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

    protected final ValidationConfig looseConfig = new ValidationConfig(0.1f, true, LoadFlowFactoryMock.class, ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT,
                                                                        ValidationConfig.EPSILON_X_DEFAULT, ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT,
                                                                        ValidationOutputWriter.CSV_MULTILINE, new LoadFlowParameters(), ValidationConfig.OK_MISSING_VALUES_DEFAULT);
    protected final ValidationConfig strictConfig = new ValidationConfig(0.01f, false, LoadFlowFactoryMock.class, ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT,
                                                                         ValidationConfig.EPSILON_X_DEFAULT, ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT,
                                                                         ValidationOutputWriter.CSV_MULTILINE, new LoadFlowParameters(), ValidationConfig.OK_MISSING_VALUES_DEFAULT);

}
