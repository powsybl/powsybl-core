/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.sampling;

import org.joda.time.Interval;

/**
 *
 * @author Quinary <itesla@quinary.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SamplerParameters {

    private final Interval histoInterval;

    private final boolean generationSampled;

    private final boolean boundariesSampled;

	public SamplerParameters(Interval histoInterval, boolean generationSampled, boolean boundariesSampled) {
		this.histoInterval = histoInterval;
        this.generationSampled = generationSampled;
        this.boundariesSampled = boundariesSampled;
	}

    public Interval getHistoInterval() {
        return histoInterval;
    }

    public boolean isGenerationSampled() {
        return generationSampled;
    }

    public boolean isBoundariesSampled() {
        return boundariesSampled;
    }

}
