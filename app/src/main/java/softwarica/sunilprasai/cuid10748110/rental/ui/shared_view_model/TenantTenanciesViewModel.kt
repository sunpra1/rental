package softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import softwarica.sunilprasai.cuid10748110.rental.model.Payment
import softwarica.sunilprasai.cuid10748110.rental.model.Review
import softwarica.sunilprasai.cuid10748110.rental.model.Tenancy
import softwarica.sunilprasai.cuid10748110.rental.model.Utility
import softwarica.sunilprasai.cuid10748110.rental.utils.AppState

class TenantTenanciesViewModel : ViewModel() {
    private val tenantTenancies = MutableLiveData<ArrayList<Tenancy>>().apply {
        value = ArrayList()
    }

    fun getTenantTenancies(): LiveData<ArrayList<Tenancy>> {
        return tenantTenancies
    }

    fun getTenantTenanciesValue(): ArrayList<Tenancy> {
        return tenantTenancies.value!!
    }

    fun setTenantTenancies(tenantTenancies: ArrayList<Tenancy>) {
        this.tenantTenancies.value = tenantTenancies
    }

    fun getTenantTenancy(tenancyPosition: Int): Tenancy {
        return getTenantTenanciesValue()[tenancyPosition]
    }

    fun getTenantTenancyPayment(tenancyPosition: Int, paymentPosition: Int): Payment {
        return getTenantTenancy(tenancyPosition).payments!![paymentPosition]
    }

    fun replaceTenantTenancyPayment(tenancyPosition: Int, paymentPosition: Int, payment: Payment) {
        getTenantTenancy(tenancyPosition).payments!![paymentPosition] = payment
        tenantTenancies.value = getTenantTenanciesValue()
    }

    fun getTenantTenancyUtility(tenancyPosition: Int, utilityPosition: Int): Utility {
        return getTenantTenancy(tenancyPosition).utilities!![utilityPosition]
    }

    fun replaceTenantTenancyUtility(tenancyPosition: Int, utilityPosition: Int, utility: Utility) {
        getTenantTenancy(tenancyPosition).utilities!![utilityPosition] = utility
        tenantTenancies.value = getTenantTenanciesValue()
    }

    fun addTenantTenancy(tenancyPosition: Int, tenancy: Tenancy) {
        getTenantTenanciesValue().add(tenancyPosition, tenancy)
        tenantTenancies.value = getTenantTenanciesValue()
    }

    fun deleteTenantTenancy(tenancyPosition: Int) {
        getTenantTenanciesValue().removeAt(tenancyPosition)
        tenantTenancies.value = getTenantTenanciesValue()
    }

    fun replaceTenantTenancy(tenancyPosition: Int, tenancy: Tenancy) {
        getTenantTenanciesValue()[tenancyPosition] = tenancy
        tenantTenancies.value = getTenantTenanciesValue()
    }

    fun addTenantHouseOwnerReview(tenancyPosition: Int, review: Review) {
        val reviewIndex =
            getTenantTenancy(tenancyPosition).house!!.owner!!.reviews!!.indexOfFirst { it.user!!.id == AppState.loggedInUser!!.id }
        if (reviewIndex > -1)
            getTenantTenancy(tenancyPosition).house!!.owner!!.reviews!![reviewIndex] =
                review
        else
            getTenantTenancy(tenancyPosition).house!!.owner!!.reviews!!.add(review)
        tenantTenancies.value = getTenantTenanciesValue()
    }
}