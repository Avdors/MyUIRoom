package com.example.myuiroom.tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myuiroom.R
import com.example.myuiroom.databinding.LoadingDataBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class LoadingData : BottomSheetDialogFragment() {

private var binding: LoadingDataBinding? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = LoadingDataBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun getTheme(): Int = R.style.CustomBottomSheetDialog

    companion object {
        fun newInstance() = LoadingData()
    }


}