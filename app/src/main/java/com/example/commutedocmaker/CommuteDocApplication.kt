package com.example.commutedocmaker

import android.app.Application
import com.example.commutedocmaker.dataSource.AutoDetailsRepository
import com.example.commutedocmaker.dataSource.DocAppDatabase
import com.example.commutedocmaker.dataSource.DraftRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class CommuteDocApplication: Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { DocAppDatabase.getInstance(this, applicationScope) }
    val draftRepository by lazy { DraftRepository(database.draftEntryDao()) }
    val autoDetailsRepository by lazy { AutoDetailsRepository(database.autoDetailsDao()) }
}