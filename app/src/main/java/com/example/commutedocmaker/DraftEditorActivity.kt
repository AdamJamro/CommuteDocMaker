package com.example.commutedocmaker

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.commutedocmaker.ui.CreateNewDraft

class DraftEditorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CreateNewDraft(
                onFinishActivity = { resultFilepath: String? ->
                    val intent = Intent().apply {
                        putExtra("content", resultFilepath)
                    }
                    setResult(
                        /* resultCode = */ RESULT_OK,
                        /* data = */ intent
                    )
                    finish()
                }
            )
        }
    }
}

