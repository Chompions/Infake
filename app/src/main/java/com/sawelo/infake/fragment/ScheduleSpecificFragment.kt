package com.sawelo.infake.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.sawelo.infake.CreateViewModel
import com.sawelo.infake.MenuData
import com.sawelo.infake.databinding.DialogSpecificScheduleBinding
import java.util.*

class ScheduleSpecificFragment : DialogFragment(), TimePicker.OnTimeChangedListener {
    private lateinit var _binding: DialogSpecificScheduleBinding
    private val binding get() = _binding

    private val model: CreateViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogSpecificScheduleBinding
            .inflate(LayoutInflater.from(context))

        binding.specificTime.setOnTimeChangedListener(this)

        val c = Calendar.getInstance()
        // Use the current time as the default values for the picker
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.specificTime.hour = c.get(Calendar.HOUR_OF_DAY)
            binding.specificTime.minute = c.get(Calendar.MINUTE)
        }

        binding.specificTime.setIs24HourView(true)

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setNeutralButton("Timer type") { _, _ ->
                ScheduleRelativeFragment().show(
                    parentFragmentManager, "ScheduleRelativeFragment")
            }
            .setPositiveButton("Ok") { _, _ ->
                model.mainSetTime(requireContext(), MenuData(
                    timerType = false,
                    hour = model.specificHourNum.value,
                    minute = model.specificMinuteNum.value
                ))
                model.dismissMenuDialog(fragment = this@ScheduleSpecificFragment)
                dialog?.dismiss()
            }
            .setNegativeButton("Cancel") { _, _ ->
                dialog?.dismiss()
            }
            .create()
    }

    override fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int) {
        model.updateSpecificTime(hourOfDay, minute)
    }

}