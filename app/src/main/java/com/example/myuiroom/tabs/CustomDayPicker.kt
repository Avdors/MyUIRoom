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
import com.example.myuiroom.databinding.FragmentCustomDayPickerBinding


class CustomDayPicker: DialogFragment()  {
    private var listener: DayPickerListener? = null
    private lateinit var binding: FragmentCustomDayPickerBinding
    interface DayPickerListener {
        fun onDaySet(Day: Int)
    }

    fun setDayPickerListener(listener: DayPickerListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCustomDayPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_custom_day_picker, null)
        val dayPicker = view.findViewById<NumberPicker>(R.id.dayPicker).apply {
            minValue = 1
            maxValue = 365
        }
        builder.setView(view)
            .setPositiveButton("Done") { dialog, id ->
                listener?.onDaySet(dayPicker.value)
            }
            .setNegativeButton("Cancel") { dialog, id ->
                dialog.cancel()
            }
        return builder.create()
    }

}