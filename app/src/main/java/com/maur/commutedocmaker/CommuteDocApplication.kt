package com.maur.commutedocmaker

import android.app.Application
import com.maur.commutedocmaker.dataSource.autoDetails.AutoDetailsRepository
import com.maur.commutedocmaker.dataSource.DocAppDatabase
import com.maur.commutedocmaker.dataSource.document.DocumentRepository
import com.maur.commutedocmaker.dataSource.draftEntry.DraftRepository
import com.maur.commutedocmaker.dataSource.preference.PreferenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class CommuteDocApplication: Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { DocAppDatabase.getInstance(this, applicationScope) }
    val draftRepository by lazy { DraftRepository(database.draftEntryDao()) }
    val autoDetailsRepository by lazy { AutoDetailsRepository(database.autoDetailsDao()) }
    val preferenceRepository by lazy { PreferenceRepository(database.preferenceDao()) }
    val documentRepository by lazy { DocumentRepository(database.documentDao()) }
}