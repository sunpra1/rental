package softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import softwarica.sunilprasai.cuid10748110.rental.model.Payment
import softwarica.sunilprasai.cuid10748110.rental.model.Tenancy

class TenantTenanciesHistoryViewModel : ViewModel() {
    private val tenantTenanciesHistory = MutableLiveData<ArrayList<Tenancy>>().apply {
        value = ArrayList()
    }

    fun getTenantTenanciesHistory(): LiveData<ArrayList<Tenancy>> {
        return tenantTenanciesHistory
    }

    private fun getTenantTenanciesHistoryValue(): ArrayList<Tenancy> {
        return tenantTenanciesHistory.value!!
    }

    fun setTenantTenanciesHistory(tenantTenanciesHistory: ArrayList<Tenancy>) {
        this.tenantTenanciesHistory.value = tenantTenanciesHistory
    }

    fun getTenantTenancyHistory(tenancyPosition: Int): Tenancy {
        return getTenantTenanciesHistoryValue()[tenancyPosition]
    }

    fun getTenantTenancyPaymentHistory(tenancyPosition: Int, paymentPosition: Int): Payment {
        return getTenantTenanciesHistoryValue()[tenancyPosition].payments!![paymentPosition]
    }

    fun deleteTenantTenancyHistory(tenancyPosition: Int) {
        getTenantTenanciesHistoryValue().removeAt(tenancyPosition)
        tenantTenanciesHistory.value = getTenantTenanciesHistoryValue()
    }

    fun addTenantTenancyHistory(tenancyPosition: Int, tenancy: Tenancy) {
        getTenantTenanciesHistoryValue().add(tenancyPosition, tenancy)
        tenantTenanciesHistory.value = getTenantTenanciesHistoryValue()
    }

    fun replaceTenantTenancyHistory(tenancyPosition: Int, tenancy: Tenancy) {
        getTenantTenanciesHistoryValue()[tenancyPosition] = tenancy
        tenantTenanciesHistory.value = getTenantTenanciesHistoryValue()
    }
}