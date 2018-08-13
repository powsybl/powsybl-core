/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools;

import com.google.auto.service.AutoService;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Version.class)
public class PowsyblCoreVersion extends AbstractVersion {

    public PowsyblCoreVersion() {
        super("powsybl-core", "${project.version}", "${buildNumber}", "${scmBranch}", Long.parseLong("${timestamp}"));
    }
}
