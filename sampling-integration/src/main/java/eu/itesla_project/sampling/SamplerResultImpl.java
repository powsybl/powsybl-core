/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.sampling;

import eu.itesla_project.modules.sampling.Sample;
import eu.itesla_project.modules.sampling.SamplerResult;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SamplerResultImpl implements SamplerResult {

    private final boolean ok;

    private final List<Sample> samples;

    public SamplerResultImpl(boolean ok, List<Sample> samples) {
        this.ok = ok;
        this.samples = samples;
    }

    @Override
    public boolean isOk() {
        return ok;
    }

    @Override
    public List<Sample> getSamples() {
        return samples;
    }

}
