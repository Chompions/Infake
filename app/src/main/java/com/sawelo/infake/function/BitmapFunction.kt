package com.sawelo.infake.function

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import java.io.ByteArrayOutputStream

class BitmapFunction(context: Context) {
    private val mContext: Context = context

    fun updateActivePhoto(
        imageUri: Uri,
        sharedPref: SharedPrefFunction,
        imageView: ImageView) {

        val bitmapFunction = BitmapFunction(mContext)
        val bitmap = bitmapFunction.generateBitmap(imageUri)
        val encodedString = bitmapFunction.convertBitmap(bitmap)

        with(sharedPref.editor) {
            putString(SharedPrefFunction.IMAGE_BASE_64, encodedString)
            apply()
        }

        imageView.setImageBitmap(bitmap)
    }

    @Suppress("Deprecation")
    fun generateBitmap(imageUri: Uri): Bitmap {
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

    fun convertBitmap(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 30, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun returnDefault(): String {
        /**
         *  This function will only run in SharedPrefFunction
         */

        val defaultImageUri = Uri.parse("android.resource://com.sawelo.infake/drawable/default_profile_picture")
        val bitmap = generateBitmap(defaultImageUri)
        return convertBitmap(bitmap)
    }
}