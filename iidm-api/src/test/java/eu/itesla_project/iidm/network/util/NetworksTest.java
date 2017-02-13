/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworksTest {

    @Test
    public void checkFlows() throws Exception {
        double r=0.04;
        double x=0.423;
        double g1=0.0;
        double g2=0.0;
        double b1=0.0;
        double b2=0.0;
        double rho1=1;
        double rho2=11.249999728;
        double alpha1=0.0;
        double alpha2=0.0;
        double u1=236.80258178710938;
        double u2=21.04814910888672;
        double theta1=0.1257718437996544;
        double theta2=0.12547118123496284;

        float p1=40.0744f;
        float q1=2.3124743f;
        float p2=-40.073254f;
        float q2=-2.3003194f;

        assertTrue(Networks.checkFlows("test", r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2));

        r= 0.04 / (rho2 * rho2);
        x= 0.423 / (rho2 * rho2);
        rho1 = 1 / rho2;
        rho2 = 1;

        assertTrue(Networks.checkFlows("test", r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2));
    }

}