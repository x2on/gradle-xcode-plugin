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

import de.felixschulze.gradle.helper.PlistHelper
import de.felixschulze.teamcity.TeamCityStatusMessageHelper
import de.felixschulze.teamcity.TeamCityStatusType
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class XcodeBuild {

    private static final Logger LOG = LoggerFactory.getLogger(XcodeBuild.class)

    private final String INTERFACE_BUILDER_ERROR = "Interface Builder encountered an error communicating with the iOS Simulator.";
    private final String COMPILE_XIB_ERROR = "CompileXIB";
    private final String IBTOOL_ERROR = "Exception while running ibtool: connection went invalid while waiting for a reply because a mach port died";

    public def build(Project project, String xcodeScheme, String xcodeSdk, File output) {
        def commands = [
                "xcodebuild",
        ]

        if (project.xcode.xcodeProject != null) {
            commands.add("-project");
            commands.add(project.xcode.xcodeProject);
        }

        if (project.xcode.xcodeConfiguration != null) {
            commands.add("-configuration");
            commands.add(project.xcode.xcodeConfiguration);
        }
        if (project.xcode.xcodeWorkspace != null) {
            commands.add("-workspace");
            commands.add(project.xcode.xcodeWorkspace);
        }
        if (xcodeScheme != null) {
            commands.add("-scheme");
            commands.add(xcodeScheme);
        }
        if (xcodeSdk != null) {
            commands.add("-sdk");
            commands.add(xcodeSdk);
        }

        commands.add("build");
        commands.add("ONLY_ACTIVE_ARCH=NO");

        commands.add("OBJROOT=" + output.absolutePath);
        commands.add("SYMROOT=" + output.absolutePath);
        commands.add("DSTROOT=" + output.absolutePath);


        if (project.xcode.provisioningProfile != null) {
            commands.add("PROVISIONING_PROFILE=\"" + project.xcode.provisioningProfile + "\"");
        }
        if (project.xcode.codeSignIdentity != null) {
            commands.add("CODE_SIGN_IDENTITY=" + project.xcode.codeSignIdentity);
        }

        if (project.xcode.infoPlist != null) {
            PlistHelper.changesValuesInPlist(project.projectDir, new File(project.xcode.infoPlist), project.xcode.bundleIdentifierSuffix, project.xcode.bundleDisplayNameSuffix, project.xcode.bundleVersionFromGit, project.xcode.teamCityLog)
        }

        Process process = CommandLineRunner.createCommand(".", commands, null)

        String outputString
        process.inputStream.eachLine {
            println it
            outputString += it
        }

        process.waitFor()

        if (process.exitValue() > 0) {

            if (outputString.contains(INTERFACE_BUILDER_ERROR) || outputString.contains(IBTOOL_ERROR) || outputString.contains(INTERFACE_BUILDER_ERROR) || outputString.contains(IBTOOL_ERROR)) {
                if (project.xcode.teamCityLog) {
                    println TeamCityStatusMessageHelper.buildStatusFailureString(TeamCityStatusType.FAILURE, 'Interface builder crashed')
                }
                LOG.error("Interface builder crashed.")
            } else {
                if (project.xcode.teamCityLog) {
                    String cleanedErrorString = outputString.substring(outputString.indexOf("** BUILD FAILED **"))
                    if (cleanedErrorString != null) {
                        //TODO: Use TeamCityStatusMessageHelper
                        println "##teamcity[message text='BUILD FAILED' errorDetails='" + TeamCityStatusMessageHelper.escapeString(cleanedErrorString) + "' status='ERROR']"
                        if (cleanedErrorString.contains(COMPILE_XIB_ERROR)) {
                            println TeamCityStatusMessageHelper.buildStatusFailureString(TeamCityStatusType.FAILURE, 'Interface builder crashed')
                            LOG.error("Interface builder crashed.")

                        } else {
                            println TeamCityStatusMessageHelper.buildStatusFailureString(TeamCityStatusType.FAILURE, 'Build failed')
                            LOG.error("Build failed.")
                        }
                    }
                }
            }

        }
    }
}
