package com.lifeflow

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

internal class LoginPhotoStore(
    context: Context
) {
    private val appContext = context.applicationContext

    fun loginPhotoVersion(): Long {
        return loginPhotoFileOrNull()?.lastModified() ?: 0L
    }

    fun loginPhotoFileOrNull(): File? {
        val file = loginPhotoFile()
        return file.takeIf { it.exists() && it.length() > 0L }
    }

    fun saveCroppedLoginPhoto(
        sourceUri: Uri,
        transform: LoginPhotoCropTransform
    ): Long {
        val sourceBitmap = LoginPhotoBitmapDecoder.decodeNormalizedBitmap(
            context = appContext,
            sourceUri = sourceUri,
            maxSide = SOURCE_MAX_SIDE
        ) ?: return loginPhotoVersion()

        return try {
            val output = Bitmap.createBitmap(
                OUTPUT_SIZE,
                OUTPUT_SIZE,
                Bitmap.Config.ARGB_8888
            )

            val previewSize = transform.previewSizePx
                .coerceAtLeast(1)
                .toFloat()
            val outputScale = OUTPUT_SIZE.toFloat() / previewSize
            val baseScale = maxOf(
                previewSize / sourceBitmap.width.toFloat(),
                previewSize / sourceBitmap.height.toFloat()
            )
            val baseDx = (previewSize - sourceBitmap.width * baseScale) / 2f
            val baseDy = (previewSize - sourceBitmap.height * baseScale) / 2f
            val center = previewSize / 2f

            val matrix = Matrix().apply {
                postScale(baseScale, baseScale)
                postTranslate(baseDx, baseDy)
                postScale(transform.scale, transform.scale, center, center)
                postTranslate(transform.offsetX, transform.offsetY)
                postScale(outputScale, outputScale)
            }

            Canvas(output).apply {
                drawColor(Color.TRANSPARENT)
                drawBitmap(
                    sourceBitmap,
                    matrix,
                    Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                )
            }

            saveBitmap(output)
        } finally {
            sourceBitmap.recycle()
        }
    }

    private fun saveBitmap(bitmap: Bitmap): Long {
        val target = loginPhotoFile()
        val temp = File(target.parentFile, "$LOGIN_PHOTO_FILE.tmp")
        target.parentFile?.mkdirs()

        FileOutputStream(temp, false).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, output)
        }

        bitmap.recycle()

        if (target.exists()) {
            target.delete()
        }

        if (!temp.renameTo(target)) {
            temp.copyTo(target, overwrite = true)
            temp.delete()
        }

        val version = System.currentTimeMillis()
        target.setLastModified(version)
        return version
    }

    private fun loginPhotoFile(): File {
        return File(
            File(appContext.filesDir, LOGIN_PHOTO_DIR),
            LOGIN_PHOTO_FILE
        )
    }

    private companion object {
        private const val LOGIN_PHOTO_DIR = "login"
        private const val LOGIN_PHOTO_FILE = "login_photo"
        private const val SOURCE_MAX_SIDE = 4096
        private const val OUTPUT_SIZE = 512
        private const val PNG_QUALITY = 96
    }
}
