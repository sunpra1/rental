package softwarica.sunilprasai.cuid10748110.rental

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.InflateException
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isNotEmpty
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import softwarica.sunilprasai.cuid10748110.rental.ui.add_house.AddHouseFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.add_house_tenancy.AddHouseTenancyFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.add_house_tenancy_payment.AddHouseTenancyPaymentFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.add_house_tenancy_payment.EditHouseTenancyPaymentFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.add_house_tenancy_utility.AddHouseTenancyUtilityFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.add_house_to_let.AddHouseToLetFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.dashboard.DashboardFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.edit_house.EditHouseFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.edit_house_tenancy.EditHouseTenancyFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.edit_house_tenancy_utility.EditHouseTenancyUtilityFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.edit_house_to_let.EditHouseToLetFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.edit_profile.EditProfileFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.house_tenancies_history.HouseTenanciesHistoryFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.houses.HousesFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.select_location.SelectLocationFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.settings.SettingsFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.tenant_tenancies.TenantTenanciesFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.tenant_tenancies_history.TenantTenanciesHistoryFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.to_lets.ToLetsFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.view_house.ViewHouseFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.view_house_tenancy.ViewHouseTenancyFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.view_house_tenancy_history.ViewHouseTenancyHistoryFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.view_house_tenancy_history_payment.ViewHouseTenancyHistoryPaymentFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.view_house_tenancy_payment.ViewHouseTenancyPaymentFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.view_house_to_let.ViewHouseToLetFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.view_location.ViewLocationFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.view_tenant_tenancy.ViewTenantTenancyFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.view_tenant_tenancy_history.ViewTenantTenancyHistoryFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.view_tenant_tenancy_history_payment.ViewTenantTenancyHistoryPaymentFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.view_tenant_tenancy_house.ViewTenantTenancyHouseFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.view_tenant_tenancy_payment.ViewTenantTenancyPaymentFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.view_to_lets_to_let.ViewToLetsToLetFragment
import softwarica.sunilprasai.cuid10748110.rental.utils.AppSensors
import softwarica.sunilprasai.cuid10748110.rental.utils.AppState
import softwarica.sunilprasai.cuid10748110.rental.utils.UserToken
import java.util.*

private const val TAG = "MainActivity"
private const val SAVE_TOOLBAR_TITLE = "SAVE_TOOLBAR_TITLE"

