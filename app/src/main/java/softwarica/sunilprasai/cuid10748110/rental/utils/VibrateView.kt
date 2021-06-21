package softwarica.sunilprasai.cuid10748110.rental.utils

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.animation.AnimationUtils

class VibrateView {
    companion object {
        fun vibrate(context: Context, anim: Int, view: View) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (UserSetting.getInstance(context).shouldUseInAppVibration) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        350,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            }
            val animation = AnimationUtils.loadAnimation(context, anim)
            view.startAnimation(animation)
        }
    }
}