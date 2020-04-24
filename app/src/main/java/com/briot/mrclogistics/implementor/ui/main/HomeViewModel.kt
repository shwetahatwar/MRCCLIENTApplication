package com.briot.mrclogistics.implementor.ui.main

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import com.briot.mrclogistics.implementor.UiHelper
import com.briot.mrclogistics.implementor.repository.remote.PickingsDashboardData
import com.briot.mrclogistics.implementor.repository.remote.PutawayDashboardData
import com.briot.mrclogistics.implementor.repository.remote.PutawayItems
import com.briot.mrclogistics.implementor.repository.remote.RemoteRepository
//import com.briot.mrclogistics.implementor.repository.remote.RoleAccessRelation
import java.net.SocketException
import java.net.SocketTimeoutException

class HomeViewModel : ViewModel() {
    val TAG = "HomeViewModel"

    var totalCount: Number? = null
    var putawayCount: Number? = null
    var pendingCount: Number? = null

    var pickedTotalCount: Number? = null
    var pickedCount: Number? = null
    var pickedPendingCount: Number? = null

    val putawayDashboardData: LiveData<PutawayDashboardData?> = MutableLiveData()
    val pickingsDashboardData: LiveData<PickingsDashboardData?> = MutableLiveData()

    val invalidPickingsDashboardData: Array<PickingsDashboardData?> = arrayOf(null)
    val invalidPutawayDashboardData: Array<PutawayDashboardData?> = arrayOf(null)

    var getResponsePutwayDashboardData: LiveData<PutawayDashboardData?> = MutableLiveData()
    val networkError: LiveData<Boolean> = MutableLiveData<Boolean>()

    fun loadPutawayDashboardItems() {
        (networkError as MutableLiveData<Boolean>).value = false
        (this.putawayDashboardData as MutableLiveData<PutawayDashboardData>).value = null

        RemoteRepository.singleInstance.getPutawayCount(this::handlePutawayDashboardItemsResponse, this::handlePutawayDashboardItemsError)
    }

    private fun handlePutawayDashboardItemsResponse(putawayDashboardData: PutawayDashboardData?) {
        // Setting response data to display
        putawayCount = putawayDashboardData!!.putawayCount
        pendingCount = putawayDashboardData!!.pendingCount
        totalCount = putawayDashboardData!!.totalCount

        (this.putawayDashboardData as MutableLiveData<PutawayDashboardData>).value = putawayDashboardData
    }

    private fun handlePutawayDashboardItemsError(error: Throwable) {
        Log.d(ContentValues.TAG, "-----in error"+ error)
        if (UiHelper.isNetworkError(error)) {
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.putawayDashboardData as MutableLiveData<Array<PutawayDashboardData?>>).value = invalidPutawayDashboardData
        }
    }


    fun loadPickingsDashboardItems() {
        (networkError as MutableLiveData<Boolean>).value = false
        (this.pickingsDashboardData as MutableLiveData<PickingsDashboardData>).value = null

        RemoteRepository.singleInstance.getPickingsCount(this::handlePickingDashboardItemsResponse,
                this::handlePickingDashboardItemsError)
    }

    private fun handlePickingDashboardItemsResponse(pickingsDashboardData: PickingsDashboardData?) {
        // Setting response data to display
        pickedTotalCount = pickingsDashboardData!!.totalCount
        pickedCount = pickingsDashboardData!!.pickedCount
        pickedPendingCount = pickingsDashboardData!!.pendingCount

        (this.pickingsDashboardData as MutableLiveData<PickingsDashboardData>).value = pickingsDashboardData
    }

    private fun handlePickingDashboardItemsError(error: Throwable) {
        Log.d(ContentValues.TAG, "-----in error"+ error)
        if (UiHelper.isNetworkError(error)) {
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.pickingsDashboardData as MutableLiveData<Array<PickingsDashboardData?>>).value = invalidPickingsDashboardData
        }
    }


}
