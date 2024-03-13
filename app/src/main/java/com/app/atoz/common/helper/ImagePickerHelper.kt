package com.app.atoz.common.helper

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.app.atoz.BuildConfig
import com.app.atoz.R
import com.app.atoz.common.extentions.bitmapFromUri
import com.app.atoz.common.extentions.toPx
import com.app.atoz.utils.FileUtils
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.AccessController
import java.text.SimpleDateFormat
import java.util.*

class ImagePickerHelper(private val activity: AppCompatActivity, private val mListener: ImagePickerListener) {

    companion object {
        const val REQUEST_CAMERA = 0
        const val SELECT_FILE = 1
        const val PERMISSIONS_TAKE_IMAGE = 2004
        const val PERMISSIONS_PICK_IMAGE = 2005
    }

    var mFile: File? = null
    var mFileUri: Uri? = null
    var mIsCropInCircleShape: Boolean = false

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(imageFileName, ".png", storageDir)
    }

    fun selectOptionToLoadImage() {
        val items: Array<String> = activity.resources.getStringArray(R.array.dialog_items_choose_profile_image)
        try {
            val builder = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.AlertDialogTheme))
            builder.setTitle(activity.getString(R.string.dialog_title_profile_photo))
            builder.setItems(items) { dialog, item ->
                when {
                    items[item].equals(activity.resources.getString(R.string.text_camera), ignoreCase = true) -> {
                        dialog.dismiss()
                        actionTakeImage()
                    }
                    items[item].equals(activity.resources.getString(R.string.text_gallery), ignoreCase = true) -> {
                        dialog.dismiss()
                        actionPickImage()
                    }
                    items[item].equals(
                        activity.resources.getString(R.string.text_cancel),
                        ignoreCase = true
                    ) -> dialog.dismiss()
                }
            }
            builder.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun actionTakeImage() {
        if (hasRequiredPermissionForTakeImage()) {
            fromCamera()
        } else {
            requestPermissionForTakeImage()
        }
    }

    fun actionPickImage() {
        if (hasRequiredPermissionForPickImage()) {
            fromGallery()
        } else {
            requestPermissionForPickImage()
        }
    }

    private fun hasRequiredPermissionForTakeImage(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionForTakeImage() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSIONS_TAKE_IMAGE
        )
    }

    private fun requestPermissionForPickImage() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            PERMISSIONS_PICK_IMAGE
        )
    }

    private fun hasRequiredPermissionForPickImage(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun fromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(activity.packageManager) != null) {
            try {
                mFile = createImageFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (mFile != null) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val fileUri = FileProvider.getUriForFile(
                    activity, BuildConfig.APPLICATION_ID + ".provider",
                    mFile!!
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
                activity.startActivityForResult(intent, REQUEST_CAMERA)
            }
        }
    }

    private fun fromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        activity.startActivityForResult(Intent.createChooser(intent, "Select Image"), SELECT_FILE)
    }

    private fun cropImage(uri: Uri?, isForCircleShape: Boolean) {
        val cropActivity = CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAllowFlipping(false)
            .setBorderLineColor(Color.argb(255, 17, 131, 200))
            .setBorderCornerColor(Color.argb(255, 17, 131, 200))
            .setMinCropResultSize(300.toPx, 300.toPx)
            .setCropMenuCropButtonTitle("Done")

        if (isForCircleShape) {
            cropActivity.setCropShape(CropImageView.CropShape.OVAL)
                .setFixAspectRatio(true)
        }

        cropActivity.start(activity)
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_TAKE_IMAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    fromCamera()
                }
            }
            PERMISSIONS_PICK_IMAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    fromGallery()
                }
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CAMERA -> {
                if (resultCode == Activity.RESULT_OK) {
                    mFile?.let {
                        mFileUri = Uri.fromFile(mFile)
                        try {
                            if (mFileUri != null) {
                                cropImage(mFileUri, mIsCropInCircleShape)
                            } else {
                                mListener.onImageLoad()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            mListener.onImageLoad()
                        }
                    }
                }
            }
            SELECT_FILE -> {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        activity.contentResolver.notifyChange(data?.data!!, null)
                        mFileUri = data.data
                        if (mFileUri != null) {
                            if (mFileUri.toString().contains("content://com.google.android.apps.photos")) {
                                if (AccessController.getContext() != null) {
                                    mListener.showLoadingIndicator(true)
                                }
                                Observable.just(mFileUri)
                                    .subscribeOn(Schedulers.io())
                                    .map {
                                        saveImageInCache()
                                    }
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeBy(
                                        onNext = {
                                            if (AccessController.getContext() != null && !activity.isFinishing) {
                                                mListener.showLoadingIndicator(false)
                                                cropImage(it, mIsCropInCircleShape)
                                            }
                                        },
                                        onError = {
                                            it.printStackTrace()
                                            mListener.showLoadingIndicator(false)
                                            mListener.displayMessage(activity.getString(R.string.text_error_invalid_google_pic))
                                        }
                                    ).addTo(mListener.getDisposable())

                            } else {
                                mFile = FileUtils.getFile(activity, mFileUri)
                                cropImage(mFileUri, mIsCropInCircleShape)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        mListener.showLoadingIndicator(false)
                        mListener.displayMessage(activity.getString(R.string.text_error_invalid_google_pic))
                    }
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result: CropImage.ActivityResult? = CropImage.getActivityResult(data)
                when {
                    result != null && resultCode == Activity.RESULT_OK -> {
                        mFileUri = result.uri
                        mFile = File(mFileUri?.path)
                        if (mFile != null) {
                            mListener.onImageLoad()
                        }
                    }
                    resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                        Timber.d("${result?.error}")
                        mListener.onImageLoad()
                    }
                }
            }
        }
    }

    private fun saveImageInCache(): Uri {
        mFile = File(
            activity.cacheDir,
            "File${System.currentTimeMillis()}"
        )
        mFile?.let {
            it.createNewFile()
            val bos = ByteArrayOutputStream()
            mFileUri?.let { fileUri ->
                activity.bitmapFromUri(fileUri)?.compress(Bitmap.CompressFormat.PNG, 0, bos)
            }
            val bitmapData = bos.toByteArray()
            val fos = FileOutputStream(mFile)
            fos.write(bitmapData)
            fos.flush()
            fos.close()

            mFileUri = Uri.fromFile(mFile)
        }
        return mFileUri!!
    }

    interface ImagePickerListener {
        fun onImageLoad()

        fun showLoadingIndicator(isShow: Boolean)

        fun displayMessage(message: String)

        fun getDisposable(): CompositeDisposable
    }
}
