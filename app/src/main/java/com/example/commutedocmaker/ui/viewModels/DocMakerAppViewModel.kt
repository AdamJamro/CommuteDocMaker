package com.example.commutedocmaker.ui.viewModels

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import com.example.commutedocmaker.dataSource.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.min

class DocMakerAppViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val draftRepository: DraftRepository,
    private val autoDetailsRepository: AutoDetailsRepository,
    injectScope: CoroutineScope? = null
) : ViewModel() {
    companion object {
        private const val ENTRIES_KEY = "entries"
        private const val AUTO_DETAILS_KEY = "details"
//        private val emptyAutoData = AutoDetailsData(
//            id = 0,
//            details = List(Details.entries.size) { "" }
//        )
    }

    private val scope = injectScope ?: viewModelScope
    private val currentlyEditedDraftPos: MutableStateFlow<Int> = MutableStateFlow(-1)
    val isLoading = mutableStateOf(true)

    private val _entries = MutableStateFlow<List<DraftEntry>>(
        savedStateHandle[ENTRIES_KEY]
            ?: emptyList()
    )
    val entries: StateFlow<List<DraftEntry>> = _entries.asStateFlow()

    private val _autoDetails = MutableStateFlow(
        savedStateHandle[AUTO_DETAILS_KEY]
            ?: List(Details.entries.size) { "" })
    val autoDetails: StateFlow<List<String>> = _autoDetails.asStateFlow()

    init {
        scope.launch {
            val fixedMinimumLoadTimeHandler = launch {
                delay(1000)
            }
            try {
                _entries.update {
                    draftRepository.allDrafts.first().sortedBy { it.draftId }
                }
                autoDetailsRepository.getById(0)?.details?.also {
                    _autoDetails.update { it }
                }
                Log.d("DEBUG", "DocMakerAppViewModel.init fetched data from database ${_autoDetails.value}")
            } catch (e: Exception) {
                Log.i("DEBUG info", "DocMakerAppViewModel.init failed to fetch data from database\n$e")
            }

            launch {
                _entries.collectLatest { entries ->
                    withContext(Dispatchers.Main) {
                        savedStateHandle[ENTRIES_KEY] = entries
                    }
                }
            }
            launch {
                _autoDetails.collectLatest { details ->
                    withContext(Dispatchers.Main) {
                        savedStateHandle[AUTO_DETAILS_KEY] = details
                    }
                }
            }
            if (_entries.value.isEmpty()) {
                fetchExampleEntries()
            }
            fixedMinimumLoadTimeHandler.join()
            isLoading.value = false
            Log.d("DEBUG", "DocMakerAppViewModel.init finished")
        }
    }

    fun updateDatabase() {
        runBlocking {
            draftRepository.deleteAll()
            autoDetailsRepository.deleteAll()
            val entries = _entries.value
            for (index in entries.indices) {
                entries[index].draftId = index
            }
            draftRepository.insert(entries)
            autoDetailsRepository.insert(_autoDetails.value)
            Log.d("DEBUG", "updated database auto details ${autoDetailsRepository.allAutoDetails.first().first().details}")

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
            if(!isActive) {
                return@launch
            }

            _entries.update { newList }
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
        var success: Boolean = false

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

    private fun fetchExampleEntries() {
        val dummyItems = listOf(
            DraftEntry("Entry 1", "this is an inside of the first entry content", emptyList()),
            DraftEntry("Entry 2 a title that is longer", "this is what makes the content of entry number 2", emptyList())
        )
        for (draft in dummyItems ) {
            addEntry(draft)
        }
        _entries.update {
            _entries.value.reversed()
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
}

class DocMakerAppViewModelFactory(
    private val savedStateHandle: SavedStateHandle,
    private val draftRepository: DraftRepository,
    private val autoDetailsRepository: AutoDetailsRepository,
    private val scope: CoroutineScope? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DocMakerAppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DocMakerAppViewModel(
                savedStateHandle,
                draftRepository,
                autoDetailsRepository,
                scope
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
