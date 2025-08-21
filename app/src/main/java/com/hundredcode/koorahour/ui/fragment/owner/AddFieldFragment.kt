package com.hundredcode.koorahour.ui.fragment.owner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hundredcode.koorahour.R
import com.hundredcode.koorahour.databinding.FragmentAddFieldBinding


class AddFieldFragment : Fragment() {
    private lateinit var binding: FragmentAddFieldBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddFieldBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

}