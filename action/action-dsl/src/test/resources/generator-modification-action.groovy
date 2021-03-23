import com.powsybl.iidm.network.RegulationMode

/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

action('unknown generator') {
    tasks {
        generatorModification('UNKNOWN') {
            targetP 50.0
        }
    }
}

action('targetV and targetQ with voltageRegulator OFF') {
    tasks {
        generatorModification('GEN') {
            minP 20.0
            maxP 60.0
            targetP 50.0
            targetV 10.0
            targetQ 25.0
            regulationMode RegulationMode.OFF
        }
    }
}

action('targetV and targetQ with voltageRegulator ON') {
    tasks {
        generatorModification('GEN') {
            targetV 10.0
            targetQ 25.0
            regulationMode RegulationMode.VOLTAGE
        }
    }
}

action('deltaTargetP within boundaries') {
    tasks {
        generatorModification('GEN') {
            deltaTargetP(-1.0)
        }
    }
}

action('deltaTargetP lower boundary overflow') {
    tasks {
        generatorModification('GEN') {
            deltaTargetP(-30000.0)
        }
    }
}

action('deltaTargetP upper boundary overflow') {
    tasks {
        generatorModification('GEN') {
            deltaTargetP 30000.0
        }
    }
}

action('targetP lower boundary overflow') {
    tasks {
        generatorModification('GEN') {
            targetP(-50000.0)
        }
    }
}

action('targetP upper boundary overflow') {
    tasks {
        generatorModification('GEN') {
            targetP 50000.0
        }
    }
}

action('connect') {
    tasks {
        generatorModification('GEN') {
            connected true
        }
    }
}

action('connect with targetP change') {
    tasks {
        generatorModification('GEN') {
            connected true
            targetP 100.0
        }
    }
}

action('disconnect') {
    tasks {
        generatorModification('GEN') {
            connected false
        }
    }
}

action('disconnect with targetP change') {
    tasks {
        generatorModification('GEN') {
            connected false
            targetP 50.0
        }
    }
}

action('connect with targetV change') {
    tasks {
        generatorModification('GEN') {
            connected true
            targetV 1234.56
        }
    }
}
