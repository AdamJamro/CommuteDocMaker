package com.example.commutedocmaker.dataSource.draftEntry

class DraftRepository(private val draftEntryDao: DraftEntryDao) {
    val allDrafts = draftEntryDao.getAllDrafts()


    suspend fun insert(draft: DraftEntry) {
        draftEntryDao.insert(draft)
    }

    suspend fun insert(drafts: List<DraftEntry>) {
        draftEntryDao.insert(drafts)
    }

    suspend fun delete(draft: DraftEntry) {
        draftEntryDao.deleteById(draft.draftId)
    }

    suspend fun delete(draftId: Int) {
        draftEntryDao.deleteById(draftId)
    }

    suspend fun deleteAll() {
        draftEntryDao.deleteAll()
    }

}