package softwarica.sunilprasai.cuid10748110.rental

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import softwarica.sunilprasai.cuid10748110.rental.utils.UserToken

class SplashScreen : AppCompatActivity() {
    private lateinit var mAppName: View
    private lateinit var mAppSlogan: View
    private lateinit var mViewModel: AuthViewModel
    private var mShouldFetchUserDetails: Boolean = false
    private lateinit var enterFromRight: Animation
    private lateinit var slideDown: Animation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        mAppName = findViewById(R.id.appName)
        mAppName.visibility = View.GONE
        mAppSlogan = findViewById(R.id.appSlogan)
        mAppSlogan.visibility = View.GONE

        enterFromRight =
            AnimationUtils.loadAnimation(applicationContext, R.anim.enter_from_right_late)
        slideDown = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_down)

        mViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        mViewModel.getLoggedInUser().observe(this) {
            Handler().postDelayed({
                if (it != null) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }, 1500)
        }
    }

    override fun onResume() {
        super.onResume()
        if (mShouldFetchUserDetails && mViewModel.getLoggedInUserValue() == null && UserToken.getInstance(
                this
            ).token != null
        ) {
            mViewModel.fetchLoggedInUserDetails(this)
        }
        mShouldFetchUserDetails = true
    }

    override fun onStart() {
        super.onStart()
        Handler().postDelayed({ animateAll() }, 250)
    }

    private fun animateAll() {
        mAppName.visibility = View.VISIBLE
        mAppName.startAnimation(enterFromRight)

        mAppSlogan.visibility = View.VISIBLE
        mAppSlogan.startAnimation(slideDown)
    }
}