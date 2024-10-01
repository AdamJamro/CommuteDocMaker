package com.maur.commutedocmaker.ui.uiUtils

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SyncedStateList<T : Any> (
    private val stateFlow: StateFlow<List<T>>,
) {

    val _snapshotStateList = mutableStateListOf<T>()

    init {
        CoroutineScope(Dispatchers.Main).launch {
            stateFlow.collectLatest { listData ->
                syncLists(listData)
            }
        }
    }


    operator fun get(index: Int): T = _snapshotStateList[index]

    @Composable
    fun collectAsState(): State<List<T>> {
        val state = stateFlow.collectAsState()

        SideEffect {
            syncLists(state.value)
        }

        return state
    }

    private fun syncLists(newList: List<T>) {
        val oldList = _snapshotStateList.toList()
        val diff = calculateDiff(oldList, newList)

        diff.forEach { (index, item) ->
            when (item) {
                is DiffItem.Add -> _snapshotStateList.add(index, item.value)
                is DiffItem.Remove -> _snapshotStateList.removeAt(index)
                is DiffItem.Update -> _snapshotStateList[index] = item.value
            }
        }
    }

    private fun calculateDiff(oldList: List<T>, newList: List<T>): List<Pair<Int, DiffItem<T>>> {
        val diff = mutableListOf<Pair<Int, DiffItem<T>>>()
        var oldIndex = 0
        var newIndex = 0

        while (oldIndex < oldList.size || newIndex < newList.size) {
            when {
                oldIndex >= oldList.size -> {
                    diff.add(newIndex to DiffItem.Add(newList[newIndex]))
                    newIndex++
                }
                newIndex >= newList.size -> {
                    diff.add(oldIndex to DiffItem.Remove)
                    oldIndex++
                }
                oldList[oldIndex] != newList[newIndex] -> {
                    diff.add(newIndex to DiffItem.Update(newList[newIndex]))
                    oldIndex++
                    newIndex++
                }
                else -> {
                    oldIndex++
                    newIndex++
                }
            }
        }

        return diff
    }

    sealed class DiffItem<out T> {
        data class Add<T>(val value: T) : DiffItem<T>()
        data object Remove : DiffItem<Nothing>()
        data class Update<T>(val value: T) : DiffItem<T>()
    }
}