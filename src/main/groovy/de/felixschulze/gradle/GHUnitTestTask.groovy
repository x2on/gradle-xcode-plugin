/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Felix Schulze
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package de.felixschulze.gradle

import de.felixschulze.gradle.helper.ProcessHelper
import de.felixschulze.teamcity.TeamCityImportDataType
import de.felixschulze.teamcity.TeamCityStatusMessageHelper
import de.felixschulze.teamcity.TeamCityStatusType
import org.gradle.api.DefaultTask
import org.gradle.api.GradleScriptException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.regex.Pattern

class GHUnitTestTask extends DefaultTask {

    private static final Logger LOG = LoggerFactory.getLogger(GHUnitTestTask.class)

    @TaskAction
    def ghunitTest() {

        if (!project.xcode.iosSimPath) {
            throw new InvalidUserDataException("iosSimPath must be defined.")
        }
        File iosSimCommandLine = new File(project.xcode.iosSimPath)
        if (!iosSimCommandLine.exists()) {
            throw new InvalidUserDataException("Invalid path for ios-sim: " + iosSimCommandLine.getAbsolutePath());
        }
        if (project.xcode.ghunitAppName == null) {
            throw new InvalidUserDataException("ghunitAppName must be defined.")
        }

        if (!project.xcode.xcodeConfiguration) {
            throw new InvalidUserDataException("xcodeConfiguration must be defined.")
        }

        if (!project.xcode.xcodeSdk.contains("iphonesimulator")) {
            throw new InvalidUserDataException("GHUnit-Tests can only run on simulator")
        }


        XcodeBuild xcodeBuild = new XcodeBuild()
        xcodeBuild.build(project, project.xcode.ghunitXcodeScheme, project.xcode.xcodeSdk, project.getBuildDir())

        File appDirectory = new File(project.getBuildDir(), project.xcode.xcodeConfiguration + "-iphonesimulator");
        File testResultsDirectory = new File(project.getBuildDir(), "test-results");

        File appFile = new File(appDirectory, project.xcode.ghunitAppName + ".app");

        def commands = [
                iosSimCommandLine.absolutePath,
                "launch",
        ]
        commands.add(appFile.getAbsolutePath());
        if (project.xcode.ghunitTestDevice != null) {
            commands.add("--family");
            commands.add(project.xcode.ghunitTestDevice);
        }

        if (project.xcode.ghunitAutoExit) {
            commands.add("--setenv");
            commands.add("GHUNIT_AUTOEXIT=YES");
        }

        if (project.xcode.teamCityLog) {
            commands.add("--setenv");
            commands.add("GHUNIT_AUTORUN=1");
            commands.add("--setenv");
            commands.add("WRITE_JUNIT_XML=1");
            commands.add("--setenv");
            commands.add("JUNIT_XML_DIR=" + testResultsDirectory.getAbsolutePath());
        }

        if (project.xcode.ghunitTestDeviceRetina) {
            commands.add("--retina");
        }

        if (project.xcode.ghunitTestDeviceTall) {
            commands.add("--tall");
        }

        ProcessHelper.killSimulatorProcess();

        Process process = CommandLineRunner.createCommand(".", commands, null)

        String output
        process.inputStream.eachLine {
            LOG.info(it)
            output += it
        }

        process.waitFor()

        String regexSimulatorTimeOut = ".*Simulator session timed out.(.*)";
        Boolean sessionTimedOut = Pattern.compile(regexSimulatorTimeOut, Pattern.DOTALL).matcher(output).matches();
        if (sessionTimedOut) {
            if (project.xcode.teamCityLog) {
                println TeamCityStatusMessageHelper.buildStatusFailureString(TeamCityStatusType.FAILURE, "Simulator session timed out.");
            }
            throw new GradleScriptException("Simulator session timed out.", null);
        }

        String regex = ".*Executed [0-9]* of [0-9]* tests, with [0-9]* failures in [0-9]*.[0-9]* seconds(.*)";
        Boolean success = Pattern.compile(regex, Pattern.DOTALL).matcher(output).matches();
        if (!success) {
            if (project.xcode.teamCityLog) {
                println TeamCityStatusMessageHelper.buildStatusFailureString(TeamCityStatusType.FAILURE, "Tests failed - The app may be crashed");
            }
            throw new GradleScriptException("Tests failed - The app may be crashed", null);
        }

        //Test results
        if (project.xcode.teamCityLog) {
            if (testResultsDirectory.exists()) {
                testResultsDirectory.eachFile {
                    if (it.name.endsWith('.xml')) {
                        println TeamCityStatusMessageHelper.importDataString(TeamCityImportDataType.JUNIT, it.canonicalPath)
                    }
                }
            } else {
                println TeamCityStatusMessageHelper.buildStatusFailureString(TeamCityStatusType.FAILURE, 'No test report found')
            }
        }

        ProcessHelper.killSimulatorProcess();
    }
}
