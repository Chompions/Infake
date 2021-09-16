package com.sawelo.infake.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.sawelo.infake.`object`.StaticObject
import com.sawelo.infake.`object`.UpdateTextObject
import com.sawelo.infake.databinding.DialogMenuScheduleBinding
import com.sawelo.infake.function.CreateFragmentFunction
import com.sawelo.infake.viewModel.CreateViewModel

class ScheduleMenuFragment: DialogFragment() {
    private lateinit var createFragmentFunction: CreateFragmentFunction
    private lateinit var _binding: DialogMenuScheduleBinding
    private val binding get() = _binding

    private val model: CreateViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        createFragmentFunction = CreateFragmentFunction(requireContext(), model)
        _binding = DialogMenuScheduleBinding
            .inflate(LayoutInflater.from(context))

        menuButton()

        binding.customTime.setOnClickListener{
            ScheduleRelativeFragment().show(parentFragmentManager, "ScheduleRelativeFragment")
        }

        return AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .setTitle("Set Time")
            .setNegativeButton("Cancel") {_,_ -> }
            .create()
    }

    // Set text for each button in ScheduleMenuFragment
    private fun menuButton() {
       StaticObject.menuArray.forEachIndexed { i, menuData ->
            val button = when (i) {
                0 -> this.binding.button1
                1 -> this.binding.button2
                2 -> this.binding.button3
                else -> this.binding.button4
            }
            button.setOnClickListener {
                createFragmentFunction.mainSetTime(menuData)
                createFragmentFunction.dismissMenuDialog(this)
            }
            button.text = UpdateTextObject.updateMainText(menuData).first
        }
    }
}