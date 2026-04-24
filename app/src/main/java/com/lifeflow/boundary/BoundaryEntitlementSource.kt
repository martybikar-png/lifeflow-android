package com.lifeflow.boundary

enum class BoundaryEntitlementSource {
    INITIAL,
    PUBLIC_SHELL,
    LOCAL_DATASTORE,
    LOCAL_LOCKED,
    SERVER_GRACE,
    SERVER_REVOKED,
    FALLBACK_DEFAULT,
    OTHER;

    companion object {
        fun fromProvenanceName(name: String): BoundaryEntitlementSource {
            return when (name) {
                "LOCAL_DATASTORE" -> LOCAL_DATASTORE
                "LOCAL_LOCKED" -> LOCAL_LOCKED
                "SERVER_GRACE" -> SERVER_GRACE
                "SERVER_REVOKED" -> SERVER_REVOKED
                "FALLBACK_DEFAULT" -> FALLBACK_DEFAULT
                else -> OTHER
            }
        }
    }
}
