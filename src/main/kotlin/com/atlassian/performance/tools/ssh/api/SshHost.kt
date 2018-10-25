package com.atlassian.performance.tools.ssh.api

import com.atlassian.performance.tools.ssh.api.auth.PublicKeyAuthentication
import com.atlassian.performance.tools.ssh.api.auth.SshAuthentication
import java.nio.file.Path
import javax.json.Json
import javax.json.JsonObject

/**
 * Holds SSH coordinates.
 *
 * @param ipAddress IP of the remote system.
 * @param userName User allowed to connect to the remote server.
 * @param authentication Private SSH authentication method for the user.
 * @param port Port of the remote system.
 */
data class SshHost(
    val ipAddress: String,
    val userName: String,
    val authentication: SshAuthentication,
    val port: Int
) {
    constructor(json: JsonObject) : this(
        ipAddress = json.getString("ipAddress"),
        userName = json.getString("userName"),
        authentication = SshAuthentication.fromJson(json.getJsonObject("authentication")),
        port = json.getInt("port")
    )

    @Deprecated(
        message = "Use the primary constructor"
    )
    constructor(
        ipAddress: String,
        userName: String,
        key: Path,
        port: Int
    ) : this(
        ipAddress = ipAddress,
        userName = userName,
        authentication = PublicKeyAuthentication(key),
        port = port
    )

    constructor(
        ipAddress: String,
        userName: String,
        key: Path
    ) : this(
        ipAddress = ipAddress,
        userName = userName,
        authentication = PublicKeyAuthentication(key),
        port = 22
    )

    val key: Path
        get() {
            if (authentication is PublicKeyAuthentication) {
                return authentication.key
            } else {
                throw Exception("The authentication used by this host does not use public key auth.")
            }
        }

    fun toJson(): JsonObject {
        return Json.createObjectBuilder()
            .add("ipAddress", ipAddress)
            .add("userName", userName)
            .add("port", port)
            .add("authentication", authentication.toJson())
            .build()
    }
}