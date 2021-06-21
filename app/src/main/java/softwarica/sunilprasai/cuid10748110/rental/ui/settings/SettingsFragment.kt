package softwarica.sunilprasai.cuid10748110.rental.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.utils.AppSensors
import softwarica.sunilprasai.cuid10748110.rental.utils.UserSetting

private const val TAG = "SettingsFragment"

class SettingsFragment : Fragment(), CompoundButton.OnCheckedChangeListener {
    private var mUserSetting: UserSetting? = null
    private var mAppSensors: AppSensors? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        mUserSetting = UserSetting.getInstance(requireContext())
        mAppSensors = AppSensors.getInstance(requireContext())
        val useVibrationSwitch: SwitchMaterial = root.findViewById(R.id.useVibrationSwitch)
        useVibrationSwitch.setOnCheckedChangeListener(this)
        useVibrationSwitch.isChecked = mUserSetting!!.shouldUseInAppVibration
        val useShakeDeviceSwitch: SwitchMaterial = root.findViewById(R.id.useShakeDeviceSwitch)
        useShakeDeviceSwitch.setOnCheckedChangeListener(this)
        useShakeDeviceSwitch.isChecked = mUserSetting!!.shouldUseShakeDevice
        val useProximitySwitch: SwitchMaterial = root.findViewById(R.id.useProximitySwitch)
        useProximitySwitch.setOnCheckedChangeListener(this)
        useProximitySwitch.isChecked = mUserSetting!!.shouldUseProximitySensor
        return root
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        Log.d(TAG, "onCheckedChanged: of button id -> " + buttonView.id)
        when (buttonView.id) {
            R.id.useVibrationSwitch -> {
                mUserSetting!!.shouldUseInAppVibration = isChecked
            }
            R.id.useShakeDeviceSwitch -> {
                mUserSetting!!.shouldUseShakeDevice = isChecked
            }
            R.id.useProximitySwitch -> {
                mUserSetting!!.shouldUseProximitySensor = isChecked
            }
        }
        mAppSensors!!.registerSensors()
    }

}