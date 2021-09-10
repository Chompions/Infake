package com.sawelo.infake.function

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import com.sawelo.infake.databinding.FragmentCreateBinding
import java.io.ByteArrayOutputStream

class BitmapFunction(context: Context) {
    private val mContext: Context = context

    /**
     * This function will only run for CreateFragment to configure contact picture &
     * UI views visibility, consequently base64 encoded string will be put in sharedPref
     * temporarily
     */
    fun updateActivePhoto(
            imageUri: Uri,
            sharedPref: SharedPrefFunction,
            binding: FragmentCreateBinding) {

        binding.progressBar.visibility = View.VISIBLE
        binding.rectangleUnderBar.visibility = View.VISIBLE

        Thread {
            Log.d("BitmapFunction", "Updating active photo with Bitmap")
            val bitmapFunction = BitmapFunction(mContext)
            val bitmap = bitmapFunction.generateBitmap(imageUri)
            val encodedString = bitmapFunction.convertBitmapToBase64(bitmap)

            with(sharedPref.editor) {
                putString(SharedPrefFunction.TEMP_IMAGE_BASE_64, encodedString)
                apply()
            }

            binding.contactPicture.post {
                binding.contactPicture.setImageBitmap(bitmap)
                binding.cancelImage.visibility = View.VISIBLE
                binding.progressBar.visibility = View.INVISIBLE
                binding.rectangleUnderBar.visibility = View.INVISIBLE
            }
        }.start()
    }

    /**
     * Generating bitmap according to imageUri input
     */
    @Suppress("Deprecation")
    private fun generateBitmap(imageUri: Uri): Bitmap {
        Log.d("BitmapFunction", "ImageUri: $imageUri")
        imageUri.let {
            return if (Build.VERSION.SDK_INT > 28) {
                val source = ImageDecoder.createSource(
                        mContext.contentResolver,
                        imageUri
                )
                ImageDecoder.decodeBitmap(source)

            } else {
                MediaStore.Images.Media.getBitmap(
                        mContext.contentResolver,
                        imageUri
                )
            }
        }
    }

    /**
     * Converting bitmap to base64 string in JPEG format of 30% quality
     */
    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        Log.d("BitmapFunction", "Converting Bitmap")
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    /**
     * Converting base64 into bitmap
     * This function possibly only be used inside NotificationService
     */
    fun convertBase64ToBitmap(base64: String): Bitmap {
        val decodedString: ByteArray = Base64.decode(base64, Base64.NO_WRAP)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }

    /**
     * Cropping bitmap input into rounded bitmap
     */
    fun getCircleBitmap(bitmap: Bitmap): Bitmap? {
        val output = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)

        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color

        canvas.drawCircle(
            bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2,
            bitmap.width.toFloat() / 2, paint
        )
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    /**
     * Returning default bitmap base64 string. Default bitmap is from
     * "/drawable/default_profile_picture"
     */
    fun returnDefault(): String {
        /**
         *  This function will return default Bitmap Base64
         */

        val defaultImageUri = Uri.parse("android.resource://com.sawelo.infake/drawable/default_profile_picture")
        val bitmap = generateBitmap(defaultImageUri)
        return convertBitmapToBase64(bitmap)
    }
}