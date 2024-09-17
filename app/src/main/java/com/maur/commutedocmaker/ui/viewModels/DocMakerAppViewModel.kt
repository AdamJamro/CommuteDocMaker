package com.maur.commutedocmaker.ui.viewModels

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import androidx.lifecycle.*
import com.maur.commutedocmaker.dataSource.autoDetails.AutoDetails
import com.maur.commutedocmaker.dataSource.autoDetails.AutoDetailsRepository
import com.maur.commutedocmaker.dataSource.autoDetails.Details
import com.maur.commutedocmaker.dataSource.draftEntry.DraftEntry
import com.maur.commutedocmaker.dataSource.draftEntry.DraftRepository
import com.maur.commutedocmaker.xlsx.XLSXConverter
import com.maur.commutedocmaker.dataSource.document.Document
import com.maur.commutedocmaker.dataSource.document.DocumentRepository
import com.maur.commutedocmaker.dataSource.preference.Preference
import com.maur.commutedocmaker.dataSource.preference.PreferenceRepository
import com.maur.commutedocmaker.dataSource.preference.PreferenceType
import com.maur.commutedocmaker.dataSource.preference.PreferenceType.ACCESS
import com.maur.commutedocmaker.shouldRevokeAccess
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import kotlin.math.min

class DocMakerAppViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val draftRepository: DraftRepository,
    private val autoDetailsRepository: AutoDetailsRepository,
    private val preferenceRepository: PreferenceRepository,
    private val documentRepository: DocumentRepository,
    injectScope: CoroutineScope? = null
) : ViewModel() {
    companion object {
        private const val ENTRIES_KEY = "entries"
        private const val AUTO_DETAILS_KEY = "details"
        private const val DOCUMENTS_KEY = "documents"
    }

    private val scope = injectScope ?: viewModelScope
    private val currentlyEditedDraftPos: MutableStateFlow<Int> = MutableStateFlow(-1)
    val isLoading = mutableStateOf(true)

    private val preferences: MutableStateFlow<List<Preference>> = MutableStateFlow(emptyList())

    private val _entries = MutableStateFlow<List<DraftEntry>>(
        savedStateHandle[ENTRIES_KEY]
            ?: emptyList()
    )
    val entries: StateFlow<List<DraftEntry>> = _entries.asStateFlow()

    private val _autoDetails = MutableStateFlow(
        savedStateHandle[AUTO_DETAILS_KEY]
            ?: List(Details.entries.size) { "" }
    )
    val autoDetails: StateFlow<List<String>> = _autoDetails.asStateFlow()

    private val _documents = MutableStateFlow<List<Document>>(
        savedStateHandle[DOCUMENTS_KEY]
            ?: emptyList()
    )
    val documents: StateFlow<List<Document>> = _documents.asStateFlow()

    init {
        scope.launch {
            val fixedMinimumLoadTimeHandler = launch { delay(1300) } // for loading screen animation

            autoDetailsRepository.get()?.let { autoData ->
                _autoDetails.update { autoData.details }
                Log.d("DEBUG", "DocMakerAppViewModel.init fetched data from database ${_autoDetails.value}")
            } ?: run {
                Log.d("DEBUG", "DocMakerAppViewModel.init did not manage to fetch details from database ${_autoDetails.value}")
            }

            draftRepository.allDrafts.firstOrNull()?.sortedBy { it.draftId }?.let { drafts ->
                Log.d("DEBUG", "DocMakerAppViewModel.init data fetched $drafts")
                _entries.update { drafts }
                Log.d("DEBUG", "DocMakerAppViewModel.init fetched data from database ${_entries.value}")
            } ?: run {
                Log.d("DEBUG", "DocMakerAppViewModel.init did not manage to fetch drafts from database ${_entries.value}")
            }
            preferenceRepository.allPreferences.firstOrNull()?.also { fetchedPreferences ->
                preferences.update { fetchedPreferences }
            }
            documentRepository.allDocuments.firstOrNull()
                ?.sortedBy { it.date }
                ?.reversed()
                ?.also { documents ->
                _documents.update { documents }
            }

            for (saveableFlow in
                listOf(
                    Pair(_entries, ENTRIES_KEY),
                    Pair(_autoDetails, AUTO_DETAILS_KEY),
                    Pair(_documents, DOCUMENTS_KEY)
                )
            ) {
                launch {
                    saveableFlow.first.collectLatest { data ->
                        withContext(Dispatchers.Default) {
                            savedStateHandle[saveableFlow.second] = data
                        }
                    }
                }
            }

            fixedMinimumLoadTimeHandler.join()
            isLoading.value = false
            Log.d("DEBUG", "DocMakerAppViewModel.init finished")
        }
    }

    fun getPreference(requestPreference: PreferenceType): String? {
        preferences.value.forEach { preference ->
            if (preference.key == requestPreference.key) {
                return preference.value
            }
        }
        return null
    }

    fun updateDatabase() {
        runBlocking {
            updatePreferencesDatabase()
            updateDraftDatabase()
            updateAutoDetailsDatabase()
            updateDocumentsDatabase()
//            if (getPreference(ACCESS) == DENIED) {
//                 TODO: revoke access
//            }
            Log.d("DEBUG", "updated database auto details ${autoDetailsRepository.allAutoDetails.first().first().details}")
            Log.d("DEBUG", "updated database entries ${draftRepository.allDrafts.first()}")
        }
    }

    private suspend fun updateDraftDatabase() {
        val entries = _entries.value
        for (index in entries.indices) {
            entries[index].draftId = index
        }
        draftRepository.deleteAll()
        draftRepository.insert(entries)
    }

    private suspend fun updateAutoDetailsDatabase() {
        autoDetailsRepository.insert(_autoDetails.value)
    }

    private suspend fun updateDocumentsDatabase() {
        documentRepository.deleteAll()
        documentRepository.insert(_documents.value.sortedBy { it.date }.reversed())
    }

    private suspend fun updatePreferencesDatabase() {
        for (preference in preferences.value) {
            when (preference.key){
                ACCESS.key -> {
                    if (shouldRevokeAccess(autoDetails.value)) {
                        preference.value = ACCESS.DENIED
                    }
                }
            }
            preferenceRepository.updatePreference(preference)
        }
    }

    fun collectCurrentEditedDraftPos() : Int {
        val result = currentlyEditedDraftPos.value
        currentlyEditedDraftPos.update { -1 }
        return result
    }

    fun updateCurrentDraftPos(newPos: Int) {
        scope.launch {
            currentlyEditedDraftPos.update { newPos }
        }
    }

    fun deleteEntry(context: Context? = null, draft: DraftEntry) {
        scope.launch {
            val index = _entries.value.indexOf(draft)
            if (index == -1) return@launch
            if (index < 0 || index > _entries.value.lastIndex) {
                if (context != null)
                    Toast.makeText(context,"std", Toast.LENGTH_SHORT).show()
            } else {
                _entries.update { oldEntries: List<DraftEntry> ->
                    val lastIndex = oldEntries.lastIndex
                    val formerEntries = oldEntries.subList(0, index)
                    val latterEntries = oldEntries.subList(
                        /*from*/ min(index + 1, lastIndex + 1),
                        /*to*/   lastIndex + 1
                    )

                    /*return*/
                    formerEntries + latterEntries
                }
            }
        }
    }

    @Suppress("unused")
    fun addEntry(entryTitle: String) {
        scope.launch {
            var newList = _entries.value.toList()
            newList = listOf(DraftEntry(entryTitle, "", emptyList())) + newList
            if(isActive) {
                _entries.update { newList }
            }
        }
    }

    fun addEntry(draftEntry: DraftEntry) {
        scope.launch {
            var newList = _entries.value.toList()
            newList = listOf(draftEntry) + newList
            if (!isActive) return@launch

            _entries.update { newList }
        }
    }

    fun updateEntry(index: Int, updatedEntry: DraftEntry): Boolean {
        var success = false

        scope.launch {
            if (index < 0 || index > _entries.value.lastIndex) {
                Log.e("DEBUG", "draft index:$index out of bounds\n${_entries.value.lastIndex}")
            } else {
                _entries.update { oldEntries: List<DraftEntry> ->
                    val lastIndex = oldEntries.lastIndex
                    val formerEntries = oldEntries.subList(0, index)
                    val latterEntries = oldEntries.subList(
                        /*from*/ min(index + 1, lastIndex + 1),
                        /*to*/   lastIndex + 1
                    )

                    /*return*/
                    formerEntries + updatedEntry + latterEntries
                }
                success = true
            }
        }
        return success
    }

    @Suppress("unused")
    private fun addEntries(entries: List<DraftEntry>) {
        scope.launch {
            val newList = _entries.value.toMutableList()
            newList.addAll(entries)
            if(!isActive) return@launch
            _entries.value = newList
        }
    }

    fun updateAutoDetails(detail: Details, value: String) {
        val newValue =  _autoDetails.value
                .toMutableList()
                .also { it[detail.ordinal] = value }
                .toList()
        _autoDetails.update { newValue }

        Log.d("DEBUG", "updated auto details ${_autoDetails.value}")
    }

    fun updateAutoDetails(detailList: List<String>) {
        _autoDetails.update { detailList }
    }

    fun deleteDocument(document: Document) {
        scope.launch {
            _documents.update { oldDocuments ->
                oldDocuments.toMutableList().also { it.remove(document) }.toList()
            }
        }
    }

    fun generateDocument(draftEntry: DraftEntry, context: Context): Document? {
        val converter  = XLSXConverter(context)
        val document: Document? = converter.convertToXLSX(draftEntry, autoDetails = AutoDetails(details = _autoDetails.value))
        Log.d("DEBUG", "DocMakerAppViewModel.generateDocument: ${document?.path ?: "null"}")
        if (document != null) {
            _documents.update { listOf(document) + _documents.value }
        }
        return document
    }

    fun getFileUri(filePath: String, context: Context): Uri {
        val file = File(filePath)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}

class DocMakerAppViewModelFactory(
    private val savedStateHandle: SavedStateHandle,
    private val draftRepository: DraftRepository,
    private val autoDetailsRepository: AutoDetailsRepository,
    private val preferenceRepository: PreferenceRepository,
    private val documentRepository: DocumentRepository,
    private val scope: CoroutineScope? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DocMakerAppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DocMakerAppViewModel(
                savedStateHandle,
                draftRepository,
                autoDetailsRepository,
                preferenceRepository,
                documentRepository,
                scope
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
