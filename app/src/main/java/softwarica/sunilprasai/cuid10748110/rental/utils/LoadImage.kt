package softwarica.sunilprasai.cuid10748110.rental.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Base64
import android.util.Log
import java.io.IOException

private const val TAG = "ImageLoader"

class LoadImage(private val callBack: ImageLoader) : AsyncTask<String, Unit, Bitmap>() {

    override fun onPostExecute(result: Bitmap?) {
        callBack.onImageLoaded(result)
    }

    override fun doInBackground(vararg params: String?): Bitmap? {
        var imageBitmap: Bitmap? = null
        try {
            val imageAsBytes = Base64.decode(params[0], Base64.DEFAULT)
            imageBitmap = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "Failed to load image: ${e.localizedMessage}")
        }
        return imageBitmap
    }

    interface ImageLoader {
        fun onImageLoaded(imageBitmap: Bitmap?)
    }
}