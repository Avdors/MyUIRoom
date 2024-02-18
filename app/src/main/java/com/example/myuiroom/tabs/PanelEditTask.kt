package com.example.myuiroom.tabs

import android.app.*

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.myuiroom.*
import com.example.myuiroom.Notification

import com.example.myuiroom.data.Database
import com.example.myuiroom.databinding.PanelEditTaskBinding
import com.example.myuiroom.models.FileModel
import com.example.myuiroom.models.TaskModel
import com.example.myuiroom.notices.*
import com.example.myuiroom.repositories.TaskRepository

import com.example.myuiroom.viewModels.TaskFactory
import com.example.myuiroom.viewModels.TaskViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField
import java.util.*

    @RequiresApi(Build.VERSION_CODES.O)
    class PanelEditTask : BottomSheetDialogFragment(), View.OnClickListener, CustomTimePicker.TimePickerListener{
    private var binding: PanelEditTaskBinding? = null
    private var nameFormParent: String? = ""
    private var taskAction: String? = ""
    private var complet: String? = null
    private var category: String? = null

        //для передачи части формы на форму more_properties.xml, через ViewStub
        private var morePropertiesView: View? = null

    private var idTask: Int? = null
    private var email: String? = "test"
    private var typeTask: String? = null

    private var taskRepository: TaskRepository? = null
    private var taskViewModel: TaskViewModel? = null
    private var taskFactory: TaskFactory? = null

        private var dayTask: Int? = 0
        private var hourTask: Int? = 12
        private var minuteTask: Int? = 0
        private var showAlertStr: String? = "false"

        private var scheduleNotification: ScheduleNotification? = null
        lateinit var  ibFamily: AppCompatTextView

        // для работы обратного вызова при закрытии этой панели
        private var dismissListener: PanelEditTaskDismissListener? = null
    //speech
    //
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var editText: EditText
    private var voiceInputCount = 0



    private var firstStart: Boolean = true
    private var currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
//            Log.d("PanelEditTask", "LocalDate: ${LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)}")
//        } else {
//            TODO("VERSION.SDK_INT < O")
//        }

        private var dateStartTaskTextView: AppCompatTextView? = null
        private var dateEndTaskTextView: AppCompatTextView? = null
        private var numberPickerDaysEditText: EditText? = null
        private var checkBoxNotification: CheckBox? = null
        private var checkBoxCompleted: CheckBox? = null
        private var infoNotification: AppCompatTextView? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialog)

        }
    private val dateFormatter: DateTimeFormatter
    init {
        dateFormatter = DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .toFormatter()
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = PanelEditTaskBinding.inflate(inflater, container, false)

        taskAction = arguments?.getString("taskAction").toString()
        nameFormParent = arguments?.getString("nameForm").toString()

        complet = arguments?.getString("completed").toString()
        category = arguments?.getString("category").toString()

        categoryBottonSelected()

        var dayTaskStr = arguments?.getString("dayTask").toString()
        val hourTaskStr = arguments?.getString("hourTask").toString()
        val minuteTaskStr = arguments?.getString("minuteTask").toString()

        if(dayTaskStr == "null" ){
            dayTaskStr = "0"

        }
        showAlertStr = arguments?.getString("showAlertStr").toString()
        dayTask = dayTaskStr?.toIntOrNull() ?: 0
        hourTask = hourTaskStr?.toIntOrNull() ?: 12
        minuteTask = minuteTaskStr?.toIntOrNull() ?: 0


        if(taskAction == "Edit") {
            idTask = arguments?.getString("idTask")?.toInt()
            binding?.editNameTask?.setText(arguments?.getString("nameTask").toString())
            email = arguments?.getString("email")?.toString()
            typeTask = arguments?.getString("typeTask")?.toString()
            binding?.editInfoTask?.setText(arguments?.getString("infoTask").toString())

        }
        else{
            val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val savedValue = sharedPreferences.getString("email", "default value")
            if(savedValue != "default value") {
                email = savedValue
            }
        }

        val taskDao = Database.getInstance((context as FragmentActivity).application).taskDao
        taskRepository = TaskRepository(taskDao)
        taskFactory = TaskFactory(taskRepository!!)
        taskViewModel = ViewModelProvider(this, taskFactory!!).get(TaskViewModel::class.java)

        scheduleNotification = ScheduleNotification()

        binding?.finishEdit?.setOnClickListener(this)
        binding?.ibClose?.setOnClickListener(this)
        binding?.ibFile?.setOnClickListener(this)
        binding?.ibDelete?.setOnClickListener(this)
        binding?.ibShare?.setOnClickListener{
            val name = binding?.editNameTask?.text.toString().trim()
            val info = binding?.editInfoTask?.text.toString().trim()
            val taskNameLabel = getString(R.string.name_task)
            val taskDescriptionLabel = getString(R.string.task_description)

            val combinedText = "$taskNameLabel $name\n$taskDescriptionLabel $info"

            shareText(combinedText)
        }

        //speech
        editText = binding?.editInfoTask!!

        val speechToText = binding?.imbMic

        speechToText?.setOnClickListener{
            startSpeechToText()
        }
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count == 1 && start == 0) {
                    editText.removeTextChangedListener(this)
                    editText.setText(s.toString().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
                    editText.setSelection(editText.text.length)
                    editText.addTextChangedListener(this)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        // Initialize buttons
        val ibFamily = binding?.ibFamily
        val ibJob = binding?.ibJob
        val ibMedical = binding?.ibHealth
        val ibTable = binding?.ibStudy
        val ibEverything = binding?.ibEverything
        // ... other buttons

        // Click listeners
        val buttons = listOf(ibFamily, ibJob, ibMedical, ibEverything, ibTable)
        buttons.forEach { button ->
            button?.setOnClickListener {
                // Deselect all buttons
                buttons.forEach { it?.isSelected = false }

                // Select the clicked button
                button.isSelected = true

                // Handle the button click event
                handleButtonClick(button.id)
            }
        }

        //ViewStub прослушиватель нажатия по тексту Дополнительные свойства
        binding?.moreProp?.setOnClickListener {
            toggleNumberDaysVisibility()
        }

        return binding?.root
    }
    //ViewStub реализация
        private fun toggleNumberDaysVisibility() {

    if (morePropertiesView == null) {

        inflateMoreProperties()
    } else {
        // Toggle visibility
        val newVisibility = if (morePropertiesView?.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        morePropertiesView?.visibility = newVisibility

    }
        }

        private fun inflateMoreProperties() {
            if (morePropertiesView == null) {
                val viewStub = binding?.stubNumberDays as? ViewStub
                morePropertiesView = viewStub?.inflate()
               // adjustConstraintsForInflatedView()
                setupMorePropertiesViews()
            }

        }

        private fun setupMorePropertiesViews() {
            morePropertiesView?.let { view ->
                // Ensure this method is called after the ViewStub is inflated
                dateStartTaskTextView = morePropertiesView?.findViewById(R.id.DateStartTask)
                dateEndTaskTextView = morePropertiesView?.findViewById(R.id.DateEndTask)
                numberPickerDaysEditText = morePropertiesView?.findViewById(R.id.numberPickerDays)
                checkBoxNotification = morePropertiesView?.findViewById(R.id.checkBoxNotification)
                checkBoxCompleted = morePropertiesView?.findViewById(R.id.checkBoxCompleted)
                infoNotification = morePropertiesView?.findViewById(R.id.infoNotification)

                // Set values based on arguments or saved state
                numberPickerDaysEditText?.text = Editable.Factory.getInstance().newEditable(arguments?.getString("dayTask", "0"))
                checkBoxCompleted?.isChecked = arguments?.getString("completed", "false").toBoolean()
                checkBoxNotification?.isChecked = arguments?.getString("showAlertStr", "false").toBoolean()
                dateStartTaskTextView?.text = arguments?.getString("dateStart").toString()
                dateEndTaskTextView?.text = if (checkBoxCompleted?.isChecked == true) arguments?.getString("dateEnd").toString() else getString(R.string.in_progres)

                if(showAlertStr == "true"){
                    checkBoxNotification?.isChecked = true
                    val time = getTime()
                    dateNotification(time)
                }else {
                    //binding?.infoNotification?.text = ""
                    infoNotification?.visibility = View.GONE
                }
                spinerProcessingMorePropert()

                // Setup listeners
                setupListeners()
            }
        }

        private fun setupListeners() {

            morePropertiesView?.findViewById<EditText>(R.id.numberPickerDays)?.let { editText ->
                // Log the current text value for debugging
                val currentText = editText.text.toString()


                editText.setOnClickListener {
                    // Here, directly use 'editText' reference to get the current value
                    val currentDayValueStr = editText.text.toString()
                    val currentDayValue = currentDayValueStr.toIntOrNull() ?: 0 // Default to 0 if null or not a number

                    // Now, create the fragment and pass the currentDayValue
                    val dayPickerFragment = CustomTimePicker().apply {
                        arguments = Bundle().apply {
                            putInt("dayValue", currentDayValue)
                        }
                    }
                    dayPickerFragment.setTimePickerListener(this)
                    dayPickerFragment.show(parentFragmentManager, "timePicker")
                }
            }

            morePropertiesView?.findViewById<CheckBox>(R.id.checkBoxCompleted)?.setOnCheckedChangeListener { _, isChecked ->

                    if (isChecked){
                        morePropertiesView?.findViewById<AppCompatTextView>(R.id.DateEndTask)?.text =  currentDate.toString()
                        complet = "true"
                    } else{
                        morePropertiesView?.findViewById<AppCompatTextView>(R.id.DateEndTask)?.text =  getString(R.string.enter_end_start)
                       complet = "false"
                    }

            }
            morePropertiesView?.findViewById<AppCompatTextView>(R.id.DateStartTask)?.setOnClickListener {
                showDatePickerDialog("Start")
            }

            morePropertiesView?.findViewById<AppCompatTextView>(R.id.DateEndTask)?.setOnClickListener {
                showDatePickerDialog("End")
            }

            morePropertiesView?.findViewById<CheckBox>(R.id.checkBoxNotification)?.setOnCheckedChangeListener { _, isChecked ->
                //showAlertStr = isChecked.toString()
                if (isChecked) {
                    // Process logic for when the notification checkbox is checked
                    showAlertStr = "true"
                    val time = getTime()
                    dateNotification(time)
                } else {
                    showAlertStr = "false"
                    morePropertiesView?.findViewById<AppCompatTextView>(R.id.infoNotification)?.visibility = View.GONE
                }
            }
        }
        // конец ViewStub реализация
        fun setDismissListener(listener: PanelEditTaskDismissListener) {
            dismissListener = listener
        }

        override fun onDismiss(dialog: DialogInterface) {
            super.onDismiss(dialog)
            dismissListener?.onPanelEditTaskDismissed()
        }
        private fun shareText(text: String) {

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, null))
        }
        fun showCustomToast(context: Context, message: String) {
            val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
            toast.show()

            Handler(Looper.getMainLooper()).postDelayed({
                toast.cancel()
            }, 800) // Display toast for 1000 milliseconds
        }

        private fun categoryBottonSelected(){
        when(category){
            "family" -> {
            binding?.ibFamily?.isSelected = true
            }
            "job" -> {
                binding?.ibJob?.isSelected = true
            }
            "study" -> {
                binding?.ibStudy?.isSelected = true
            }
            "health" -> {
                binding?.ibHealth?.isSelected = true
            }
            "everything" -> {
                binding?.ibEverything?.isSelected = true
            }
        }


        }

        private fun handleButtonClick(buttonId: Int) {
            when (buttonId) {
                R.id.ib_family -> category = "family"
                R.id.ib_job -> category = "job"
                R.id.ib_study -> category = "study"
                R.id.ib_health -> category = "health"
                R.id.ib_everything -> category = "everything"
                // Add other cases as needed
            }
            // Show toast for category selection
            showCustomToast(requireContext(), getString(R.string.you_selected_categoru) + " " + category)
            // Create or update the task based on whether it exists
            if (idTask == null) {
                createTask() // Ensure this method now correctly sets idTask
            } else {
                updateTask() // This should now only update the existing task, including setting its category
            }
        }
        //speech
        private val result = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
                val newText = results[0].replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                voiceInputCount++
                editText.append("$voiceInputCount. $newText\n")
                editText.setSelection(editText.text.length)
            }
        }
        private fun checkBatteryOptimizations(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Disable Battery Optimizations")
                        .setMessage("Please disable battery optimizations for reliable notifications.")
                        .setPositiveButton("Settings") { _, _ ->
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            startActivity(intent)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        }
        private fun showNotificationSettingsDialog(context: Context) {
            AlertDialog.Builder(context)
                .setTitle(getString(R.string.enable_notifications))
                .setMessage(getString(R.string.please_enable_settings))
                .setPositiveButton(getString(R.string.settings)) { dialog, _ ->
                    openNotificationSettings(context)
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
        private fun openNotificationSettings(context: Context) {
            val intent = Intent().apply {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                        action = "android.settings.APP_NOTIFICATION_SETTINGS"
                        putExtra("app_package", context.packageName)
                        putExtra("app_uid", context.applicationInfo.uid)
                    }
                    else -> {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        addCategory(Intent.CATEGORY_DEFAULT)
                        data = Uri.parse("package:" + context.packageName)
                    }
                }
            }
            context.startActivity(intent)
        }
        private fun startSpeechToText() {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something")
                }
                result.launch(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
       @RequiresApi(Build.VERSION_CODES.O)
    private fun showDatePickerDialog(type:String) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
           val dateStartTaskTextView: AppCompatTextView? = morePropertiesView?.findViewById(R.id.DateStartTask)
           val dateEndTaskTextView: AppCompatTextView? = morePropertiesView?.findViewById(R.id.DateEndTask)
        var dateTextView: AppCompatTextView? = null

        if(type == "End"){
        dateTextView = dateEndTaskTextView as AppCompatTextView
        }
        else if(type == "Start"){
            dateTextView = dateStartTaskTextView as AppCompatTextView
        }
        if(dateTextView != null) {
            val datePickerDialog = DatePickerDialog(
                context as FragmentActivity,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                    val formattedDate = selectedDate.format(dateFormatter)
                    dateTextView.setText(formattedDate)
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }
    }
        fun spinerProcessingMorePropert(){

            val taskTypeSpinner: Spinner? = morePropertiesView?.findViewById(R.id.taskTypeSpinner)
            val taskTypes = resources.getStringArray(R.array.type)
            val adapter = ArrayAdapter(context as FragmentActivity, android.R.layout.simple_spinner_item, taskTypes)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            taskTypeSpinner?.adapter = adapter

            if (firstStart && typeTask != null){
                val position = adapter.getPosition(typeTask)
                taskTypeSpinner?.setSelection(position)
                firstStart = false
            }
            taskTypeSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    typeTask = parent?.getItemAtPosition(position).toString()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
        }
    override fun onClick(view: View) {
    when (view.id) {
    R.id.finishEdit -> {
        // инициализирую заполнение скрытых полей more_properties
        if (morePropertiesView == null) {
            toggleNumberDaysVisibility()
        }
           if (
               dateStartTaskTextView?.text?.toString()!!.isNotEmpty() and
               dateEndTaskTextView?.text?.toString()!!.isNotEmpty()
           ) {

           if (nameFormParent == "TaskAll" && taskAction != "Edit") {

               createTask()
               dismiss()
               (context as FragmentActivity).supportFragmentManager.beginTransaction()
                   .replace(R.id.content, TaskAll()).commit()
           } else if (nameFormParent == "TaskAll" && taskAction == "Edit") {

               updateTask()

               dismiss()
               (context as FragmentActivity).supportFragmentManager.beginTransaction()
                   .replace(R.id.content, TaskAll()).commit()
           } else if (nameFormParent == "TaskForType" && taskAction == "Edit") {

               updateTask()

               if (showAlertStr == "true") {
                   grateNotification()
               }
               dismiss()
               (context as FragmentActivity).supportFragmentManager.beginTransaction()
                   .replace(R.id.content, TaskForType()).commit()
           } else if (nameFormParent == "TaskForType" && taskAction != "Edit") {
               createTask()
               //grateNotification()
               dismiss()

               (context as FragmentActivity).supportFragmentManager.beginTransaction()
                   .replace(R.id.content, TaskForType()).commit()
           }
       } else
           Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show()
   }
        R.id.ib_close ->{
            dismiss()
        }
        R.id.ib_file ->{
            if(idTask != null) {
 //               dismiss()
                val addFile = AddFile()
                val parameters = Bundle()
                addFile.arguments = parameters
                parameters.putInt("idTask", idTask!!)
//                (context as FragmentActivity).supportFragmentManager.beginTransaction()
//                    .replace(R.id.content, addFile).commit()
                addFile.show((context as FragmentActivity).supportFragmentManager, "addFile")
            }
        }
        R.id.ib_delete ->{
            if (idTask != null && nameFormParent == "TaskForType") {

                showConfirmationDialog(getString(R.string.file_deletion), getString(R.string.question_delete))
            }else{
                dismiss()
                if (nameFormParent == "TaskForType") {
                    (context as FragmentActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.content, TaskForType()).commit()
                }else{
                    (context as FragmentActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.content, TaskAll()).commit()
                }
            }
        }
    }
    }

        private fun createTask(){
            //проверка на случай незаполнения обязательных полей
            if(binding?.editNameTask?.text?.isEmpty() == true){
                binding?.editNameTask?.setText(getString(R.string.unnamed_task))
            }
            if(binding?.editInfoTask?.text?.isEmpty() == true){
                binding?.editInfoTask?.setText(getString(R.string.one))
            }
            // прописываю тип по умолчанию если он не заполнен
            if(typeTask != "null"){
                typeTask = "important"
            }
            // инициализирую заполнение скрытых полей more_properties
            if (morePropertiesView == null) {
                toggleNumberDaysVisibility()
            }
            taskViewModel?.startInsert(
                binding?.editNameTask?.text?.toString()!!,
                email.toString(),
                typeTask.toString(),
                binding?.editInfoTask?.text?.toString()!!,
                dateStartTaskTextView?.text?.toString()!!,
                dateEndTaskTextView?.text?.toString()!!,
                complet.toString(),
                dayTask!!,
                hourTask!!,
                minuteTask!!,
                showAlertStr.toString()
            ) { taskId ->
                idTask = taskId.toInt() // Set the generated task ID
                if (showAlertStr == "true") {
                    grateNotification()
                }
            }
            // если задача создана, меняю режим открытия панели
            taskAction = "Edit"
        }
        private fun updateTask(){
            // инициализирую заполнение скрытых полей more_properties
            if (morePropertiesView == null) {
                toggleNumberDaysVisibility()
            }
            if (idTask != null) {
                taskViewModel?.startUpdateTask(
                    idTask?.toInt()!!,
                    binding?.editNameTask?.text?.toString()!!,
                    email.toString(),
                    typeTask.toString(),
                    binding?.editInfoTask?.text?.toString()!!,
                    dateStartTaskTextView?.text?.toString()!!,
                    dateEndTaskTextView?.text?.toString()!!,
                    complet.toString(),
                    dayTask!!,
                    hourTask!!,
                    minuteTask!!,
                    showAlertStr.toString(),
                    category.toString()
                )
            }
        }
        private fun deleteTask(){
            // инициализирую заполнение скрытых полей more_properties
            if (morePropertiesView == null) {
                toggleNumberDaysVisibility()
            }
            taskViewModel?.deleteTask(TaskModel(
                idTask?.toInt()!!,
                binding?.editNameTask?.text?.toString()!!,
                email.toString(),
                typeTask.toString(),
                binding?.editInfoTask?.text?.toString()!!,
                dateStartTaskTextView?.text?.toString()!!,
                dateEndTaskTextView?.text?.toString()!!,
                complet.toString(),
                dayTask!!,
                hourTask!!,
                minuteTask!!,
                showAlertStr.toString(),
                category.toString()
            ))
            dismiss()
            if (nameFormParent == "TaskForType") {
                (context as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.content, TaskForType()).commit()
            }else{
                (context as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.content, TaskAll()).commit()
            }
        }
    // Проверяю есть ли разрешение на уведомления
        @RequiresApi(Build.VERSION_CODES.Q)
        private fun areNotificationsEnabled(context: Context): Boolean {

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                return notificationManager.areNotificationsEnabled()
            } else {
                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val appInfo = context.applicationInfo
                val pkg = context.applicationContext.packageName
                val uid = appInfo.uid

                return appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, uid, pkg) == AppOpsManager.MODE_ALLOWED
            }
        }
    private fun grateNotification(){
      //если не открывались доп реквизиты, то мы в любом случае не создаем повторное уведомление
        if (morePropertiesView != null) {
            val time = getTime()

            val currentTime = System.currentTimeMillis()

            //проверяю чтобы дата уведомления была не меньше текущей даты
            if (time > currentTime) {
                scheduleNotification?.createNotifForTime(
                    requireContext(),
                    idTask!!,
                    time,
                    binding?.editNameTask?.text.toString()
                )

                showAlert(
                    time,
                    getString(R.string.close_task),
                    binding?.editNameTask?.text.toString()
                )
            } else {
            }
        }
    }
    private fun showAlert(time: Long, title: String, message: String) {
        val date = Date(time)
        val dateFormat = android.text.format.DateFormat.getLongDateFormat(requireContext())
        val timeFormat = android.text.format.DateFormat.getTimeFormat(requireContext())

        AlertDialog.Builder(context)
            .setTitle(getString(R.string.notifications_will_send))
            .setMessage(
                "Title: " + title +
                        "\nMessage: " + message +
                        "\nAt: " + dateFormat.format(date) + " " + timeFormat.format(date)
            )
            .setPositiveButton(getString(R.string.okkay)){_,_ ->}
            .show()
    }
        private fun showConfirmationDialog(title: String, message: String) {
            // инициализирую заполнение скрытых полей more_properties
            if (morePropertiesView == null) {
                toggleNumberDaysVisibility()
            }

            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(message)

            builder.setPositiveButton("Yes") { dialog, which ->
               val modelTask =  TaskModel(
                    idTask?.toInt()!!,
                    binding?.editNameTask?.text?.toString()!!,
                    email.toString(),
                    typeTask.toString(),
                    binding?.editInfoTask?.text?.toString()!!,
                    dateStartTaskTextView?.text?.toString()!!,
                    dateEndTaskTextView?.text?.toString()!!,
                    complet.toString(),
                    dayTask!!,
                    hourTask!!,
                    minuteTask!!,
                    showAlertStr.toString(),
                    category.toString())

                taskViewModel?.loadUniqueFiles(idTask?.toInt()!!, modelTask)
            }

            builder.setNegativeButton("No") { dialog, which ->
                // User clicked "No" button, do nothing
                dialog.dismiss()
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

    private fun dateNotification(time: Long) {
            val date = Date(time)
            val dateFormat = android.text.format.DateFormat.getLongDateFormat(requireContext())
            val timeFormat = android.text.format.DateFormat.getTimeFormat(requireContext())

           // binding?.infoNotification?.setText("A reminder to close the task will be displayed: " + dateFormat.format(date) + " " + timeFormat.format(date))
        // для ViewStub
        //val dateText = dateFormat.format(Date(time)) + " " + timeFormat.format(Date(time))
        val textReminder: String = getString(R.string.notification_will_be_shown_at) + " "
        morePropertiesView?.findViewById<AppCompatTextView>(R.id.infoNotification)?.setText(textReminder + dateFormat.format(date) + " " + timeFormat.format(date))
    }

    private fun getTime(): Long {
        val calendar = Calendar.getInstance()
        //calendar.set(year!!, month!!,day!!,hour!!,minute!!)

        // We Replacing the procedure with a new scheme

        val startDateText = dateStartTaskTextView?.text.toString()
        try {
            val parsedDate = LocalDate.parse(startDateText, dateFormatter)
            calendar.set(parsedDate.year, parsedDate.monthValue - 1, parsedDate.dayOfMonth)
        } catch (e: DateTimeParseException) {

            // Handle the error, perhaps by using the current date or notifying the user
        }
        calendar.add(Calendar.DAY_OF_MONTH, dayTask ?: 0)
        calendar.set(Calendar.HOUR_OF_DAY, hourTask ?: 12)
        calendar.set(Calendar.MINUTE, minuteTask ?: 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

        override fun onTimeSet(day: Int, hourOfDay: Int, minute: Int) {
            val numberPickerDaysEditText: EditText? = morePropertiesView?.findViewById(R.id.numberPickerDays)
            numberPickerDaysEditText?.setText(day.toString())
           // binding?.numberPickerDays?.setText(day.toString())
            dayTask = day
            hourTask = hourOfDay
            minuteTask = minute
        }
    }