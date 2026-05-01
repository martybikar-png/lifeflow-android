package com.lifeflow

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import kotlin.math.max

internal object LoginPhotoBitmapDecoder {
    fun decodeNormalizedBitmap(
        context: Context,
        sourceUri: Uri,
        maxSide: Int
    ): Bitmap? {
        val appContext = context.applicationContext

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            decodeWithImageDecoder(
                context = appContext,
                sourceUri = sourceUri,
                maxSide = maxSide
            )
        } else {
            decodeWithBitmapFactory(
                context = appContext,
                sourceUri = sourceUri,
                maxSide = maxSide
            )
        }
    }

    private fun decodeWithImageDecoder(
        context: Context,
        sourceUri: Uri,
        maxSide: Int
    ): Bitmap? {
        return runCatching {
            val source = ImageDecoder.createSource(
                context.contentResolver,
                sourceUri
            )

            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.setTargetSampleSize(
                    sampleSizeFor(
                        width = info.size.width,
                        height = info.size.height,
                        maxSide = maxSide
                    )
                )
            }
        }.getOrNull()
    }

    private fun decodeWithBitmapFactory(
        context: Context,
        sourceUri: Uri,
        maxSide: Int
    ): Bitmap? {
        val orientation = readExifOrientation(
            context = context,
            sourceUri = sourceUri
        )

        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        } ?: return null

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSizeFor(
                width = bounds.outWidth,
                height = bounds.outHeight,
                maxSide = maxSide
            )
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val decoded = context.contentResolver.openInputStream(sourceUri)?.use { input ->
            BitmapFactory.decodeStream(input, null, decodeOptions)
        } ?: return null

        return applyExifOrientation(
            bitmap = decoded,
            orientation = orientation
        )
    }

    private fun readExifOrientation(
        context: Context,
        sourceUri: Uri
    ): Int {
        return runCatching {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
    }

    private fun sampleSizeFor(
        width: Int,
        height: Int,
        maxSide: Int
    ): Int {
        var sampleSize = 1
        val safeMaxSide = maxSide.coerceAtLeast(1)

        while (max(width / sampleSize, height / sampleSize) > safeMaxSide) {
            sampleSize *= 2
        }

        return sampleSize.coerceAtLeast(1)
    }

    private fun applyExifOrientation(
        bitmap: Bitmap,
        orientation: Int
    ): Bitmap {
        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(270f)
            else -> return bitmap
        }

        return runCatching {
            Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            ).also { rotated ->
                if (rotated !== bitmap) {
                    bitmap.recycle()
                }
            }
        }.getOrElse {
            bitmap
        }
    }
}
