/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.tools;

import com.google.auto.service.AutoService;
import eu.itesla_project.cases.CaseRepository;
import eu.itesla_project.cases.CaseType;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.computation.local.LocalComputationManager;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.modules.online.OnlineConfig;
import eu.itesla_project.modules.online.OnlineWorkflowParameters;
import eu.itesla_project.modules.online.TimeHorizon;
import eu.itesla_project.online.LocalOnlineApplicationMBean;
import eu.itesla_project.online.OnlineWorkflowStartParameters;
import eu.itesla_project.simulation.securityindexes.SecurityIndexType;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class OnlineWorkflowTool implements Tool {

    @Override
    public Command getCommand() {
        return OnlineWorkflowCommand.INSTANCE;
    }

    private void showHelp(String message) {
        System.err.println(message);
        System.err.println();
        HelpFormatter formatter = new HelpFormatter();
        // it would be nice to have access to the private method  eu.itesla_project.commons.tools.Main.printCommandUsage
        formatter.printHelp(80, getCommand().getName(), "", getCommand().getOptions(), "\n" + Objects.toString(getCommand().getUsageFooter(), ""), true);
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

        Set<DateTime> baseCasesSet = null;

        OnlineWorkflowParameters params = OnlineWorkflowParameters.loadDefault();
        boolean atLeastOneBaseCaseLineParam = line.hasOption(OnlineWorkflowCommand.CASE_TYPE) || line.hasOption(OnlineWorkflowCommand.COUNTRIES)
                || line.hasOption(OnlineWorkflowCommand.BASE_CASE) || line.hasOption(OnlineWorkflowCommand.BASECASES_INTERVAL);
        boolean allNeededBaseCaseLineParams = line.hasOption(OnlineWorkflowCommand.CASE_TYPE) && line.hasOption(OnlineWorkflowCommand.COUNTRIES)
                && (line.hasOption(OnlineWorkflowCommand.BASE_CASE) || line.hasOption(OnlineWorkflowCommand.BASECASES_INTERVAL));

        if (line.hasOption(OnlineWorkflowCommand.CASE_FILE)) {
            if (atLeastOneBaseCaseLineParam) {
                showHelp("parameter " + OnlineWorkflowCommand.CASE_FILE + " cannot be used together with parameters: " + OnlineWorkflowCommand.CASE_TYPE + ", " + OnlineWorkflowCommand.COUNTRIES + ", " + OnlineWorkflowCommand.BASE_CASE + ", " + OnlineWorkflowCommand.BASECASES_INTERVAL);
                return;
            }
            params.setCaseFile(line.getOptionValue(OnlineWorkflowCommand.CASE_FILE));
        } else {
            if (params.getCaseFile() != null) {
                if (atLeastOneBaseCaseLineParam) {
                    if (!allNeededBaseCaseLineParams) {
                        showHelp("to override default parameter " + OnlineWorkflowCommand.CASE_FILE + ", all these parameters must be specified: " + OnlineWorkflowCommand.CASE_TYPE + ", " + OnlineWorkflowCommand.COUNTRIES + ", " + OnlineWorkflowCommand.BASE_CASE + " or " + OnlineWorkflowCommand.BASECASES_INTERVAL);
                        return;
                    }
                    params.setCaseFile(null);
                }
            }
            if (line.hasOption(OnlineWorkflowCommand.CASE_TYPE))
                params.setCaseType(CaseType.valueOf(line.getOptionValue(OnlineWorkflowCommand.CASE_TYPE)));
            if (line.hasOption(OnlineWorkflowCommand.COUNTRIES)) {
                params.setCountries(Arrays.stream(line.getOptionValue(OnlineWorkflowCommand.COUNTRIES).split(","))
                        .map(Country::valueOf)
                        .collect(Collectors.toSet()));
            }
            if (line.hasOption(OnlineWorkflowCommand.BASECASES_INTERVAL)) {
                Interval basecasesInterval = Interval.parse(line.getOptionValue(OnlineWorkflowCommand.BASECASES_INTERVAL));
                OnlineConfig oConfig = OnlineConfig.load();
                CaseRepository caseRepo = oConfig.getCaseRepositoryFactoryClass().newInstance().create(new LocalComputationManager());
                baseCasesSet = caseRepo.dataAvailable(params.getCaseType(), params.getCountries(), basecasesInterval);
                System.out.println("Base cases available for interval " + basecasesInterval.toString());
                baseCasesSet.forEach(x -> {
                    System.out.println(" " + x);
                });
            }
            if (baseCasesSet == null) {
                baseCasesSet = new HashSet<>();
                String base = line.getOptionValue(OnlineWorkflowCommand.BASE_CASE);
                if (base != null) {
                    baseCasesSet.add(DateTime.parse(base));
                } else {
                    baseCasesSet.add(params.getBaseCaseDate());
                }
            }
        }

        String histo = line.getOptionValue(OnlineWorkflowCommand.HISTODB_INTERVAL);
        if (histo != null)
            params.setHistoInterval(Interval.parse(histo));

        String states = line.getOptionValue(OnlineWorkflowCommand.STATES);
        if (states != null)
            params.setStates(Integer.parseInt(states));

        String timeHorizon = line.getOptionValue(OnlineWorkflowCommand.TIME_HORIZON);
        if (timeHorizon != null)
            params.setTimeHorizon(TimeHorizon.fromName(timeHorizon));

        String workflowid = line.getOptionValue(OnlineWorkflowCommand.WORKFLOW_ID);
        if (workflowid != null)
            params.setOfflineWorkflowId(workflowid);

        String feAnalysisId = line.getOptionValue(OnlineWorkflowCommand.FEANALYSIS_ID);
        if (feAnalysisId != null)
            params.setFeAnalysisId(feAnalysisId);

        String rulesPurity = line.getOptionValue(OnlineWorkflowCommand.RULES_PURITY);
        if (rulesPurity != null)
            params.setRulesPurityThreshold(Double.parseDouble(rulesPurity));

        if (line.hasOption(OnlineWorkflowCommand.STORE_STATES))
            params.setStoreStates(true);

        if (line.hasOption(OnlineWorkflowCommand.ANALYSE_BASECASE))
            params.setAnalyseBasecase(true);

        if (line.hasOption(OnlineWorkflowCommand.VALIDATION)) {
            params.setValidation(true);
            params.setStoreStates(true); // if validation then store states
            params.setAnalyseBasecase(true); // if validation then analyze base case
        }

        Set<SecurityIndexType> securityIndexes = null;
        if (line.hasOption(OnlineWorkflowCommand.SECURITY_INDEXES)) {
            if (!"ALL".equals(line.getOptionValue(OnlineWorkflowCommand.SECURITY_INDEXES)))
                securityIndexes = Arrays.stream(line.getOptionValue(OnlineWorkflowCommand.SECURITY_INDEXES).split(","))
                        .map(SecurityIndexType::valueOf)
                        .collect(Collectors.toSet());
            params.setSecurityIndexes(securityIndexes);
        }

        if (line.hasOption(OnlineWorkflowCommand.MERGE_OPTIMIZED))
            params.setMergeOptimized(true);

        String limitReduction = line.getOptionValue(OnlineWorkflowCommand.LIMIT_REDUCTION);
        if (limitReduction != null)
            params.setLimitReduction(Float.parseFloat(limitReduction));

        if (line.hasOption(OnlineWorkflowCommand.HANDLE_VIOLATION_IN_N)) {
            params.setHandleViolationsInN(true);
            params.setAnalyseBasecase(true); // if I need to handle violations in N, I need to analyze base case
        }

        String constraintMargin = line.getOptionValue(OnlineWorkflowCommand.CONSTRAINT_MARGIN);
        if (constraintMargin != null)
            params.setConstraintMargin(Float.parseFloat(constraintMargin));

        String urlString = "service:jmx:rmi:///jndi/rmi://" + startconfig.getJmxHost() + ":" + startconfig.getJmxPort() + "/jmxrmi";

        JMXServiceURL serviceURL = new JMXServiceURL(urlString);
        Map<String, String> jmxEnv = new HashMap<>();
        JMXConnector connector = JMXConnectorFactory.connect(serviceURL, jmxEnv);
        MBeanServerConnection mbsc = connector.getMBeanServerConnection();

        ObjectName name = new ObjectName(LocalOnlineApplicationMBean.BEAN_NAME);
        LocalOnlineApplicationMBean application = MBeanServerInvocationHandler.newProxyInstance(mbsc, name, LocalOnlineApplicationMBean.class, false);

        if (line.hasOption(OnlineWorkflowCommand.START_CMD)) {
            if (params.getCaseFile() != null) {
                System.out.println("starting Online Workflow, caseFile " + params.getCaseFile());
                application.startWorkflow(startconfig, params);

            } else {
                for (DateTime basecase : baseCasesSet) {
                    params.setBaseCaseDate(basecase);
                    System.out.println("starting Online Workflow, basecase " + basecase.toString());
                    application.startWorkflow(startconfig, params);
                }
            }
        } else if (line.hasOption(OnlineWorkflowCommand.SHUTDOWN_CMD)) {
            application.shutdown();
        } else {
            showHelp("");
        }

    }

}
