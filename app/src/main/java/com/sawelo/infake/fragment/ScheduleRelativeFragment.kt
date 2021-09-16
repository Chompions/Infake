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
import com.sawelo.infake.R
import com.sawelo.infake.`object`.StaticObject
import com.sawelo.infake.dataClass.ScheduleData
import com.sawelo.infake.databinding.DialogRelativeScheduleBinding
import com.sawelo.infake.function.CreateFragmentFunction
import com.sawelo.infake.viewModel.CreateViewModel


class ScheduleRelativeFragment : DialogFragment(), NumberPicker.OnValueChangeListener {
    private lateinit var createFragmentFunction: CreateFragmentFunction
    private lateinit var _binding: DialogRelativeScheduleBinding
    private val binding get() = _binding

    private val model: CreateViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        createFragmentFunction = CreateFragmentFunction(requireContext(), model)
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
            createFragmentFunction.mainSetTime(
                ScheduleData(
                timerType = true,
                relativeHour = model.relativeHourNum.value ?: 0,
                relativeMinute = model.relativeMinuteNum.value ?: 0,
                relativeSecond = model.relativeSecondNum.value ?: 0
            )
            )
            createFragmentFunction.dismissMenuDialog(fragment = this@ScheduleRelativeFragment)
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
        if (picker != null) {
            when (picker.id) {
                this.binding.relativeTimeHour.id -> model.updateRelativeTime(
                    Pair(StaticObject.TimeEnum.HOUR.name, newVal)
                )
                this.binding.relativeTimeMinute.id -> model.updateRelativeTime(
                    Pair(StaticObject.TimeEnum.MINUTE.name, newVal)
                )
                this.binding.relativeTimeSecond.id -> model.updateRelativeTime(
                    Pair(StaticObject.TimeEnum.SECOND.name, newVal)
                )
            }
        }
    }
}