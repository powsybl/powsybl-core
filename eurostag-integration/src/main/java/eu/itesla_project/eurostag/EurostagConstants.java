/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag;

import eu.itesla_project.computation.Command;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface EurostagConstants {

    String EUSTAG_CPT = "eustag_cpt.e";
    String TSOINDEXES = "tsoindexes";
    String WP43 = "wp43_eurostag.sh";

    String PRE_FAULT_SEQ_FILE_NAME = "sim_pre_fault.seq";
    String PRE_FAULT_SAC_FILE_NAME = PRE_FAULT_SEQ_FILE_NAME.replace(".seq", ".sac");
    String PRE_FAULT_SAC_GZ_FILE_NAME = PRE_FAULT_SAC_FILE_NAME + ".gz";

    String FAULT_SEQ_FILE_NAME = "sim_fault_" + Command.EXECUTION_NUMBER_PATTERN + ".seq";

    String ALL_SCENARIOS_ZIP_FILE_NAME = "eurostag-all-scenarios.zip";

    String DDB_DICT_GENS_CSV = "dict_gens.csv"; //dictionary file used by wp43 transient index

    String LIMITS_ZIP_FILE_NAME = "eurostag-limits.zip";
}
