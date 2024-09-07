package com.example.commutedocmaker.dataSource.autoDetails

class AutoDetailsRepository(private val dao: AutoDetailsDao) {
    val allAutoDetails = dao.getAllAutoDetails()

    suspend fun insert(details: List<String>) {
        if (details.size <= Details.entries.size) {
            dao.insert(AutoDetails(id = 0, details = details))
        }
        else {
            throw IllegalArgumentException("Details list size must be equal to ${Details.entries.size}")
        }
    }

    private suspend fun update(autoDetails: AutoDetails) {
        dao.update(autoDetails)
    }


    suspend fun get(): AutoDetails? {
        return dao.getAutoDetailsById(0)
    }

    suspend fun delete(autoDetails: AutoDetails) {
        dao.deleteById(autoDetails.id)
    }

    suspend fun delete(autoDetailsId: Int) {
        dao.deleteById(autoDetailsId)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }
}