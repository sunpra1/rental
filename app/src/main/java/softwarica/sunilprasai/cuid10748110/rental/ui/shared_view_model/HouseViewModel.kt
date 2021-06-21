package softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import softwarica.sunilprasai.cuid10748110.rental.model.*
import softwarica.sunilprasai.cuid10748110.rental.utils.AppState

class HouseViewModel : ViewModel() {
    private val houses = MutableLiveData<ArrayList<House>>().apply {
        value = ArrayList()
    }

    fun getHouses(): LiveData<ArrayList<House>> {
        return houses
    }

    private fun getHousesValue(): ArrayList<House> {
        return houses.value!!
    }

    fun setHouses(newHouses: ArrayList<House>) {
        houses.value = newHouses
    }

    fun getHouse(position: Int): House {
        return houses.value!![position]
    }

    fun addHouse(house: House) {
        houses.value!!.add(house)
        houses.value = getHousesValue()
    }

    fun addHouse(position: Int, house: House) {
        houses.value!!.add(position, house)
        houses.value = getHousesValue()
    }

    fun replaceHouse(position: Int, house: House) {
        houses.value!![position] = house
        houses.value = getHousesValue()
    }

    fun removeHouse(position: Int) {
        houses.value!!.removeAt(position)
        houses.value = getHousesValue()
    }

    fun addTenancy(housePosition: Int, tenancy: Tenancy) {
        getHouse(housePosition).tenancies!!.add(tenancy)
        houses.value = getHousesValue()
    }

    fun addTenancy(housePosition: Int, tenancyPosition: Int, tenancy: Tenancy) {
        getHouse(housePosition).tenancies!!.add(tenancyPosition, tenancy)
        houses.value = getHousesValue()
    }

    fun replaceTenancy(housePosition: Int, tenancyPosition: Int, tenancy: Tenancy) {
        getHouse(housePosition).tenancies!![tenancyPosition] = tenancy
        houses.value = getHousesValue()
    }

    fun deleteTenancy(housePosition: Int, tenancyPosition: Int) {
        getHouse(housePosition).tenancies!!.removeAt(tenancyPosition)
        houses.value = getHousesValue()
    }

    fun addToLet(housePosition: Int, toLet: ToLet) {
        getHouse(housePosition).toLets!!.add(toLet)
        houses.value = getHousesValue()
    }

    fun addToLet(housePosition: Int, toLetPosition: Int, toLet: ToLet) {
        getHouse(housePosition).toLets!!.add(toLetPosition, toLet)
        houses.value = getHousesValue()
    }

    fun replaceToLet(housePosition: Int, toLetPosition: Int, toLet: ToLet) {
        getHouse(housePosition).toLets!![toLetPosition] = toLet
        houses.value = getHousesValue()
    }

    fun deleteToLet(housePosition: Int, toLetPosition: Int) {
        getHouse(housePosition).toLets!!.removeAt(toLetPosition)
        houses.value = getHousesValue()
    }

    fun deletePayment(housePosition: Int, tenancyPosition: Int, paymentPosition: Int) {
        getTenancy(housePosition, tenancyPosition).payments!!.removeAt(
            paymentPosition
        )
        houses.value = getHousesValue()
    }

    fun deleteUtility(housePosition: Int, tenancyPosition: Int, utilityPosition: Int) {
        getTenancy(housePosition, tenancyPosition).utilities!!.removeAt(
            utilityPosition
        )
        houses.value = getHousesValue()
    }

    fun addPayment(
        payment: Payment,
        housePosition: Int,
        tenancyPosition: Int,
        paymentPosition: Int
    ) {
        getTenancy(housePosition, tenancyPosition).payments!!.add(
            paymentPosition,
            payment
        )
        houses.value = getHousesValue()
    }

    fun addUtility(
        utility: Utility,
        housePosition: Int,
        tenancyPosition: Int,
        utilityPosition: Int
    ) {
        getTenancy(housePosition, tenancyPosition).utilities!!.add(
            utilityPosition,
            utility
        )
        houses.value = getHousesValue()
    }

    fun getUtility(housePosition: Int, tenancyPosition: Int, utilityPosition: Int): Utility {
        return getTenancy(housePosition, tenancyPosition).utilities!![utilityPosition]
    }

    fun getPayment(housePosition: Int, tenancyPosition: Int, paymentPosition: Int): Payment {
        return getTenancy(housePosition, tenancyPosition).payments!![paymentPosition]
    }

    fun getTenancy(housePosition: Int, tenancyPosition: Int): Tenancy {
        return houses.value!![housePosition].tenancies!![tenancyPosition]
    }

    fun getToLet(housePosition: Int, toLetPosition: Int): ToLet {
        return getHouse(housePosition).toLets!![toLetPosition]
    }

    fun getTenancyHistory(housePosition: Int, tenancyPosition: Int): Tenancy {
        return getHouse(housePosition).tenanciesHistory!![tenancyPosition]
    }

    fun addTenancyHistory(housePosition: Int, tenancyPosition: Int, tenancy: Tenancy) {
        getHouse(housePosition).tenanciesHistory!!.add(tenancyPosition, tenancy)
        houses.value = getHousesValue()
    }

    fun deleteTenancyHistory(housePosition: Int, tenancyPosition: Int) {
        getHouse(housePosition).tenanciesHistory!!.removeAt(tenancyPosition)
        houses.value = getHousesValue()
    }

    fun replaceTenancyHistory(housePosition: Int, tenancyPosition: Int, tenancy: Tenancy) {
        getHouse(housePosition).tenanciesHistory!![tenancyPosition] = tenancy
        houses.value = getHousesValue()
    }

    fun getHouseTenancyHistoryPayment(
        housePosition: Int,
        tenancyPosition: Int,
        paymentPosition: Int
    ): Payment {
        return getHouse(housePosition).tenanciesHistory!![tenancyPosition].payments!![paymentPosition]
    }

    fun addHouseTenantReview(housePosition: Int, tenancyPosition: Int, review: Review) {
        val reviewIndex = getTenancy(
            housePosition,
            tenancyPosition
        ).tenant!!.reviews!!.indexOfFirst { it.user!!.id == AppState.loggedInUser!!.id }
        if (reviewIndex > -1)
            getTenancy(housePosition, tenancyPosition).tenant!!.reviews!![reviewIndex] = review
        else
            getTenancy(housePosition, tenancyPosition).tenant!!.reviews!!.add(review)
        houses.value = getHousesValue()
    }
}