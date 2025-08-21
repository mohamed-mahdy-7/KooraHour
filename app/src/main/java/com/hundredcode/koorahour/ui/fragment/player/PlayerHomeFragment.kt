package com.hundredcode.koorahour.ui.fragment.player

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hundredcode.koorahour.R
import com.hundredcode.koorahour.databinding.FragmentPlayerHomeBinding


class PlayerHomeFragment : Fragment() {
    private lateinit var binding: FragmentPlayerHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlayerHomeBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

}