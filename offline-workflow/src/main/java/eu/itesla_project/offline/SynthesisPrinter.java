/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline;

import java.util.Collection;

/**
 * Just print offline workflow synthesis on standard output for debug purposes.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SynthesisPrinter implements OfflineWorkflowSynthesisListener {

    @Override
    public void onSamplesChange(Collection<SampleSynthesis> samples) {
        System.out.println("onSamplesChange");
        for (SampleSynthesis sample : samples) {
            System.out.println("    " + sample.getId() + " " + sample.getLastTaskEvent().getTaskType() + " " + sample.getLastTaskEvent().getTaskStatus());
        }
    }

}
