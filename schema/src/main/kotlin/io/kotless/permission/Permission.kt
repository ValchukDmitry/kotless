package io.kotless.permission

import io.kotless.AwsResource
import io.kotless.PermissionLevel

/**
 * Permission to act upon other resource
 *
 * It is a definition of permission to make specified actions
 * resources of specified type with specified ids.
 *
 * The permission is granted to object owning it.
 *
 * @param resource type of resource permission is for
 * @param level actions permitted by permission
 * @param ids identifiers of resources under permission
 */
data class Permission(val resource: AwsResource, val level: PermissionLevel, val ids: Set<String>) {
    fun cloudIds(region: String, account: String) = ids.map { "${resource.glob(region, account)}:$it" }.toSet()

    val actions: Set<String> = when (level) {
        PermissionLevel.Read -> resource.read
        PermissionLevel.Write -> resource.write
        PermissionLevel.ReadWrite -> resource.read + resource.write
    }
}
