/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.limitsreduction.criterion.duration;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.security.limitsreduction.criterion.duration.AllTemporaryDurationCriterion;
import com.powsybl.security.limitsreduction.criterion.duration.EqualityTemporaryDurationCriterion;
import com.powsybl.security.limitsreduction.criterion.duration.IntervalTemporaryDurationCriterion;
import com.powsybl.security.limitsreduction.criterion.duration.PermanentDurationCriterion;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class LimitDurationCriterionModule extends SimpleModule {
    public LimitDurationCriterionModule() {
        addSerializer(PermanentDurationCriterion.class, new PermanentDurationCriterionSerializer());
        addSerializer(AllTemporaryDurationCriterion.class, new AllTemporaryDurationCriterionSerializer());
        addSerializer(EqualityTemporaryDurationCriterion.class, new EqualityTemporaryDurationCriterionSerializer());
        addSerializer(IntervalTemporaryDurationCriterion.class, new IntervalTemporaryDurationCriterionSerializer());

        addDeserializer(PermanentDurationCriterion.class, new PermanentDurationCriterionDeserializer());
        addDeserializer(AllTemporaryDurationCriterion.class, new AllTemporaryDurationCriterionDeserializer());
        addDeserializer(EqualityTemporaryDurationCriterion.class, new EqualityTemporaryDurationCriterionDeserializer());
        addDeserializer(IntervalTemporaryDurationCriterion.class, new IntervalTemporaryDurationCriterionDeserializer());
    }
}
