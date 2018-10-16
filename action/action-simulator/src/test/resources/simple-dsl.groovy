import com.powsybl.iidm.network.RatioTapChangerStep

/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

rule('rule1') {
    when line('NHV1_NHV2_2').terminal1.i > 50 || line('NHV1_NHV2_2').overloaded
    apply 'action1'
}

action('action1') {
    description 'hello'
    tasks {
        script {
            load('LOAD').p0 += 1
            step = transformer('NHV2_NLOAD').ratioTapChanger.getCurrentStep()
            step.r = 3.3
            step.rdx = 1.1 + step.r
            step.setG(2.2 + step.getR())
            step.setRdb(1.1 + step.getG())
            step.rho = 1.0
            step.setRho(step.rho + step.getRho())
            step.ratio = step.getRatio() + step.ratio
            step.setRatio(step.ratio + 1.0)
            RatioTapChangerStep step0 = transformer('NHV2_NLOAD').ratioTapChanger.getStep(0)
            step0.r = 13.3
            step0.rdx = 11.1 + step0.r
            step0.setG(12.2 + step0.getR())
            step0.setRdb(11.1 + step0.getG())
            step0.rho = 11.0
            step0.setRho(step0.rho + step0.getRho())
            step0.ratio = step0.getRatio() + step0.ratio
            step0.setRatio(step0.ratio + 11.0)
        }
    }
}