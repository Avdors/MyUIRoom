package com.example.myuiroom.tabs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.example.myuiroom.R
import com.example.myuiroom.databinding.FragmentCustomTimePickerBinding


class CustomTimePicker : DialogFragment() {

    private var listener: TimePickerListener? = null
    private lateinit var binding: FragmentCustomTimePickerBinding
    interface TimePickerListener {
        fun onTimeSet(Day: Int, hourOfDay: Int, minute: Int)
    }

    fun setTimePickerListener(listener: TimePickerListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCustomTimePickerBinding.inflate(inflater, container, false)
        // return inflater.inflate(R.layout.fragment_custom_time_picker, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_custom_time_picker, null)
        val hourPicker = view.findViewById<NumberPicker>(R.id.hourPicker).apply {
            minValue = 0
            maxValue = 23
        }
        val minutePicker = view.findViewById<NumberPicker>(R.id.minutePicker).apply {
            minValue = 0
            maxValue = 59
        }
        val dayPicker = view.findViewById<NumberPicker>(R.id.dayPicker).apply {
            minValue = 0
            maxValue = 99
        }

        builder.setView(view)
            .setPositiveButton("Done") { dialog, id ->
                listener?.onTimeSet(dayPicker.value, hourPicker.value, minutePicker.value)
            }
            .setNegativeButton("Cancel") { dialog, id ->
                dialog.cancel()
            }
        return builder.create()

        //return super.onCreateDialog(savedInstanceState)
    }

}