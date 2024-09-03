package com.example.commutedocmaker

import android.app.Application
import com.example.commutedocmaker.dataSource.autoDetailsData.AutoDetailsRepository
import com.example.commutedocmaker.dataSource.DocAppDatabase
import com.example.commutedocmaker.dataSource.draftEntry.DraftRepository
import com.example.commutedocmaker.dataSource.preference.PreferenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class CommuteDocApplication: Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { DocAppDatabase.getInstance(this, applicationScope) }
    val draftRepository by lazy { DraftRepository(database.draftEntryDao()) }
    val autoDetailsRepository by lazy { AutoDetailsRepository(database.autoDetailsDao()) }
    val preferenceRepository by lazy { PreferenceRepository(database.preferenceDao()) }
}