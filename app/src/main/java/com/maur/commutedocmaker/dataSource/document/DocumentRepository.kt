package com.maur.commutedocmaker.dataSource.document

class DocumentRepository(private val dao: DocumentDao) {
    val allDocuments = dao.getAllDocuments()

    suspend fun insert(list: List<Document>) {
        list.forEach { dao.insert(it) }
    }

    suspend fun insertDocument(document: Document) {
        dao.insert(document)
    }

    suspend fun updateDocument(document: Document) {
        dao.update(document)
    }

    suspend fun getDocument(title: String): Document? {
        return dao.get(title)
    }

    suspend fun getDocument(document: Document): Document? {
        return dao.get(document.title)
    }

    suspend fun deleteDocument(title: String) {
        dao.delete(title)
    }

    suspend fun deleteDocument(document: Document) {
        dao.delete(document.title)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }
}