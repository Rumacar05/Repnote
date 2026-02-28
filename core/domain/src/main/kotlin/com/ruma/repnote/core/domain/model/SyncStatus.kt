package com.ruma.repnote.core.domain.model

/**
 * Represents the synchronization status of a workout session with Firestore.
 */
enum class SyncStatus {
    /** Session has not been synced to Firestore yet */
    PENDING,

    /** Session is currently being synced to Firestore */
    SYNCING,

    /** Session has been successfully synced to Firestore */
    SYNCED,

    /** Sync failed, will retry later */
    ERROR,
}
