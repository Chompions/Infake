package com.sawelo.infake.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat.startForegroundService
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.sawelo.infake.R
import com.sawelo.infake.`object`.StaticObject
import com.sawelo.infake.dataClass.FlutterScreenData
import com.sawelo.infake.databinding.FragmentCreateBinding
import com.sawelo.infake.function.BitmapFunction
import com.sawelo.infake.function.IntentFunction
import com.sawelo.infake.function.SharedPrefFunction
import com.sawelo.infake.viewModel.CreateViewModel
import java.util.*


class CreateFragment : Fragment(R.layout.fragment_create) {
    private lateinit var photoResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestContactPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var contactResultLauncher: ActivityResultLauncher<Void>
    private lateinit var sharedPref: SharedPrefFunction
    private lateinit var intentFunction: IntentFunction
    private lateinit var bitmapFunction: BitmapFunction

    private var activeRouteValue: String? = null
    private val mapIdRoute = mutableMapOf<Int, FlutterScreenData>()

    private var _binding: FragmentCreateBinding? = null
    private val binding get() = _binding!!

    /**
     * Use activityViewModels() delegate class instead of viewModels() to create shared ViewModel
     * If viewModels() is used, then every fragment will access different ViewModel instance
     **/

    private val model: CreateViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = SharedPrefFunction(requireContext())
        intentFunction = IntentFunction(requireContext())
        bitmapFunction = BitmapFunction(requireContext())

        // Clean up sharedPref before starting anything
        with(sharedPref.editor) {
            clear()
            apply()
        }

        photoResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val data: Intent? = result.data
            if (result.resultCode == Activity.RESULT_OK && data != null) {
                val imageUri: Uri = data.data!!
                bitmapFunction.updateActivePhoto(
                    imageUri, sharedPref, binding
                )
            }
        }

        requestContactPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                contactResultLauncher.launch(null)
            } else {
                Toast.makeText(requireContext(), "Your permission is required", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        contactResultLauncher = registerForActivityResult(
            ActivityResultContracts.PickContact()
        ) { Uri ->
            val contentUri = Uri ?: android.net.Uri.parse(
                "android.resource://com.sawelo.infake/drawable/default_profile_picture"
            )

            println(contentUri)

            val contentResolver = requireContext().contentResolver

            val projection: Array<out String> = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                ContactsContract.Contacts.PHOTO_URI
            )

            val cursor: Cursor? = contentResolver.query(
                contentUri, projection, null, null, null
            )

            cursor?.moveToFirst()
            if (cursor?.moveToFirst() == true) {
                val contactId = cursor.getString(0)
                val contactLookup = cursor.getString(1)
                val contactName = cursor.getString(2)

                val hasPhoneNumber: Boolean = cursor.getInt(3) == 1
                val hasPhoto: Boolean = cursor.getString(4) != null

                var contactNumber = ""
                var imageUri: Uri? = null

                if (hasPhoneNumber) {
                    val numberCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = $contactId",
                        null, null
                    )

                    if (numberCursor?.moveToNext() == true) {
                        contactNumber = numberCursor.getString(
                            numberCursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.DATA1
                            )
                        )
                    }
                    numberCursor?.close()
                }

                if (hasPhoto) {
                    imageUri = android.net.Uri.parse(cursor.getString(4))
                    if (imageUri != null) {
                        bitmapFunction.updateActivePhoto(
                            imageUri, sharedPref, binding
                        )
                    }
                }

                println("Contact ID: $contactId")
                println("Contact Lookup: $contactLookup")
                println("Contact Name: $contactName ")
                println("Contact HasPhoneNumber: $hasPhoneNumber")
                println("Contact hasPhoto: $hasPhoto")
                println("Contact photoUri: $imageUri")
                println("Contact Number: $contactNumber")

                binding.contactName.setText(contactName)
                binding.contactNumber.setText(contactNumber)

            }
            cursor?.close()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        val mainScheduleTextObserver = Observer<String> {
            binding.scheduleCallButton.text = it
        }
        model.mainScheduleText.observe(viewLifecycleOwner, mainScheduleTextObserver)

        binding.useExistingContact.setOnClickListener { openContact() }
        binding.contactPictureCardView.setOnClickListener { openImage() }
        binding.cancelImage.setOnClickListener { cancelImage() }
        binding.scheduleCallButton.setOnClickListener { scheduleCall() }
        binding.createProfileButton.setOnClickListener { createProfile() }

        // Radio button
        createRadioButton()
        binding.callScreenRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            Log.d("CreateFragment", "CheckedId is: $checkedId")
            activeRouteValue = mapIdRoute[checkedId]?.incomingRouteName
        }

        clearEditText(binding.contactName)
        clearEditText(binding.contactNumber)
        binding.cancelImage.visibility = View.INVISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun clearEditText(appCompatEditText: AppCompatEditText) {
        appCompatEditText.setOnTouchListener { _, event ->
            val drawableRight = 2
            val checkAction: Boolean = event.action == MotionEvent.ACTION_UP
            val checkPressArea: Boolean = (
                    event.rawX >= (appCompatEditText.right - appCompatEditText.compoundDrawables[drawableRight].bounds.width() - appCompatEditText.paddingEnd)
                            && event.rawX <= (appCompatEditText.right - appCompatEditText.paddingEnd))

            if (checkAction) {
                if (checkPressArea) {
                    appCompatEditText.text?.clear()
                    return@setOnTouchListener true
                }
                return@setOnTouchListener false
            } else {
                return@setOnTouchListener false
            }
        }
    }

    /**
     * Create radio buttons programmatically below the existing Radio Group inside
     * fragment_create.xml. This function will run following the list inside
     * StaticObject.screenRouteList
     */
    private fun createRadioButton() {
        val radioGroup = requireView().findViewById<RadioGroup>(R.id.call_screen_radio_group)

        StaticObject.screenRouteList.forEach { screen ->
            // Create resource id
            val resId = requireContext().resources.getIdentifier(
                screen.drawableName, "drawable", requireContext().packageName
            )
            val radioButton = RadioButton(requireContext()).apply {
                id = resId
                buttonDrawable = screenStateSelector(resId)
                setPadding(0, 0, 10, 0)
            }
            radioGroup.addView(radioButton)
            // Assign resId to screen
            mapIdRoute[resId] = screen
        }
    }

    /**
     * This function will create selector programmatically without having to make xml
     * for each selector manually. Through this selector, drawable will have a state between
     * checked and not checked
     */
    @Suppress("Deprecation")
    private fun screenStateSelector(bitmapId: Int): StateListDrawable {
        val bitmap: Bitmap = BitmapFactory.decodeResource(resources, bitmapId)
        val scaledDownBitmap = Bitmap.createScaledBitmap(
            bitmap, 300, 600, true
        )

        val layer1 = BitmapDrawable(requireContext().resources, scaledDownBitmap)
        val layer2 = if (Build.VERSION.SDK_INT >= 23) {
            ColorDrawable(resources.getColor(R.color.blue_mid_transparent, null))
        } else {
            ColorDrawable(resources.getColor(R.color.blue_mid_transparent))
        }
        val mixLayer = LayerDrawable(arrayOf(layer1, layer2))

        /**
         * Use mixLayer to show layer-list with blue overlay
         * Use layer1 to show only the required bitmap
         */
        val res: StateListDrawable = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_checked), mixLayer)
            addState(intArrayOf(-android.R.attr.state_checked), layer1)

        }
        return res
    }

    private fun cancelImage() {
        if (binding.cancelImage.visibility == View.VISIBLE) {
            binding.contactPicture.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources, R.drawable.default_profile_picture, null
                )
            )
            binding.cancelImage.visibility = View.INVISIBLE

            with(sharedPref.editor) {
                remove(SharedPrefFunction.TEMP_IMAGE_BASE_64)
                apply()
            }
            Log.d("CreateFragment", "Cleaning image data")
        }
    }

    private fun openImage() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        photoResultLauncher.launch(intent)
    }

    private fun openContact() {
        requestContactPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
    }

    private fun scheduleCall() {
        ScheduleMenuFragment().show(parentFragmentManager, "ScheduleMenuFragment")
    }

    private fun createProfile() {
        // Put data from UI to sharedPref
        val sharedPref = SharedPrefFunction(requireContext())

        /**
         * Check if string is blank as in -> "" to insert null
         * Otherwise, use input string as value
         */
        fun checkBlank(input: String): String? {
            return if (input.isBlank() || input.isEmpty()) {
                println("Input string is blank or empty")
                null
            } else {
                println("Input string is $input")
                input
            }
        }

        with(sharedPref.editor) {
            putString(
                SharedPrefFunction.ACTIVE_NAME,
                checkBlank(binding.contactName.text.toString())
            )
            putString(
                SharedPrefFunction.ACTIVE_NUMBER,
                checkBlank(binding.contactNumber.text.toString())
            )
            putString(SharedPrefFunction.ACTIVE_ROUTE, activeRouteValue)
            putString(SharedPrefFunction.IMAGE_BASE_64, sharedPref.tempImageBase64)
            apply()
        }

        /**
         * Start the AlarmService only if activeRouteValue is not null
         * If null, then don't do anything, encourage the user to select one
         */
        if (activeRouteValue != null) {
            if (model.mainScheduleText.value == "Now" || model.mainScheduleText.value == "Schedule Call") {
                Toast.makeText(requireContext(), "Please wait for 10 seconds", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Profile created", Toast.LENGTH_SHORT).show()
            }
            startForegroundService(requireContext(), intentFunction.alarmServiceIntent)
        } else {
            Toast.makeText(requireContext(), "Please select a call screen", Toast.LENGTH_SHORT)
                .show()
        }

    }
}
