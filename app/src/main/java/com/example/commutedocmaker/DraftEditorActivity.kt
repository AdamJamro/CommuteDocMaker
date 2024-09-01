package com.example.commutedocmaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.commutedocmaker.dataSource.DraftEntry
import com.example.commutedocmaker.ui.DraftEditorView
import com.example.commutedocmaker.ui.viewModels.DraftEditorViewModelFactory

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
                viewModel = viewModel(factory = viewModelFactory),
                onFinishActivity = { resultCode: Int,
                                     draft: DraftEntry? ->
                    intent.apply {
                        putExtra("draft_raw", draft)
//                        putExtra("draft_index", draftIndex)
                    }
                    setResult(
                        resultCode,
                        intent
                    )
                    finish()
                }
            )
        }
    }
}

