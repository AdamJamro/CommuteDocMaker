package com.example.commutedocmaker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import com.example.commutedocmaker.dataSource.DraftEntry
import com.example.commutedocmaker.ui.viewModels.DocMakerAppViewModel
import com.example.commutedocmaker.ui.viewModels.DocMakerAppViewModelFactory
import com.example.cupcake.ui.theme.CommuteDocMakerTheme

class MainActivity : ComponentActivity() {

    private val vm: DocMakerAppViewModel by viewModels(
        factoryProducer = {
            DocMakerAppViewModelFactory(
                SavedStateHandle(),
                (application as CommuteDocApplication).draftRepository,
                (application as CommuteDocApplication).autoDetailsRepository,
                lifecycleScope
            )
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
//        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Log.d("DEBUG", "MainActivity.onCreate")


        setContent {
            CommuteDocMakerTheme {
                DocMakerAppView(
                    viewModel = vm,
                    onOpenDraftEditor = { draftTitle: String,
                                          draftData: DraftEntry?,
                                          draftIndex: Int ->
                        //debug
                        draftData?.let {
                            if (it.draftDataPatches.isNotEmpty()) {
                                Log.d("DEBUG", "launchDraftEditor: draftData: $it " +
                                        "\n${it.title} " +
                                        "\n${it.draftDataPatches.first()}" +
                                        "\n${it.draftDataPatches.first().dates}")
                            }
                            else
                                Log.d("DEBUG", "launchDraftEditor: draftData: $it " +
                                        "\n${it.title} ")
                        }

                        launchDraftEditor(draftTitle, draftData)
                        vm.updateCurrentDraftPos(draftIndex)
                    }
                )
            }
        }

        Log.d("DEBUG", "ui setup complete")

    }

    override fun onPause() {
        super.onPause()
        vm.updateDatabase()
    }

    private val getResultFromDraftEditor = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val intent: Intent? = result.data
        val draft = getSerializable(intent, "draft_raw", DraftEntry::class.java)
        val draftIndex = vm.collectCurrentEditedDraftPos()
//        val draftIndex = intent?.getIntExtra("draft_index", -1) ?: -1

        when (result.resultCode) {
            RESULT_OK -> {
                if (draftIndex >= 0 && draftIndex <= vm.entries.value.lastIndex) {
                    val success = vm.updateEntry(draftIndex, draft)
                    val msg =
                        if (success)
                            "successfully updated"
                        else
                            "saving error: draft data lost"
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                }
                else {
                    vm.addEntry(draft)
                }
            }
            RESULT_CANCELED -> {
                //PASS
            }
            else -> {
                Toast.makeText(this, "Failure: Draft was not saved", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchDraftEditor(
        draftTitle: String?,
        draftData: DraftEntry?,
//        draftIndex: Int?
    ) {
        val intent = Intent(this, DraftEditorActivity::class.java)

        intent.apply {
            putExtra("draft_title", draftTitle)
            putExtra("draft_raw", draftData)
//            putExtra("draft_index", draftIndex)
        }
        getResultFromDraftEditor.launch(intent)
    }


}
