package com.maur.commutedocmaker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Debug
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import com.maur.commutedocmaker.dataSource.document.Document
import com.maur.commutedocmaker.dataSource.draftEntry.DraftEntry
import com.maur.commutedocmaker.dataSource.preference.PreferenceType
import com.maur.commutedocmaker.dataSource.preference.PreferenceType.ACCESS.GRANTED
import com.maur.commutedocmaker.viewModels.DocMakerAppViewModel
import com.maur.commutedocmaker.viewModels.DocMakerAppViewModelFactory
import com.maur.cupcake.ui.theme.CommuteDocMakerTheme

class MainActivity : ComponentActivity() {

    private val vm: DocMakerAppViewModel by viewModels(
        factoryProducer = {
            DocMakerAppViewModelFactory(
                SavedStateHandle(),
                (application as CommuteDocApplication).draftRepository,
                (application as CommuteDocApplication).autoDetailsRepository,
                (application as CommuteDocApplication).preferenceRepository,
                (application as CommuteDocApplication).documentRepository,
                lifecycleScope
            )
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
//        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if(Debug.isDebuggerConnected())
            finish()

        Log.d("DEBUG", "MainActivity.onCreate")


        setContent {
            CommuteDocMakerTheme(
                dynamicColor = true,
            ) {
                DocMakerAppView(
                    viewModel = vm,
                    onOpenDraftEditor = { draftTitle: String,
                                          draftData: DraftEntry?,
                                          draftIndex: Int ->
                        //debug
                        draftData?.apply {
                            if (draftDataPatches.isNotEmpty()) {
                                Log.d("DEBUG", "launchDraftEditor: draftData: $this " +
                                        "\n${title} " +
                                        "\n${draftDataPatches.first()}" +
                                        "\n${draftDataPatches.first().dates}")
                            }
                            else
                                Log.d("DEBUG", "launchDraftEditor: draftData: $this " +
                                        "\n${title} ")
                        }

                        launchDraftEditor(draftTitle, draftData)
                        vm.updateCurrentDraftPos(draftIndex)
                    },
                    onSendDocument = { document: Document ->
                        val excelUri: Uri = vm.getFileUri(document.path, applicationContext)
                        val shareIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_STREAM, excelUri)
                            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        startActivity(Intent.createChooser(shareIntent, stringRes(this, R.string.share_doc)))
                    },
                    onGenerateDocument = { draft: DraftEntry ->
                        vm.updateDatabase()
                        Toast.makeText(this, "Generating doc...", Toast.LENGTH_SHORT).show()
                        var document: Document? = null
                        var reason: String = ""
                        vm.getPreference(PreferenceType.ACCESS)?.also { access ->
                            if (access == GRANTED) {
                                document = vm.generateDocument(draft, applicationContext)
                            } else {
                                reason = "Access denied."
                            }
                        }
                        val result = document != null
                        val response =
                            if (result) "Document generated successfully"
                            else "Document failed to be generated. $reason"
                        Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
                        //return
                        result
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
                if (draft == null){
                    Toast.makeText(this, "Failure: Draft was not saved. Try again", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                else if (draftIndex >= 0 && draftIndex <= vm.entries.value.lastIndex) {
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
