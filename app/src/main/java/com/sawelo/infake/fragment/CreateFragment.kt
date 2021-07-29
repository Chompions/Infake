package com.sawelo.infake.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat.startForegroundService
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sawelo.infake.ContactData
import com.sawelo.infake.CreateViewModel
import com.sawelo.infake.R
import com.sawelo.infake.databinding.FragmentCreateBinding
import com.sawelo.infake.function.SharedPrefFunction
import com.sawelo.infake.service.AlarmService
import java.util.*


class CreateFragment : Fragment(R.layout.fragment_create) {
    private lateinit var photoResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var sharedPref: SharedPrefFunction

    private var _binding: FragmentCreateBinding? = null
    private val binding get() = _binding!!

    /**
     * Use activityViewModels() delegate class instead of viewModels() to create shared ViewModel
     * If viewModels() is used, then every fragment will access different ViewModel instance
     **/

    private val model: CreateViewModel by activityViewModels()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        sharedPref = SharedPrefFunction(requireContext())

        @Suppress("Deprecation")
        photoResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data

            if (result.resultCode == Activity.RESULT_OK && data != null) {
                val imageUri = data.data

                try {
                    imageUri?.let {
                        if(Build.VERSION.SDK_INT > 28) {
                            val source = ImageDecoder.createSource(
                                requireActivity().contentResolver,
                                imageUri)
                            val bitmap = ImageDecoder.decodeBitmap(source)
                            binding.contactPicture.setImageBitmap(bitmap)
                        } else {
                            val bitmap = MediaStore.Images.Media.getBitmap(
                                requireActivity().contentResolver,
                                imageUri
                            )
                            binding.contactPicture.setImageBitmap(bitmap)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        _binding = FragmentCreateBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel = model
            createFragment = this@CreateFragment
            lifecycleOwner = viewLifecycleOwner
        }

        clearEditText(binding.contactName)
        clearEditText(binding.contactNumber)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("ClickableViewAccessibility")
    fun clearEditText(appCompatEditText: AppCompatEditText) {
        appCompatEditText.setOnTouchListener {_, event ->
            val drawableRight = 2
            val checkAction: Boolean = event.action == MotionEvent.ACTION_UP
            val checkPressArea: Boolean = (
                    event.rawX >= (appCompatEditText.right - appCompatEditText.compoundDrawables[drawableRight].bounds.width() - appCompatEditText.paddingEnd)
                            && event.rawX <= (appCompatEditText.right - appCompatEditText.paddingEnd))

            if (checkAction) {
                if(checkPressArea) {
                    appCompatEditText.text?.clear()
                    return@setOnTouchListener true
                }
                return@setOnTouchListener false
            } else {
                return@setOnTouchListener false
            }
        }
    }

    fun openImage() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        photoResultLauncher.launch(intent)
    }

    fun scheduleCall() {
        ScheduleMenuFragment().show(parentFragmentManager, "ScheduleMenuFragment")
    }

    fun createProfile() {
        // Put data from UI to sharedPref
        val sharedPref = SharedPrefFunction(requireContext())
        sharedPref.putStringData(ContactData(
                binding.contactName.text.toString(),
                binding.contactNumber.text.toString(),
                "/WhatsAppIncomingCall"
        ))
        // TODO: Change route input according to UI choices

        Log.d("CreateFragment", "Active data: " +
                sharedPref.activeName +
                sharedPref.activeNumber +
                sharedPref.activeRoute)

        // Start AlarmService
        val alarmServiceIntent = Intent(requireContext(), AlarmService::class.java)
        startForegroundService(requireContext(), alarmServiceIntent)
    }
}
