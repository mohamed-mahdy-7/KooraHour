package com.hundredcode.koorahour.ui.fragment.player

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hundredcode.koorahour.R
import com.hundredcode.koorahour.databinding.FragmentPlayerProfileFragmentBinding


class PlayerProfileFragment : Fragment() {
    private lateinit var binding: FragmentPlayerProfileFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlayerProfileFragmentBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

}