class MainActivity : AppCompatActivity(), FragmentManager.OnBackStackChangedListener,
    BottomNavigationView.OnNavigationItemSelectedListener, AppSensors.SensorEventListener {
    private lateinit var mBottomNavigationView: BottomNavigationView
    private lateinit var mActionBarMenu: Menu

    private lateinit var mFragmentHolderOne: FragmentContainerView
    private lateinit var mFragmentHolderTwo: FragmentContainerView
    private var mIsBiggerScreen = false
    private lateinit var mAppSensors: AppSensors
    private lateinit var mViewModel: AuthViewModel
    private var mShouldFetchUserDetails: Boolean = false

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        supportActionBar!!.title = savedInstanceState.getString(SAVE_TOOLBAR_TITLE)
        if (mIsBiggerScreen)
            makeBothFragmentHolderVisible()
        else {
            if (mFragmentHolderTwo.isNotEmpty())
                makeFragmentHolderTwoVisible()
            else
                makeFragmentHolderOneVisible()
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        mAppSensors = AppSensors.getInstance(this)
        mAppSensors.registerListener(this)

        mIsBiggerScreen =
            resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE || resources.getBoolean(
                R.bool.isTablet
            )
        supportFragmentManager.addOnBackStackChangedListener(this)
        mBottomNavigationView = findViewById(R.id.nav_view)
        mBottomNavigationView.setOnNavigationItemSelectedListener(this)

        mFragmentHolderOne = findViewById(R.id.fragment_holder_one)
        mFragmentHolderTwo = findViewById(R.id.fragment_holder_two)

        mViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        mViewModel.getLoggedInUser().observe(this) {
            if (it == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                AppState.loggedInUser = it
            }
        }

        if (savedInstanceState == null)
            handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        Log.d(TAG, "handleIntent: ${intent.action}")

        val fragmentTransaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
        fragmentTransaction.addToBackStack(HousesFragment::class.java.simpleName)
            .replace(mFragmentHolderOne.id, HousesFragment()).commit()

    }

    override fun onResume() {
        super.onResume()
        mAppSensors.registerSensors()
        if (mShouldFetchUserDetails) {
            if (UserToken.getInstance(
                    this
                ).token == null
            ) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                if ((mViewModel.getLoggedInUserValue() == null || AppState.loggedInUser == null)) {
                    mViewModel.fetchLoggedInUserDetails(this)
                }
            }
        }
        mShouldFetchUserDetails = true
    }

    override fun onStop() {
        super.onStop()
        mAppSensors.unRegisterSensors()
    }

    override fun onBackStackChanged() {
        val backStackSize = supportFragmentManager.backStackEntryCount
        Log.d(TAG, "onBackStackChanged: backStackCount -> $backStackSize")
        if (backStackSize > 0) {
            Log.d(
                TAG,
                "onBackStackChanged: ${supportFragmentManager.getBackStackEntryAt(backStackSize - 1).name}"
            )
            when (supportFragmentManager.getBackStackEntryAt(backStackSize - 1).name) {
                HousesFragment::class.java.simpleName -> {
                    supportActionBar?.title = "HOUSE"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderOneVisible()
                }

                DashboardFragment::class.java.simpleName -> {
                    supportActionBar?.title =
                        AppState.loggedInUser!!.fullName!!.toUpperCase(Locale.ROOT)
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderOneVisible()
                }

                AddHouseFragment::class.java.simpleName -> {
                    supportActionBar?.title = "ADD HOUSE"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                SelectLocationFragment::class.java.simpleName -> {
                    supportActionBar?.title = "SELECT LOCATION"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                EditHouseFragment::class.java.simpleName -> {
                    supportActionBar?.title = "EDIT HOUSE"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                ViewHouseFragment::class.java.simpleName -> {
                    supportActionBar?.title = "VIEW HOUSE"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                ViewLocationFragment::class.java.simpleName -> {
                    supportActionBar?.title = "HOUSE ON MAP"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                AddHouseTenancyFragment::class.java.simpleName -> {
                    supportActionBar?.title = "ADD TENANT"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                EditHouseTenancyFragment::class.java.simpleName -> {
                    supportActionBar?.title = "EDIT TENANCY"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                ViewHouseTenancyFragment::class.java.simpleName -> {
                    supportActionBar?.title = "VIEW TENANCY"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                AddHouseTenancyUtilityFragment::class.java.simpleName -> {
                    supportActionBar?.title = "ADD UTILITY"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                AddHouseTenancyPaymentFragment::class.java.simpleName -> {
                    supportActionBar?.title = "ADD PAYMENT"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                EditHouseTenancyUtilityFragment::class.java.simpleName -> {
                    supportActionBar?.title = "EDIT UTILITY"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                EditHouseTenancyPaymentFragment::class.java.simpleName -> {
                    supportActionBar?.title = "EDIT PAYMENT"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                ViewHouseTenancyPaymentFragment::class.java.simpleName -> {
                    supportActionBar?.title = "VIEW PAYMENT"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                AddHouseToLetFragment::class.java.simpleName -> {
                    supportActionBar?.title = "ADD TO-LET"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                EditHouseToLetFragment::class.java.simpleName -> {
                    supportActionBar?.title = "EDIT TO-LET"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                ViewHouseToLetFragment::class.java.simpleName -> {
                    supportActionBar?.title = "VIEW TO-LET"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                TenantTenanciesFragment::class.java.simpleName -> {
                    supportActionBar?.title = "YOUR TENANCIES"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderOneVisible()
                }

                ViewTenantTenancyFragment::class.java.simpleName -> {
                    supportActionBar?.title = "VIEW YOUR TENANCY"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                ViewTenantTenancyPaymentFragment::class.java.simpleName -> {
                    supportActionBar?.title = "VIEW YOUR PAYMENT"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                ViewTenantTenancyHouseFragment::class.java.simpleName -> {
                    supportActionBar?.title = "VIEW HOUSE"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                HouseTenanciesHistoryFragment::class.java.simpleName -> {
                    supportActionBar?.title = "HOUSE TENANCIES HISTORY"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                TenantTenanciesHistoryFragment::class.java.simpleName -> {
                    supportActionBar?.title = "YOUR TENANCIES HISTORY"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderOneVisible()
                }

                ViewTenantTenancyHistoryFragment::class.java.simpleName -> {
                    supportActionBar?.title = "VIEW TENANCY HISTORY"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                ViewTenantTenancyHistoryPaymentFragment::class.java.simpleName -> {
                    supportActionBar?.title = "VIEW PAYMENT HISTORY"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                ViewHouseTenancyHistoryFragment::class.java.simpleName -> {
                    supportActionBar?.title = "VIEW TENANCY HISTORY"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                ViewHouseTenancyHistoryPaymentFragment::class.java.simpleName -> {
                    supportActionBar?.title = "VIEW PAYMENT HISTORY"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                ToLetsFragment::class.java.simpleName -> {
                    supportActionBar?.title = "TO-LETS"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderOneVisible()
                }

                ViewToLetsToLetFragment::class.java.simpleName -> {
                    supportActionBar?.title = "VIEW TO-LET"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                EditProfileFragment::class.java.simpleName -> {
                    supportActionBar?.title = "EDIT PROFILE"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }

                SettingsFragment::class.java.simpleName -> {
                    supportActionBar?.title = "SETTINGS"
                    if (mIsBiggerScreen) makeBothFragmentHolderVisible() else makeFragmentHolderTwoVisible()
                }
            }
            invalidateOptionsMenu()
        } else {
            finish()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onNavigationItemSelected: ${item.title}")
        val fragmentTransaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
        supportFragmentManager.removeOnBackStackChangedListener(this)
        for (i in 0 until supportFragmentManager.backStackEntryCount) {
            supportFragmentManager.popBackStack()
        }
        supportFragmentManager.addOnBackStackChangedListener(this)
        when (item.itemId) {
            R.id.navigation_home -> {
                fragmentTransaction.addToBackStack(HousesFragment::class.java.simpleName)
                    .replace(mFragmentHolderOne.id, HousesFragment()).commit()
            }

            R.id.navigation_dashboard -> {
                fragmentTransaction.addToBackStack(DashboardFragment::class.java.simpleName)
                    .replace(mFragmentHolderOne.id, DashboardFragment()).commit()
            }

            R.id.navigation_tenancies -> {
                fragmentTransaction.addToBackStack(TenantTenanciesFragment::class.java.simpleName)
                    .replace(mFragmentHolderOne.id, TenantTenanciesFragment()).commit()
            }

            R.id.navigation_to_lets -> {
                fragmentTransaction.addToBackStack(ToLetsFragment::class.java.simpleName)
                    .replace(mFragmentHolderOne.id, ToLetsFragment()).commit()
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.d(TAG, "onCreateOptionsMenu: ")
        return try {
            menuInflater.inflate(R.menu.main, menu)
            true
        } catch (e: InflateException) {
            Log.e(TAG, "onCreateOptionsMenu: ${e.localizedMessage}")
            super.onCreateOptionsMenu(menu)
        } finally {
            mActionBarMenu = menu!!
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                Log.d(TAG, "onOptionsItemSelected: back button selected")
                supportFragmentManager.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            AlertDialog.Builder(this).apply {
                setIcon(R.drawable.danger_red)
                setTitle("Exit Application")
                setMessage("Please confirm to exit the application")
                setPositiveButton(R.string.ok) { _, _ ->
                    super.onBackPressed()
                }
                setNegativeButton(R.string.cancel) { _, _ ->
                    //Do nothing
                }
                show()
            }
            Log.d(TAG, "onBackPressed: exit")
        } else {
            super.onBackPressed()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return menu?.let {
            val backStackSize = supportFragmentManager.backStackEntryCount
            if (backStackSize > 0) {
                Log.d(TAG, "onPrepareOptionsMenu:")
                when (supportFragmentManager.getBackStackEntryAt(backStackSize - 1).name) {
                    HousesFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(false)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, true)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_home).isChecked = true
                        true
                    }

                    DashboardFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(false)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_dashboard).isChecked =
                            true
                        true
                    }

                    TenantTenanciesFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(false)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, true)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_tenancies).isChecked =
                            true
                        true
                    }

                    ToLetsFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(false)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, true)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_to_lets).isChecked =
                            true
                        true
                    }

                    AddHouseFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_home).isChecked = true
                        true
                    }

                    SelectLocationFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, true)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_home).isChecked = true
                        true
                    }

                    EditHouseFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_home).isChecked = true
                        true
                    }

                    ViewHouseFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, true)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_home).isChecked = true
                        true
                    }

                    ViewLocationFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, true)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        true
                    }

                    AddHouseTenancyFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_home).isChecked = true
                        true
                    }

                    EditHouseTenancyFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_home).isChecked = true
                        true
                    }

                    ViewHouseTenancyFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, true)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_home).isChecked = true
                        true
                    }

                    AddHouseTenancyUtilityFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_home).isChecked = true
                        true
                    }

                    AddHouseTenancyPaymentFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_home).isChecked = true
                        true
                    }

                    EditHouseTenancyUtilityFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_home).isChecked = true
                        true
                    }

                    EditHouseTenancyPaymentFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_home).isChecked = true
                        true
                    }

                    ViewHouseTenancyPaymentFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_home).isChecked = true
                        true
                    }

                    AddHouseToLetFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_home).isChecked = true
                        true
                    }

                    EditHouseToLetFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_home).isChecked = true
                        true
                    }

                    ViewHouseToLetFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_home).isChecked = true
                        true
                    }

                    ViewTenantTenancyFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_tenancies).isChecked =
                            true
                        true
                    }

                    ViewTenantTenancyPaymentFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_tenancies).isChecked =
                            true
                        true
                    }

                    ViewTenantTenancyHouseFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, true)
                        mBottomNavigationView.menu.findItem(R.id.navigation_tenancies).isChecked =
                            true
                        true
                    }

                    HouseTenanciesHistoryFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_home).isChecked = false
                        true
                    }

                    TenantTenanciesHistoryFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_tenancies).isChecked =
                            false
                        true
                    }

                    ViewTenantTenancyHistoryFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_tenancies).isChecked =
                            false
                        true
                    }

                    ViewTenantTenancyHistoryPaymentFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_tenancies).isChecked =
                            false
                        true
                    }

                    ViewHouseTenancyHistoryFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_tenancies).isChecked =
                            false
                        true
                    }

                    ViewHouseTenancyHistoryPaymentFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_tenancies).isChecked =
                            false
                        true
                    }

                    ViewToLetsToLetFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_to_lets).isChecked =
                            false
                        true
                    }

                    EditProfileFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_dashboard).isChecked =
                            false
                        true
                    }

                    SettingsFragment::class.java.simpleName -> {
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        mActionBarMenu.setGroupVisible(R.id.houseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.selectLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewLocationFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.viewHouseTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.toLetsFragmentMenu, false)
                        mActionBarMenu.setGroupVisible(R.id.tenantTenancyHouseFragmentMenu, false)
                        mBottomNavigationView.menu.findItem(R.id.navigation_dashboard).isChecked =
                            false
                        true
                    }

                    else -> null
                }
            } else null
        } ?: super.onPrepareOptionsMenu(menu)
    }

    private fun makeFragmentHolderOneVisible() {
        mFragmentHolderOne.visibility = View.VISIBLE
        mFragmentHolderTwo.visibility = View.GONE
    }

    private fun makeFragmentHolderTwoVisible() {
        mFragmentHolderOne.visibility = View.GONE
        mFragmentHolderTwo.visibility = View.VISIBLE
    }

    private fun makeBothFragmentHolderVisible() {
        mFragmentHolderOne.visibility = View.VISIBLE
        mFragmentHolderTwo.visibility = View.VISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putCharSequence(SAVE_TOOLBAR_TITLE, supportActionBar!!.title)
        super.onSaveInstanceState(outState)
    }

    override fun onDeviceShake() {
        supportFragmentManager.popBackStack()
    }
}