/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.rulesdb.fs;

import eu.itesla_project.modules.rules.*;
import eu.itesla_project.simulation.securityindexes.SecurityIndexId;
import eu.itesla_project.simulation.securityindexes.SecurityIndexType;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class FileSystemRulesDbClient implements RulesDbClient {

    private final SecurityRuleSerializerLoader loader;

    protected FileSystemRulesDbClient(SecurityRuleSerializerLoader loader) {
        this.loader = Objects.requireNonNull(loader);
    }

    protected interface RulesFS extends AutoCloseable {

        Path getRoot();

        @Override void close() throws IOException;
    }

    protected abstract RulesFS createRulesFS() throws IOException;

    @Override
    public synchronized List<String> listWorkflows() {
        List<String> ls = new ArrayList<>();
        try (RulesFS fs = createRulesFS()) {
            Path rootDir = fs.getRoot();
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(rootDir, Files::isDirectory)) {
                for (Path p : ds) {
                    String workflowDirName = p.getFileName().toString();
                    // because in case of zip file, file name ends with /
                    if (workflowDirName.endsWith(File.separator)) {
                        workflowDirName = workflowDirName.substring(0, workflowDirName.length() - File.separator.length());
                    }
                    ls.add(workflowDirName);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ls;
    }

    @Override
    public synchronized void updateRule(SecurityRule rule) {
        Objects.requireNonNull(rule);
        try (RulesFS fs = createRulesFS()) {
            Path rootDir = fs.getRoot();
            Path workflowDir = rootDir.resolve(rule.getWorkflowId());
            Path rulesDir = workflowDir.resolve(rule.getId().getAttributeSet().name());
            Files.createDirectories(rulesDir);
            SecurityRuleSerializer serializer = loader.load(rule.getClass());
            Path ruleFile = rulesDir.resolve(rule.getId().getSecurityIndexId().toString() + "." + serializer.getFormat());
            serializer.format(rule, Files.newOutputStream(ruleFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized List<SecurityRule> getRules(String workflowId, RuleAttributeSet attributeSet, String contingencyId, SecurityIndexType securityIndexType) {
        Objects.requireNonNull(workflowId);
        Objects.requireNonNull(contingencyId);
        List<SecurityRule> rules = new ArrayList<>(1);
        try (RulesFS fs = createRulesFS()) {
            Path rootDir = fs.getRoot();
            Path workflowDir = rootDir.resolve(workflowId);
            for (RuleAttributeSet attributeSet2 : attributeSet != null ? new RuleAttributeSet[] {attributeSet} : RuleAttributeSet.values()) {
                Path attributeSetDir = workflowDir.resolve(attributeSet2.name());
                for (SecurityIndexType securityIndexType2 : securityIndexType != null ? new SecurityIndexType[] {securityIndexType} : SecurityIndexType.values()) {
                    SecurityIndexId securityIndexId = new SecurityIndexId(contingencyId, securityIndexType2);
                    for (SecurityRuleSerializer serializer : loader.loadAll()) {
                        Path ruleFile = attributeSetDir.resolve(securityIndexId.toString() + "." + serializer.getFormat());
                        if (Files.exists(ruleFile)) {
                            RuleId ruleId = new RuleId(attributeSet2, securityIndexId);
                            rules.add(serializer.parse(ruleId, workflowId, Files.newInputStream(ruleFile)));
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return rules;
    }

    private void listRules(String workflowId, RuleAttributeSet attributeSet, List<RuleId> ruleIds, Path rootDir) throws IOException {
        Path attributeSetDir = rootDir.resolve(workflowId).resolve(attributeSet.name());
        if (Files.exists(attributeSetDir)) {
            try (Stream<Path> stream = Files.list(attributeSetDir)) {
                stream.forEach(ruleFile -> {
                    for (SecurityRuleSerializer serializer : loader.loadAll()) {
                        if (ruleFile.getFileName().toString().endsWith(serializer.getFormat())) {
                            String fileName = ruleFile.getFileName().toString();
                            SecurityIndexId securityIndexId = SecurityIndexId.fromString(fileName.substring(0, fileName.length() - serializer.getFormat().length() - 1));
                            ruleIds.add(new RuleId(attributeSet, securityIndexId));
                        }
                    }
                });
            }
        }
    }

    @Override
    public synchronized Collection<RuleId> listRules(String workflowId, RuleAttributeSet attributeSet) {
        Objects.requireNonNull(workflowId);
        List<RuleId> ruleIds = new ArrayList<>(1);
        try (RulesFS fs = createRulesFS()) {
            Path rootDir = fs.getRoot();
            if (attributeSet != null) {
                listRules(workflowId, attributeSet, ruleIds, rootDir);
            } else {
                for (RuleAttributeSet attributeSet2 : RuleAttributeSet.values()) {
                    listRules(workflowId, attributeSet2, ruleIds, rootDir);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ruleIds;
    }

    @Override
    public void close() throws Exception {
    }

}
