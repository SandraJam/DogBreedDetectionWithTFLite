package com.dupre.sandra.dogbreeddetectorwithtflite

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class DogDetector(private val context: Context) {

    var view: DogView? = null
    private val labels = mutableListOf<String>()
    private lateinit var interpreter: Interpreter

    companion object {
        private const val IMG_SIZE = 224
        private const val MODEL_NAME = "dog-breed-detector.tflite"
        private const val MEAN = 128
        private const val STD = 128.0f
    }

    init {
        initializeLabels()
        initializeInterpreter()
    }

    fun recognizeDog(bitmap: Bitmap) {
        val imgData = fromBitmapToByteBuffer(bitmap)
        val outputs = Array(1, { FloatArray(labels.size) })

        interpreter.run(imgData, outputs)

        val dogBreed = labels
            .mapIndexed { index, label ->
                Pair(label, outputs[0][index])
            }
            .sortedByDescending { it.second }
            .first()

        view?.displayDogBreed(dogBreed.first, dogBreed.second * 100)
    }

    private fun initializeLabels() {
        labels.addAll(context.assets.open("labels.txt").bufferedReader().readLines())
    }

    private fun initializeInterpreter() {
        interpreter = Interpreter(
            context.assets.openFd(MODEL_NAME).let {
                FileInputStream(it.fileDescriptor).channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    it.startOffset,
                    it.declaredLength
                )
            }
        )
    }

    private fun fromBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val imgData = ByteBuffer.allocateDirect(4 * IMG_SIZE * IMG_SIZE * 3).apply {
            order(ByteOrder.nativeOrder())
            rewind()
        }

        val pixels = IntArray(IMG_SIZE * IMG_SIZE)
        Bitmap.createScaledBitmap(bitmap, IMG_SIZE, IMG_SIZE, false).apply {
            getPixels(pixels, 0, width, 0, 0, width, height)
        }

        pixels.forEach {
            imgData.putFloat(((it shr 16 and 0xFF) - MEAN) / STD)
            imgData.putFloat(((it shr 8 and 0xFF) - MEAN) / STD)
            imgData.putFloat(((it and 0xFF) - MEAN) / STD)
        }

        return imgData
    }
}