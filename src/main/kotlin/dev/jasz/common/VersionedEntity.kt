package dev.jasz.common

data class VersionedEntity<ENTITY>(
    val version: Int,
    val entity: ENTITY,
)
