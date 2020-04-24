package com.briot.mrclogistics.implementor.ui.main

import android.annotation.SuppressLint
import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.briot.mrclogistics.implementor.UiHelper
import com.briot.mrclogistics.implementor.repository.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class VendorMaterialScanViewModel : ViewModel() {
    var materialBarcode: String? = ""
    var userId: String? = ""
    var logedInUsername: String? = ""

    val TAG = "VendorMaterialScanViewModel"

    val networkError: LiveData<Boolean> = MutableLiveData()
    val itemVendorSubmissionSuccessful: LiveData<Boolean> = MutableLiveData()

    val vendorItems: LiveData<Array<VendorMaterialInward?>> = MutableLiveData()
    val invalidVendorItems: Array<VendorMaterialInward?> = arrayOf(null)
    val invalidvendorPutItems: LiveData<Array<PostVendorResponse?>> = MutableLiveData()
    val users: LiveData<Array<User?>> = MutableLiveData()
    var userResponseData: Array<User?> = arrayOf(null)
    var messageContent: String = ""

    // var username: String?=""

//    fun getUserID(responseData: Array<>, logedInUsername: String?){
//        Log.d(ContentValues.TAG, "from function userResponseData ---- " + responseData)
//        Log.d(ContentValues.TAG, "from function logedInUsername ---- " + logedInUsername)
//    }

    fun handleSubmitVendor() {
        var VendorMaterialInward = VendorMaterialInward()

        VendorMaterialInward.materialBarcode = materialBarcode

        for (item in userResponseData) {
            if (item!!.username == logedInUsername){
                Log.d(ContentValues.TAG, "item ----id " + item!!.id)
                VendorMaterialInward.userId = item.id?.toString()
            }
        }
        Log.d(ContentValues.TAG, "item ---- VendorMaterialInward " + VendorMaterialInward)

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                (networkError as MutableLiveData<Boolean>).value = false
            }
        }
        RemoteRepository.singleInstance.postMaterialInwards(VendorMaterialInward,
                this::handleVendorItemsResponse, this::handleVendorItemsError)
        Log.d(ContentValues.TAG, " after post ------")

    }

    private fun handleVendorItemsResponse(postVendorItemResponse: VendorMaterialInward?) {
        Log.d(ContentValues.TAG, " response ----- " + postVendorItemResponse)
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                (itemVendorSubmissionSuccessful as MutableLiveData<Boolean>).value = true
            }
        }
    }

    private fun handleVendorItemsError(error: Throwable) {
        Log.d(ContentValues.TAG, error.localizedMessage)
        if (UiHelper.isNetworkError(error)) {
            (networkError as MutableLiveData<Boolean>).value = true
            messageContent = "Not able to connect to the server."
        } else if (error is HttpException) {
            if (error.code() >= 401) {
                if (error.code() == 500) {
                    messageContent = "Invalid barcode scanned"
                }else {
                    var msg = error.response()?.errorBody()?.string()
                    if (msg != null && msg.isNotEmpty()) {
                        messageContent = msg
                    } else {
                        messageContent = error.message()
                    }
                }
            }
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.vendorItems as MutableLiveData<Array<VendorMaterialInward?>>).value = invalidVendorItems
            messageContent = "Oops something went wrong."
        }

    }

    fun getUsers(){
        RemoteRepository.singleInstance.getUsers(this::handleUserResponse, this::handleVendorItemsError)
    }

    private fun handleUserResponse(users: Array<User?>) {

        userResponseData = users
        Log.d(ContentValues.TAG, "item  response----- " + userResponseData)
        Log.d(ContentValues.TAG, "item  handleUserResponse----- " + userResponseData[1]!!.username)
        (this.users as MutableLiveData<Array<User?>>).value = users

    }

}