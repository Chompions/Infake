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
import com.sawelo.infake.R
import com.sawelo.infake.dataClass.ScheduleData
import com.sawelo.infake.databinding.DialogSpecificScheduleBinding
import com.sawelo.infake.function.CreateFragmentFunction
import com.sawelo.infake.viewModel.CreateViewModel
import java.util.*

class ScheduleSpecificFragment : DialogFragment(), TimePicker.OnTimeChangedListener {
    private lateinit var createFragmentFunction: CreateFragmentFunction
    private lateinit var _binding: DialogSpecificScheduleBinding
    private val binding get() = _binding

    private val model: CreateViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        createFragmentFunction = CreateFragmentFunction(requireContext(), model)
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
            createFragmentFunction.mainSetTime(
                ScheduleData(
                timerType = false,
                specificHour = model.specificHourNum.value ?: 0,
                specificMinute = model.specificMinuteNum.value ?: 0)
            )
            println("ScheduleSpecificFragment, Hour: ${model.specificHourNum.value}")
            println("ScheduleSpecificFragment, Minute: ${model.specificMinuteNum.value}")
            createFragmentFunction.dismissMenuDialog(fragment = this@ScheduleSpecificFragment)
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