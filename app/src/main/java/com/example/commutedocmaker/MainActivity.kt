package com.example.commutedocmaker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.commutedocmaker.ui.viewModels.DocMakerAppViewModel
import com.example.cupcake.ui.theme.CommuteDocMakerTheme

class MainActivity : ComponentActivity() {

    private val vm: DocMakerAppViewModel by viewModels()
    private lateinit var filename: String

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            CommuteDocMakerTheme {
                DocMakerApp(
                    viewModel = vm,
                    onCreateNewDraft = { name: String ->
                        filename = name
                        launchDraftEditor()
                    }
                    )
            }
        }
    }

    private val getResultFromDraftEditor = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data: Intent? = result.data
        val content = data?.getStringExtra("content")
        if (result.resultCode == RESULT_OK && content != null) {
            vm.addEntry(filename)
        } else {
            Toast.makeText(this, "Draft not saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchDraftEditor() {
        val intent = Intent(this, DraftEditorActivity::class.java)
        getResultFromDraftEditor.launch(intent)
    }


}
