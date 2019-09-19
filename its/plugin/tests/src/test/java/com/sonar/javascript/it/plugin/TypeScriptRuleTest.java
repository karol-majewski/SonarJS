/*
 * SonarQube JavaScript Plugin
 * Copyright (C) 2012-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.sonar.javascript.it.plugin;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.locator.FileLocation;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonarsource.analyzer.commons.ProfileGenerator;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeScriptRuleTest {

  @ClassRule
  public static final Orchestrator orchestrator = Tests.ORCHESTRATOR;

  private static final String PROJECT_KEY = "ts-rule-project";
  private static final File PROJECT_DIR = TestUtils.projectDir(PROJECT_KEY);

  @BeforeClass
  public static void startServer() throws Exception {

    File tsProfile = ProfileGenerator.generateProfile(
      orchestrator.getServer().getUrl(),
      "ts", "typescript",
      new ProfileGenerator.RulesConfiguration(),
      Collections.emptySet());

    File jsProfile = ProfileGenerator.generateProfile(
      orchestrator.getServer().getUrl(),
      "js", "javascript",
      new ProfileGenerator.RulesConfiguration(),
      Collections.singleton("CommentRegularExpression"));

    orchestrator.resetData();
    orchestrator.getServer()
      .restoreProfile(FileLocation.of(jsProfile))
      .restoreProfile(FileLocation.of(tsProfile));

    TestUtils.npmInstall(PROJECT_DIR);
  }

  @Test
  public void test() throws Exception {
    ExpectedIssues.parseForExpectedIssues(PROJECT_KEY, PROJECT_DIR.toPath());
    orchestrator.getServer().provisionProject(PROJECT_KEY, PROJECT_KEY);
    orchestrator.getServer().associateProjectToQualityProfile(PROJECT_KEY, "ts", "rules");
    orchestrator.getServer().associateProjectToQualityProfile(PROJECT_KEY, "js", "rules");

    String theProperty = "sonar.issue.ignore.multicriteria";

    SonarScanner build = SonarScanner.create()
      .setProjectDir(PROJECT_DIR)
      .setProjectKey(PROJECT_KEY)
      .setSourceDirs(".")
      .setProperty("dump.old", FileLocation.of("target/expected/ts/" + PROJECT_KEY).getFile().getAbsolutePath())
      .setProperty("dump.new", FileLocation.of("target/actual/ts/" + PROJECT_KEY).getFile().getAbsolutePath())
      .setProperty("lits.differences", FileLocation.of("target/differences").getFile().getAbsolutePath())
      .setProperty(theProperty, "x")
      .setProperty(theProperty + ".x.ruleKey", "typescript:S1438")
      .setProperty(theProperty + ".x.resourceKey", "**/*")
      .setProperty("sonar.cpd.exclusions", "**/*");


    List<String> rules = new ArrayList<>();

//    Files.walk(PROJECT_DIR.toPath())
//      .filter(file -> file.toString().endsWith(".ts") || file.toString().endsWith(".tsx"))
//      .forEach(file -> {
//        String ruleKey = file.toFile().getName().split("\\.")[0];
//        rules.add(ruleKey);
//        build.setProperty(theProperty + "." + ruleKey + ".ruleKey", "typescript:" + ruleKey);
//        build.setProperty(theProperty + "." + ruleKey + ".resourceKey", "**/" + ruleKey + ".*");
//      });
//
//    build.setProperty(theProperty, String.join(",", rules));

    orchestrator.executeBuild(build);

    assertThat(new String(Files.readAllBytes(Paths.get("target/differences")), StandardCharsets.UTF_8)).isEmpty();
  }
}
