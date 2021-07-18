package com.sawelo.infake.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.sawelo.infake.CreateViewModel
import com.sawelo.infake.databinding.DialogMenuScheduleBinding

class ScheduleMenuFragment: DialogFragment() {
    private lateinit var _binding: DialogMenuScheduleBinding
    private val binding get() = _binding

    private val model: CreateViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogMenuScheduleBinding
            .inflate(LayoutInflater.from(context))

        model.menuButton(
            requireContext(),
            this@ScheduleMenuFragment,
            binding)

        binding.customTime.setOnClickListener{
            ScheduleRelativeFragment().show(parentFragmentManager, "ScheduleRelativeFragment")
        }

        return AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .setTitle("Set Time")
            .setNegativeButton("Cancel") {_,_ -> }
            .create()
    }
}