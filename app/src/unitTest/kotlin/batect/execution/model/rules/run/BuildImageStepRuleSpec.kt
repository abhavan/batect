/*
   Copyright 2017-2020 Charles Korn.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package batect.execution.model.rules.run

import batect.config.Container
import batect.execution.model.rules.TaskStepRuleEvaluationResult
import batect.execution.model.steps.BuildImageStep
import batect.testutils.equalTo
import batect.testutils.imageSourceDoesNotMatter
import batect.testutils.on
import com.natpryce.hamkrest.assertion.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object BuildImageStepRuleSpec : Spek({
    describe("a build image step rule") {
        val source = Container("the-container", imageSourceDoesNotMatter())
        val imageTag = "the-image-tag"
        val rule = BuildImageStepRule(source, imageTag)

        on("evaluating the rule") {
            val result = rule.evaluate(emptySet())

            it("returns a 'build image' step") {
                assertThat(result, equalTo(TaskStepRuleEvaluationResult.Ready(BuildImageStep(source, imageTag))))
            }
        }

        on("toString()") {
            it("returns a human-readable representation of itself") {
                assertThat(rule.toString(), equalTo("BuildImageStepRule(container: 'the-container', image tag: 'the-image-tag')"))
            }
        }
    }
})
