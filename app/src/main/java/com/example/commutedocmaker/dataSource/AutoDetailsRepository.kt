package com.example.commutedocmaker.dataSource

import kotlinx.coroutines.flow.Flow

class AutoDetailsRepository(private val dao: AutoDetailsDao) {
    val allAutoDetails = dao.getAllAutoDetails()

    suspend fun insert(details: List<String>) {
        if (details.size <= Details.entries.size) {
            dao.insert(AutoDetailsData(id = 0, details = details))
        }
        else {
            throw IllegalArgumentException("Details list size must be equal to ${Details.entries.size}")
        }
    }

    suspend fun getById(autoDetailsId: Int): AutoDetailsData? {
        return dao.getAutoDetailsById(autoDetailsId)
    }

    suspend fun delete(autoDetails: AutoDetailsData) {
        dao.deleteById(autoDetails.id)
    }

    suspend fun delete(autoDetailsId: Int) {
        dao.deleteById(autoDetailsId)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }
}