package com.sawelo.infake.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.sawelo.infake.CreateViewModel
import com.sawelo.infake.R
import com.sawelo.infake.databinding.FragmentCreateBinding
import com.sawelo.infake.function.BitmapFunction
import com.sawelo.infake.function.IntentFunction
import com.sawelo.infake.function.SharedPrefFunction
import java.util.*


class CreateFragment : Fragment(R.layout.fragment_create) {
    private lateinit var photoResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var sharedPref: SharedPrefFunction
    private lateinit var intentFunction: IntentFunction

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
        intentFunction = IntentFunction(requireContext())

        photoResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            if (result.resultCode == Activity.RESULT_OK && data != null) {
                val imageUri: Uri = data.data!!
                val bitmapFunction = BitmapFunction(requireContext())
                val bitmap = bitmapFunction.generateBitmap(imageUri)
                val encodedString = bitmapFunction.convertBitmap(bitmap)

                with(sharedPref.editor) {
                    putString(SharedPrefFunction.IMAGE_BASE_64, encodedString)
                    apply()
                }

                binding.contactPicture.setImageBitmap(bitmap)
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

        // Check if string is blank as in -> ""
        fun checkBlank(input: String, default: String): String {
            return if (input.isBlank()) default else input
        }

        with (sharedPref.editor) {
            putString(SharedPrefFunction.ACTIVE_NAME, checkBlank(binding.contactName.text.toString(), sharedPref.activeName))
            putString(SharedPrefFunction.ACTIVE_NUMBER, checkBlank(binding.contactNumber.text.toString(), sharedPref.activeNumber))
//            putString(SharedPrefFunction.ACTIVE_ROUTE, checkBlank("/WhatsAppIncomingCall", sharedPref.activeRoute))
            apply()
        }
        // TODO: Change route input according to UI choices

        Log.d("CreateFragment", "Active data: ${sharedPref.activeName}, ${sharedPref.activeNumber}, ${sharedPref.activeRoute}")

        // Start AlarmService
        startForegroundService(requireContext(), intentFunction.alarmServiceIntent)
    }
}
