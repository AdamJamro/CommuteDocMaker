package com.example.commutedocmaker.dataSource.autoDetailsData

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

    private suspend fun update(autoDetails: AutoDetailsData) {
        dao.update(autoDetails)
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