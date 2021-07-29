package com.sawelo.infake.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.sawelo.infake.CreateViewModel
import com.sawelo.infake.R
import com.sawelo.infake.ScheduleData
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

        val span = SpannableString("i").apply {
            setSpan(ImageSpan(
                requireContext(),
                R.drawable.ic_baseline_timer),
                0, 1 ,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.timerType.text = span

        binding.timerType.setOnClickListener{
            ScheduleRelativeFragment().show(
                parentFragmentManager, "ScheduleRelativeFragment")
            dialog?.dismiss()
        }

        binding.okBtn.setOnClickListener{
            model.mainSetTime(requireContext(), ScheduleData(
                timerType = false,
                hour = model.specificHourNum.value,
                minute = model.specificMinuteNum.value
            ))
            model.dismissMenuDialog(fragment = this@ScheduleSpecificFragment)
            dialog?.dismiss()
        }

        binding.cancelBtn.setOnClickListener{
            dialog?.dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int) {
        model.updateSpecificTime(hourOfDay, minute)
    }

}