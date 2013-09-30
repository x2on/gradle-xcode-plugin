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

package de.felixschulze.gradle.helper;

import org.gradle.api.GradleScriptException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Helper for shutdown iPhone Simulator
 *
 * @author <a href="mail@felixschulze.de">Felix Schulze</a>
 */
public class ProcessHelper {

    private static final String EMULATOR_COMMAND = "ps axo pid,command | grep 'iPhone Simulator'";
    private static final String KILL = "killall 'iPhone Simulator'";

    public static boolean isProcessRunning() throws IOException {

        String[] commands = new String[]{"/bin/sh", "-c", EMULATOR_COMMAND};
        Process p = Runtime.getRuntime().exec(commands);
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("iPhone Simulator.app")) {
                return true;
            }
        }
        return false;
    }

    public static void killSimulatorProcess() throws GradleScriptException {
        try {
            String[] commands = new String[]{"/bin/sh", "-c", KILL};
            Runtime.getRuntime().exec(commands);
        } catch (IOException e) {
            throw new GradleScriptException("Error while shutdown simulator: ", e);
        }
    }
}
