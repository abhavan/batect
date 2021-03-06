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

package batect.docker

import batect.config.DeviceMount
import batect.config.HealthCheckConfig
import batect.config.PortMapping
import batect.testutils.given
import batect.testutils.on
import batect.utils.Json
import com.natpryce.hamkrest.assertion.assertThat
import org.araqnid.hamkrest.json.equivalentTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.Duration

object DockerContainerCreationRequestSpec : Spek({
    describe("a Docker container creation request") {
        given("a request with all values provided") {
            val request = DockerContainerCreationRequest(
                "the-container-name",
                DockerImage("the-image"),
                DockerNetwork("the-network"),
                listOf("do-the-thing"),
                listOf("sh"),
                "the-hostname",
                setOf("the-first-network-alias", "the-second-network-alias"),
                mapOf("SOME_VAR" to "some value"),
                "/work-dir",
                setOf(
                    DockerVolumeMount(DockerVolumeMountSource.LocalPath("/local"), "/container-1", "ro"),
                    DockerVolumeMount(DockerVolumeMountSource.Volume("my-volume"), "/container-2", "ro")
                ),
                setOf(DeviceMount("/dev/local", "/dev/container", "rw")),
                setOf(PortMapping(123, 456)),
                HealthCheckConfig(Duration.ofNanos(555), 12, Duration.ofNanos(333), "exit 0"),
                UserAndGroup(789, 222),
                privileged = true,
                init = true,
                capabilitiesToAdd = setOf(Capability.NET_ADMIN, Capability.KILL),
                capabilitiesToDrop = setOf(Capability.AUDIT_READ, Capability.CHOWN),
                useTTY = true,
                attachStdin = false
            )

            on("converting it to JSON for submission to the Docker API") {
                val json = request.toJson()

                it("returns the request in the format expected by the Docker API") {
                    assertThat(json, equivalentTo("""{
                        |   "AttachStdin": false,
                        |   "AttachStdout": true,
                        |   "AttachStderr": true,
                        |   "Tty": true,
                        |   "OpenStdin": false,
                        |   "StdinOnce": false,
                        |   "Image": "the-image",
                        |   "Cmd": ["do-the-thing"],
                        |   "Entrypoint": ["sh"],
                        |   "Hostname": "the-hostname",
                        |   "WorkingDir": "/work-dir",
                        |   "User": "789:222",
                        |   "Env": [
                        |       "SOME_VAR=some value"
                        |   ],
                        |   "ExposedPorts": {
                        |       "456/tcp": {}
                        |   },
                        |   "HostConfig": {
                        |       "NetworkMode": "the-network",
                        |       "Binds": [
                        |           "/local:/container-1:ro",
                        |           "my-volume:/container-2:ro"
                        |       ],
                        |       "Devices": [
                        |           {
                        |               "PathOnHost": "/dev/local",
                        |               "PathInContainer": "/dev/container",
                        |               "CgroupPermissions": "rw"
                        |           }
                        |       ],
                        |       "PortBindings": {
                        |           "456/tcp": [
                        |               {
                        |                   "HostIp": "",
                        |                   "HostPort": "123"
                        |               }
                        |           ]
                        |       },
                        |       "Privileged": true,
                        |       "Init": true,
                        |       "CapAdd": ["NET_ADMIN", "KILL"],
                        |       "CapDrop": ["AUDIT_READ", "CHOWN"]
                        |   },
                        |   "Healthcheck": {
                        |       "Test": ["CMD-SHELL", "exit 0"],
                        |       "Interval": 555,
                        |       "Retries": 12,
                        |       "StartPeriod": 333
                        |   },
                        |   "NetworkingConfig": {
                        |       "EndpointsConfig": {
                        |           "the-network": {
                        |               "Aliases": [
                        |                   "the-first-network-alias",
                        |                   "the-second-network-alias"
                        |               ]
                        |           }
                        |       }
                        |   }
                        |}""".trimMargin()))
                }
            }

            on("converting it to JSON for logging") {
                val json = Json.parser.stringify(DockerContainerCreationRequest.serializer(), request)

                it("returns a JSON representation of the Kotlin object") {
                    assertThat(json, equivalentTo("""
                        |{
                        |  "name": "the-container-name",
                        |  "image": { "id": "the-image" },
                        |  "network": { "id": "the-network" },
                        |  "command": ["do-the-thing"],
                        |  "entrypoint": ["sh"],
                        |  "hostname": "the-hostname",
                        |  "networkAliases": [
                        |    "the-first-network-alias",
                        |    "the-second-network-alias"
                        |  ],
                        |  "environmentVariables": {
                        |    "SOME_VAR": "some value"
                        |  },
                        |  "workingDirectory": "/work-dir",
                        |  "volumeMounts": [
                        |    {
                        |      "source": {
                        |        "type": "batect.docker.DockerVolumeMountSource.LocalPath",
                        |        "formatted": "/local",
                        |        "path": "/local"
                        |      },
                        |      "containerPath": "/container-1",
                        |      "options": "ro"
                        |    },
                        |    {
                        |      "source": {
                        |        "type": "batect.docker.DockerVolumeMountSource.Volume",
                        |        "formatted": "my-volume",
                        |        "name": "my-volume"
                        |      },
                        |      "containerPath": "/container-2",
                        |      "options": "ro"
                        |    }
                        |  ],
                        |  "deviceMounts": [
                        |    {
                        |      "local": "/dev/local",
                        |      "container": "/dev/container",
                        |      "options": "rw"
                        |    }
                        |  ],
                        |  "portMappings": [
                        |    { "local": 123, "container": 456 }
                        |  ],
                        |  "healthCheckConfig": {
                        |    "interval": "555ns",
                        |    "retries": 12,
                        |    "start_period": "333ns",
                        |    "command": "exit 0"
                        |  },
                        |  "userAndGroup": { "userId": 789, "groupId": 222 },
                        |  "privileged": true,
                        |  "init": true,
                        |  "capabilitiesToAdd": ["NET_ADMIN", "KILL"],
                        |  "capabilitiesToDrop": ["AUDIT_READ", "CHOWN"],
                        |  "useTTY": true,
                        |  "attachStdin": false
                        |}
                    """.trimMargin()))
                }
            }
        }

        given("a request with only the minimal set of values provided") {
            val request = DockerContainerCreationRequest(
                "the-container-name",
                DockerImage("the-image"),
                DockerNetwork("the-network"),
                emptyList(),
                emptyList(),
                "the-hostname",
                setOf("the-network-alias"),
                emptyMap(),
                null,
                emptySet(),
                emptySet(),
                emptySet(),
                HealthCheckConfig(),
                null,
                privileged = false,
                init = false,
                capabilitiesToAdd = emptySet(),
                capabilitiesToDrop = emptySet(),
                useTTY = false,
                attachStdin = false
            )

            on("converting it to JSON for submission to the Docker API") {
                val json = request.toJson()

                it("returns the request in the format expected by the Docker API") {
                    assertThat(json, equivalentTo("""{
                        |   "AttachStdin": false,
                        |   "AttachStdout": true,
                        |   "AttachStderr": true,
                        |   "Tty": false,
                        |   "OpenStdin": false,
                        |   "StdinOnce": false,
                        |   "Image": "the-image",
                        |   "Hostname": "the-hostname",
                        |   "Env": [],
                        |   "ExposedPorts": {},
                        |   "HostConfig": {
                        |       "NetworkMode": "the-network",
                        |       "Binds": [],
                        |       "Devices": [],
                        |       "PortBindings": {},
                        |       "Privileged": false,
                        |       "Init": false,
                        |       "CapAdd": [],
                        |       "CapDrop": []
                        |   },
                        |   "Healthcheck": {
                        |       "Test": [],
                        |       "Interval": 0,
                        |       "Retries": 0,
                        |       "StartPeriod": 0
                        |   },
                        |   "NetworkingConfig": {
                        |       "EndpointsConfig": {
                        |           "the-network": {
                        |               "Aliases": [
                        |                   "the-network-alias"
                        |               ]
                        |           }
                        |       }
                        |   }
                        |}""".trimMargin()))
                }
            }
        }
    }
})
