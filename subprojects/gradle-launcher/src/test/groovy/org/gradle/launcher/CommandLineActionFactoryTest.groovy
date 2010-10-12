/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.launcher

import org.gradle.initialization.CommandLineConverter
import org.gradle.util.GradleVersion
import org.gradle.util.RedirectStdOutAndErr
import org.gradle.util.SetSystemProperties
import org.junit.Rule
import spock.lang.Specification
import org.gradle.*

class CommandLineActionFactoryTest extends Specification {
    @Rule
    public final RedirectStdOutAndErr outputs = new RedirectStdOutAndErr();
    @Rule
    public final SetSystemProperties sysProperties = new SetSystemProperties();
    final BuildCompleter buildCompleter = Mock()
    final CommandLineConverter<StartParameter> startParameterConverter = Mock()
    final GradleLauncherFactory gradleLauncherFactory = Mock()
    final GradleLauncher gradleLauncher = Mock()
    final BuildResult buildResult = Mock()
    final CommandLineActionFactory factory = new CommandLineActionFactory(buildCompleter, startParameterConverter)
    final List<String> args = ['args']

    def setup() {
        GradleLauncher.injectCustomFactory(gradleLauncherFactory)
    }

    def cleanup() {
        GradleLauncher.injectCustomFactory(null)
    }

    def reportsCommandLineParseFailure() {
        def failure = new CommandLineArgumentException('<broken>')

        when:
        def action = factory.convert(args)

        then:
        1 * startParameterConverter.configure(!null) >> { args -> args[0].option('some-build-option') }
        1 * startParameterConverter.convert(!null, !null) >> { throw failure }

        when:
        action.run()

        then:
        outputs.stdErr.contains('<broken>')
        outputs.stdErr.contains('USAGE: gradle [option...] [task...]')
        outputs.stdErr.contains('--help')
        outputs.stdErr.contains('--some-build-option')
        1 * buildCompleter.exit(failure)
    }

    def displaysUsageMessage() {
        when:
        def action = factory.convert([option])
        action.run()

        then:
        _ * startParameterConverter.configure(!null) >> { args -> args[0].option('some-build-option') }
        outputs.stdOut.contains('USAGE: gradle [option...] [task...]')
        outputs.stdOut.contains('--help')
        outputs.stdOut.contains('--some-build-option')
        1 * buildCompleter.exit(null)

        where:
        option << ['-h', '-?', '--help']
    }

    def usesSystemPropertyForGradleAppName() {
        System.setProperty("org.gradle.appname", "gradle-app");

        when:
        def action = factory.convert(['-?'])
        action.run()

        then:
        outputs.stdOut.contains('USAGE: gradle-app [option...] [task...]')
        1 * buildCompleter.exit(null)
    }

    def displaysVersionMessage() {
        when:
        def action = factory.convert([option])
        action.run()

        then:
        outputs.stdOut.contains(new GradleVersion().prettyPrint())
        1 * buildCompleter.exit(null)

        where:
        option << ['-v', '--version']
    }

    def launchesGUI() {
        when:
        def action = factory.convert(['--gui'])

        then:
        action instanceof CommandLineActionFactory.ShowGuiAction
    }

    def executesBuild() {
        when:
        def action = factory.convert(args)

        then:
        1 * startParameterConverter.convert(!null, !null)

        when:
        action.run()

        then:
        1 * gradleLauncherFactory.newInstance(!null) >> gradleLauncher
        1 * gradleLauncher.run() >> buildResult
        1 * buildResult.failure >> null
        1 * buildCompleter.exit(null)
    }

    def executesFailedBuild() {
        def RuntimeException failure = new RuntimeException()

        when:
        def action = factory.convert(args)

        then:
        1 * startParameterConverter.convert(!null, !null)

        when:
        action.run()

        then:
        1 * gradleLauncherFactory.newInstance(!null) >> gradleLauncher
        1 * gradleLauncher.run() >> buildResult
        1 * buildResult.failure >> failure
        1 * buildCompleter.exit(failure)
    }
}
