package com.example.commutedocmaker.ui.viewModels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.commutedocmaker.dataSource.DraftEntry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DocMakerAppViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val _entries = MutableStateFlow<List<DraftEntry>>(emptyList())
//    private val _entries =  MutableStateFlow<List<DraftEntry>>(
//        savedStateHandle.get<String>("DraftEntryList")?.let { json -> gson.fromJson(json, object: TypeToken<List<DraftEntry>>() {}.type) } ?: emptyList()
//    )

    val entries: StateFlow<List<DraftEntry>> = _entries.asStateFlow()


    init {
        if (_entries.value.isEmpty())
            fetchEntries()
    }

    fun addEntry(entryTitle: String) {
        viewModelScope.launch {
            val newList = _entries.value.toMutableList()
            newList.add(DraftEntry(entryTitle, ""))
            _entries.value = newList
//            savedStateHandle["DraftEntryList"] = newList
        }
    }

    private fun addEntries(entries: List<DraftEntry>) {
        viewModelScope.launch {
            val newList = _entries.value.toMutableList()
            newList.addAll(entries)
            _entries.value = newList
//            savedStateHandle["DraftEntryList"] = newList
        }
    }

    private fun fetchEntries() {
        val dummyItems = listOf(DraftEntry("Entry 1", "this is an inside of the first entry content"), DraftEntry("Entry 2 a title that is longer", "this is what makes the content of entry number 2"))
        addEntries(dummyItems)
    }
}

class CommuteDocMakerViewModelFactory(private val savedStateHandle: SavedStateHandle) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DocMakerAppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DocMakerAppViewModel(savedStateHandle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
