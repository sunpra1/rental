package softwarica.sunilprasai.cuid10748110.rental.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.security.InvalidParameterException

object FileHandler {
    private const val TAG = "FileHandler"
    fun getFileSize(uri: Uri, context: Context): Double {
        var dataSize: Long = 0
        try {
            val scheme = uri.scheme
            scheme?.let {
                if (it == ContentResolver.SCHEME_CONTENT) {
                    val fileInputStream = context.contentResolver.openInputStream(uri)
                    if (fileInputStream != null) {
                        dataSize = fileInputStream.available().toLong()
                    }
                } else if (it == ContentResolver.SCHEME_FILE) {
                    val path = uri.path
                    if (path != null) {
                        val f = File(path)
                        dataSize = f.length()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getFileSize() failed: ${e.localizedMessage}")
            e.printStackTrace()
        }

        return if (dataSize > 0) dataSize / (1024.0 * 1024.0) else dataSize.toDouble()
    }

    fun getFile(uri: Uri, context: Context): File {
        Log.d(TAG, "getFile: ${uri.scheme}")
        val columnName = "_data"
        return when (uri.scheme) {
            ContentResolver.SCHEME_FILE -> {
                File(uri.path!!)
            }
            ContentResolver.SCHEME_CONTENT -> {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor!!.moveToFirst()
                val s = cursor.getString(cursor.getColumnIndex(columnName))
                File(s)
            }
            else -> throw InvalidParameterException("Provided Uri is invalid")
        }
    }
}