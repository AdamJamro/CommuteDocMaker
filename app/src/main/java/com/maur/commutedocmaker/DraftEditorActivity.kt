package com.maur.commutedocmaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maur.commutedocmaker.dataSource.draftEntry.DraftEntry
import com.maur.commutedocmaker.dataSource.preference.PreferenceType
import com.maur.commutedocmaker.dataSource.preference.PreferenceType.ACCESS.DENIED
import com.maur.commutedocmaker.ui.views.DraftEditorView
import com.maur.commutedocmaker.ui.viewModels.DraftEditorViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DraftEditorActivity : ComponentActivity() {
    private lateinit var viewModelFactory: DraftEditorViewModelFactory


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val draft = getSerializable(intent, "draft_raw", DraftEntry::class.java)
        val title = intent.getStringExtra("draft_title") ?: "new draft"
        val draftIndex = intent.getIntExtra("draft_index", -1)
        viewModelFactory = DraftEditorViewModelFactory(
            title,
            scope = lifecycleScope,
            existingDraftEntry = draft,
//            draftIndex = draftIndex
        )


        setContent {
            DraftEditorView(
                modifier = Modifier.safeContentPadding(),
                viewModel = viewModel(factory = viewModelFactory),
                onFinishActivity = { resultCode: Int,
                                     draft: DraftEntry? ->
                    lifecycleScope.launch {
                        intent.apply {
                            putExtra("draft_raw", draft)
//                        putExtra("draft_index", draftIndex)
                        }
                        if(verifyUserAccess()) {
                            setResult(
                                resultCode,
                                intent
                            )
                        } else {
                            setResult(RESULT_CANCELED)
                        }

                        withContext(Dispatchers.Main) {
                            if(isActive)
                                finish()
                        }
                    }
                }
            )
        }
    }

    private suspend fun verifyUserAccess(): Boolean {
        (application as CommuteDocApplication)
            .preferenceRepository
            .getPreference(PreferenceType.ACCESS.key)?.let {
                if(it == DENIED) {
                    return false
                }
            }
        return true
    }
}

