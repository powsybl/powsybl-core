/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

action('both targetP and deltaTargetP') {
    modifications {
        generatorModification('GEN') {
            targetP 50.0
            deltaTargetP 1.0
        }
    }
}
