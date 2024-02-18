package com.example.myuiroom.tabs

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myuiroom.R
import com.example.myuiroom.adapters.FileAdapter
import com.example.myuiroom.data.Database
import com.example.myuiroom.data.TaskDao
import com.example.myuiroom.databinding.AddFileBinding
import com.example.myuiroom.models.FileModel
import com.example.myuiroom.repositories.TaskRepository
import com.example.myuiroom.viewModels.FilesViewModel
import com.example.myuiroom.viewModels.FilesViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class AddFile : BottomSheetDialogFragment() {

    private lateinit var binding: AddFileBinding
    private lateinit var filesRecyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter
    private lateinit var viewModel: FilesViewModel
    private lateinit var repository: TaskRepository
    private lateinit var taskDao: TaskDao

    private var taskId: Int = 0

    // Assuming you have a method to get files for a specific task
    private var fileList: List<FileModel> = listOf() // Populate this list with your files

    private val filePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val filePath = getPathFromUri(uri)
                      if (filePath != null) {
                        // Save file path to database and update UI
                        saveFileToDatabase(filePath)
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            taskId = it.getInt(ARG_TASK_ID)

        }

        taskDao = Database.getInstance(requireContext()).taskDao
        repository = TaskRepository(taskDao)
        val factory = FilesViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(FilesViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = AddFileBinding.inflate(inflater, container, false)

        binding.addButton.setOnClickListener{
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }

            filePicker.launch(intent)

           // startActivityForResult(intent, REQUEST_CODE_PICK_FILE)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filesRecyclerView = view.findViewById(R.id.filesRecyclerView)
        setupRecyclerView()

        viewModel.getTaskWithFiles(taskId).observe(viewLifecycleOwner) { taskWithFiles ->
            if (taskWithFiles != null) {
                fileAdapter.updateFiles(taskWithFiles.files)
            } else {
                // Handle the case where taskWithFiles is null
                // For example, show a message or handle the error
            }

        }


    }

    private fun setupRecyclerView() {
        fileAdapter = FileAdapter(fileList) { file ->
            // Handle file click here
            openFile(file)
        }

        filesRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 3) // 3 columns
            adapter = fileAdapter
        }
    }

    private fun openFile(file: FileModel) {
        val context = requireContext()
        val relativePath = file.filePath.substringAfterLast("/files/")
        val fileToOpen = File(context.filesDir, relativePath)

        val fileUri = FileProvider.getUriForFile(context,
            "com.example.myuiroom.fileprovider", fileToOpen
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, getMimeType(relativePath))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)

    }

    private fun getMimeType(filePath: String): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(filePath)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
    }

    companion object {
        private const val ARG_TASK_ID = "idTask"
        private const val REQUEST_CODE_PICK_FILE = 1001
        @JvmStatic
        fun newInstance(taskId: Int) = AddFile().apply {
            arguments = Bundle().apply {
                putInt(ARG_TASK_ID, taskId)
            }
        }
    }


    private fun getPathFromUri(uri: Uri): String? {
        val context = requireContext()
        val contentResolver = context.contentResolver ?: return null


        // Try to retrieve the actual file name
        val originalFileName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.getString(nameIndex)
            } else null
        }

        // Fall back to last segment if DISPLAY_NAME not found
        val fileName = originalFileName ?: uri.lastPathSegment ?: return null

        val destinationFile = File(context.filesDir, fileName)


        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: IOException) {

            return null
        }

        return destinationFile.absolutePath

    }

    private fun saveFileToDatabase(filePath: String) {
        val fileModel = FileModel(fileId = 0, taskId = taskId, filePath = filePath)
        CoroutineScope(Dispatchers.IO).launch {
            //val taskDao = Database.getInstance(requireContext()).taskDao
            repository.insertFile(fileModel)
            // Update the UI after saving
            withContext(Dispatchers.Main) {
                updateFileList()
            }
        }
    }

    private fun updateFileList() {
        viewModel.getTaskWithFiles(taskId).observe(viewLifecycleOwner) { taskWithFiles ->
            fileAdapter.updateFiles(taskWithFiles.files)
        }
    }
}