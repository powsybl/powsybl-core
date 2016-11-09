/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.tools;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.online.LocalOnlineApplicationMBean;
import eu.itesla_project.online.OnlineWorkflowStartParameters;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class RunTDSimulationsMpiTool implements Tool {

    private static Command COMMAND = new Command() {

        @Override
        public String getName() {
            return "run-td-simulation-mpi";
        }

        @Override
        public String getTheme() {
            return Themes.ONLINE_WORKFLOW;
        }

        @Override
        public String getDescription() {
            return "Run time-domain simulation";
        }

        @Override
        public Options getOptions() {
            Options options = new Options();
            options.addOption(Option.builder().longOpt("case-file")
                    .desc("the case path")
                    .hasArg()
                    .argName("FILE")
                    .required()
                    .build());
            options.addOption(Option.builder().longOpt("contingencies")
                    .desc("contingencies to test separated by , (all the db in not set)")
                    .hasArg()
                    .argName("LIST")
                    .build());
            options.addOption(Option.builder().longOpt("empty-contingency")
                    .desc("include the empty contingency among the contingencies")
                    .build());
            options.addOption(Option.builder().longOpt("output-folder")
                    .desc("the folder where to store the data")
                    .hasArg()
                    .argName("FOLDER")
                    .required()
                    .build());
            return options;
        }

        @Override
        public String getUsageFooter() {
            return null;
        }

    };

    @Override
    public Command getCommand() {
        return COMMAND;
    }


    @Override
    public void run(CommandLine line) throws Exception {

        OnlineWorkflowStartParameters startconfig = OnlineWorkflowStartParameters.loadDefault();

        String host = line.getOptionValue(OnlineWorkflowCommand.HOST);
        String port = line.getOptionValue(OnlineWorkflowCommand.PORT);
        String threads = line.getOptionValue(OnlineWorkflowCommand.THREADS);
        if (host != null)
            startconfig.setJmxHost(host);
        if (port != null)
            startconfig.setJmxPort(Integer.valueOf(port));
        if (threads != null)
            startconfig.setThreads(Integer.valueOf(threads));


        String urlString = "service:jmx:rmi:///jndi/rmi://" + startconfig.getJmxHost() + ":" + startconfig.getJmxPort() + "/jmxrmi";

        JMXServiceURL serviceURL = new JMXServiceURL(urlString);
        Map<String, String> jmxEnv = new HashMap<>();
        JMXConnector connector = JMXConnectorFactory.connect(serviceURL, jmxEnv);
        MBeanServerConnection mbsc = connector.getMBeanServerConnection();

        ObjectName name = new ObjectName(LocalOnlineApplicationMBean.BEAN_NAME);
        LocalOnlineApplicationMBean application = MBeanServerInvocationHandler.newProxyInstance(mbsc, name, LocalOnlineApplicationMBean.class, false);


        boolean emptyContingency = line.hasOption("empty-contingency");
        Path caseFile = Paths.get(line.getOptionValue("case-file"));
        application.runTDSimulations(startconfig, caseFile.toString(), line.getOptionValue("contingencies"), Boolean.toString(emptyContingency), line.getOptionValue("output-folder"));
    }

}
