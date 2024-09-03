package com.example.commutedocmaker.dataSource.preference

class PreferenceRepository(private val dao: PreferenceDao) {
    val allPreferences = dao.getAllPreferences()

    suspend fun insert(preference: Preference) {
        dao.insertPreference(preference)
    }

    suspend fun getPreference(key: String): String? {
        return dao.getPreference(key)?.value
    }

    suspend fun deletePreference(key: String) {
        dao.deletePreference(key)
    }

    suspend fun updatePreference(preference: Preference) {
        dao.updatePreference(preference)
    }
}