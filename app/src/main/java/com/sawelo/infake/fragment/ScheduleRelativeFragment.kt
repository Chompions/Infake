package com.sawelo.infake.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.sawelo.infake.CreateViewModel
import com.sawelo.infake.R
import com.sawelo.infake.ScheduleData
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

        binding.relativeTimeHour.value = model.relativeHourNum.value?: 0
        binding.relativeTimeMinute.value = model.relativeMinuteNum.value?: 0
        binding.relativeTimeSecond.value = model.relativeSecondNum.value?: 0

        binding.relativeTimeHour.setOnValueChangedListener(this)
        binding.relativeTimeMinute.setOnValueChangedListener(this)
        binding.relativeTimeSecond.setOnValueChangedListener(this)

        val relativeTimeTextObserver = Observer<String> { newText ->
            binding.relativeTimeText.text = newText
        }

        model.relativeTimeText.observe(this, relativeTimeTextObserver)

        val span = SpannableString("i").apply {
            setSpan(
                ImageSpan(
                requireContext(),
                R.drawable.ic_baseline_access_alarm),
                0, 1 ,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.alarmType.text = span

        binding.alarmType.setOnClickListener{
            ScheduleSpecificFragment().show(
                parentFragmentManager, "ScheduleSpecificFragment"
            )
            dialog?.dismiss()
        }

        binding.okBtn.setOnClickListener{
            model.mainSetTime(requireContext(), ScheduleData(
                timerType = true,
                hour = model.relativeHourNum.value,
                minute = model.relativeMinuteNum.value,
                second = model.relativeSecondNum.value
            ))
            model.dismissMenuDialog(fragment = this@ScheduleRelativeFragment)
            dialog?.dismiss()
        }

        binding.cancelBtn.setOnClickListener{
            dialog?.dismiss()
        }

        return AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .create()
    }

    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
        model.updateRelativeTime(picker, binding, newVal)
    }
}