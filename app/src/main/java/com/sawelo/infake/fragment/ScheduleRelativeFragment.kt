package com.sawelo.infake.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.sawelo.infake.CreateViewModel
import com.sawelo.infake.databinding.DialogRelativeScheduleBinding




class ScheduleRelativeFragment : DialogFragment(), NumberPicker.OnValueChangeListener {
    private lateinit var _binding: DialogRelativeScheduleBinding
    private val binding get() = _binding

    private val model: CreateViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogRelativeScheduleBinding
            .inflate(LayoutInflater.from(context))

        binding.relativeTimeHour.maxValue = 24
        binding.relativeTimeMinute.maxValue = 60
        binding.relativeTimeSecond.maxValue = 60

        binding.relativeTimeHour.setOnValueChangedListener(this)
        binding.relativeTimeMinute.setOnValueChangedListener(this)
        binding.relativeTimeSecond.setOnValueChangedListener(this)

        val relativeTimeTextObserver = Observer<String> { newText ->
            binding.relativeTimeText.text = newText
        }

        model.relativeTimeText.observe(this, relativeTimeTextObserver)

        return AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .setNeutralButton("Alarm type") { _, _ ->
                ScheduleSpecificFragment().show(
                    parentFragmentManager, "ScheduleSpecificFragment"
                )
            }
            .setPositiveButton("Ok") { _, _ ->
                model.setRelativeTime(requireContext())
                model.dismissMenuDialog(fragment = this@ScheduleRelativeFragment)
                dialog?.dismiss()
            }
            .setNegativeButton("Cancel") { _, _ ->
                dialog?.dismiss()
            }
            .create()
    }

    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
        model.updateRelativeTime(picker, binding, newVal)
    }
}