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

import de.felixschulze.teamcity.TeamCityStatusMessageHelper
import de.felixschulze.teamcity.TeamCityStatusType
import org.gradle.api.DefaultTask
import org.gradle.api.GradleScriptException
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.regex.Matcher

class ClangScanTask extends DefaultTask {

    @OutputDirectory
    File output

    private static final Logger LOG = LoggerFactory.getLogger(ClangScanTask.class)

    @TaskAction
    def clangScanBuild() throws IOException {

        def commands = [
                "scan-build"
        ]

        commands.add("-o")
        commands.add(new File(output, "clang-reports").absolutePath)

        XcodeBuild xcodeBuild = new XcodeBuild()
        commands.addAll(xcodeBuild.commands(project, project.xcode.xcodeScheme, project.xcode.xcodeSdk, output))

        Process process = CommandLineRunner.createCommand(".", commands, null)

        String regex = /scan-build: ([0-9]*) bugs found./

        String output
        int numberOfBugs = 0
        process.inputStream.eachLine {
            LOG.info(it)
            Matcher matcher = (it =~ regex)

            if (matcher.matches()) {
                println "MATCHES: " + matcher[0][1]
                numberOfBugs = matcher[0][1] as int
            }
            output += it
        }

        process.waitFor()

        if (process.exitValue() > 0) {
            if (project.xcode.teamCityLog) {
                println TeamCityStatusMessageHelper.buildStatusFailureString(TeamCityStatusType.FAILURE, "Build failed");
            }
            throw new GradleScriptException("Build failed", null)
        }

        if (numberOfBugs > 0) {
            if (project.xcode.teamCityLog) {
                println TeamCityStatusMessageHelper.buildStatusFailureString(TeamCityStatusType.FAILURE, "Scan-Build: ${numberOfBugs} bugs found");
            }
            throw new GradleScriptException("Scan-Build: ${numberOfBugs} bugs found", null)
        }

    }
}
