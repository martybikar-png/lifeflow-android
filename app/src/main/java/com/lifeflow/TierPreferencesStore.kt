package com.lifeflow

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lifeflow.domain.core.TierState
import com.lifeflow.domain.core.TierTruthProvenance
import com.lifeflow.domain.core.TierTruthSnapshot
import com.lifeflow.domain.core.TierTruthSource
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.io.File

internal class TierPreferencesStore(
    context: Context
) : TierTruthSource {

    private val appContext = context.applicationContext
    private val dataStore: DataStore<Preferences> = dataStoreFor(appContext)

    override fun currentSnapshotOrNull(): TierTruthSnapshot? = runBlocking {
        val prefs = runCatching { dataStore.data.firstOrNull() ?: emptyPreferences() }
            .getOrElse { emptyPreferences() }

        val tierRaw = prefs[CURRENT_TIER_KEY]
            ?.trim()
            ?.uppercase()
            ?: return@runBlocking null

        val tierState = runCatching { TierState.valueOf(tierRaw) }.getOrNull()
            ?: return@runBlocking null

        val provenance = prefs[PROVENANCE_KEY]
            ?.trim()
            ?.uppercase()
            ?.let { runCatching { TierTruthProvenance.valueOf(it) }.getOrNull() }
            ?: TierTruthProvenance.LOCAL_DATASTORE

        TierTruthSnapshot(
            effectiveTier = tierState,
            provenance = provenance,
            isGraceAccess = prefs[IS_GRACE_ACCESS_KEY] ?: false,
            isRevoked = prefs[IS_REVOKED_KEY] ?: false,
            isLocked = prefs[IS_LOCKED_KEY] ?: false,
            auditTag = prefs[AUDIT_TAG_KEY]
        )
    }

    override fun persistSnapshot(snapshot: TierTruthSnapshot) {
        runBlocking {
            dataStore.edit { prefs ->
                val auditTag = snapshot.auditTag

                prefs[CURRENT_TIER_KEY] = snapshot.effectiveTier.name
                prefs[PROVENANCE_KEY] = snapshot.provenance.name
                prefs[IS_GRACE_ACCESS_KEY] = snapshot.isGraceAccess
                prefs[IS_REVOKED_KEY] = snapshot.isRevoked
                prefs[IS_LOCKED_KEY] = snapshot.isLocked

                if (auditTag.isNullOrBlank()) {
                    prefs.remove(AUDIT_TAG_KEY)
                } else {
                    prefs[AUDIT_TAG_KEY] = auditTag
                }
            }
        }
    }

    override fun clear() {
        runBlocking {
            dataStore.edit { prefs ->
                prefs.remove(CURRENT_TIER_KEY)
                prefs.remove(PROVENANCE_KEY)
                prefs.remove(IS_GRACE_ACCESS_KEY)
                prefs.remove(IS_REVOKED_KEY)
                prefs.remove(IS_LOCKED_KEY)
                prefs.remove(AUDIT_TAG_KEY)
            }
        }
    }

    private class SharedPreferencesTierMigration(
        private val context: Context
    ) : DataMigration<Preferences> {

        private val legacyPrefs = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)

        override suspend fun shouldMigrate(currentData: Preferences): Boolean {
            return currentData[CURRENT_TIER_KEY] == null &&
                legacyPrefs.contains(LEGACY_KEY_CURRENT_TIER)
        }

        override suspend fun migrate(currentData: Preferences): Preferences {
            val legacyRaw = legacyPrefs.getString(LEGACY_KEY_CURRENT_TIER, null)
                ?.trim()
                ?.uppercase()
                ?: return currentData

            val normalizedTier = runCatching { TierState.valueOf(legacyRaw) }
                .getOrNull()
                ?.name
                ?: return currentData

            return mutablePreferencesOf(
                CURRENT_TIER_KEY to normalizedTier,
                PROVENANCE_KEY to TierTruthProvenance.MIGRATED_SHARED_PREFERENCES.name,
                IS_GRACE_ACCESS_KEY to false,
                IS_REVOKED_KEY to false,
                IS_LOCKED_KEY to false
            )
        }

        override suspend fun cleanUp() {
            legacyPrefs.edit()
                .remove(LEGACY_KEY_CURRENT_TIER)
                .apply()
        }
    }

    private companion object {
        @Volatile
        private var sharedDataStore: DataStore<Preferences>? = null

        private const val LEGACY_PREFS_NAME = "lifeflow_tier_truth"
        private const val LEGACY_KEY_CURRENT_TIER = "current_tier"

        private val CURRENT_TIER_KEY = stringPreferencesKey("current_tier")
        private val PROVENANCE_KEY = stringPreferencesKey("provenance")
        private val IS_GRACE_ACCESS_KEY = booleanPreferencesKey("is_grace_access")
        private val IS_REVOKED_KEY = booleanPreferencesKey("is_revoked")
        private val IS_LOCKED_KEY = booleanPreferencesKey("is_locked")
        private val AUDIT_TAG_KEY = stringPreferencesKey("audit_tag")

        private fun dataStoreFor(appContext: Context): DataStore<Preferences> {
            sharedDataStore?.let { return it }

            return synchronized(this) {
                sharedDataStore ?: PreferenceDataStoreFactory.create(
                    migrations = listOf(SharedPreferencesTierMigration(appContext)),
                    produceFile = {
                        tierTruthDataStoreFile(appContext)
                    }
                ).also { created ->
                    sharedDataStore = created
                }
            }
        }

        private fun tierTruthDataStoreFile(appContext: Context): File {
            return File(
                appContext.filesDir.parentFile,
                "datastore/lifeflow_tier_truth.preferences_pb"
            )
        }
    }
}
