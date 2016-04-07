/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.dymola.service;

import javax.xml.ws.Endpoint;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
class ServiceMain {
    private Endpoint endpoint;

    public static void main(String[] args) {
        ServiceMain dymolaService = new ServiceMain();

        String serviceWorkDir = "./server";
        int dymolaStartPort = 9000;
        int dymolaPortsRangeSize = 10;
        boolean dymolaDebug=true;

        String wsHost="localhost";
        int wsPort=8888;
        int wsNthreads = 5;

        String fakeSourceDir=null;

        if (args.length > 0) {
            serviceWorkDir=args[0];
            dymolaStartPort=Integer.parseInt(args[1]);
            dymolaPortsRangeSize=Integer.parseInt(args[2]);
            wsHost=args[3];
            wsPort=Integer.parseInt(args[4]);
            wsNthreads=Integer.parseInt(args[5]);
            dymolaDebug=Boolean.parseBoolean(args[6]);
            if (args.length>7){
                fakeSourceDir=args[7];
            }
        }

        String serviceURL = "http://"+wsHost+":" + wsPort + "/dymservice";

        System.setProperty("sun.net.httpserver.idleInterval","30000");

        System.out.println("Instantiating Dymola service proxy");
        SimulatorServerImpl simImpl=new SimulatorServerImpl(serviceWorkDir, dymolaStartPort, dymolaPortsRangeSize, dymolaDebug);
        if (fakeSourceDir!=null) {
            simImpl.setFakeSourceDir(fakeSourceDir);
        }

        dymolaService.endpoint = Endpoint.create(simImpl);
        ExecutorService executor = Executors.newFixedThreadPool(wsNthreads);
        dymolaService.endpoint.setExecutor(executor);
        System.out.println("Publishing dymola service, url is: " + serviceURL);
        System.out.println(" thread pool size: " + wsNthreads);
        dymolaService.endpoint.publish(serviceURL);
    }
}