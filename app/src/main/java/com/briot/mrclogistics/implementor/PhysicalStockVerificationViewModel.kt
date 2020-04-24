package com.briot.mrclogistics.implementor

import android.annotation.SuppressLint
import android.content.ContentValues
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.briot.mrclogistics.implementor.repository.local.PrefConstants
import com.briot.mrclogistics.implementor.repository.local.PrefRepository
import com.briot.mrclogistics.implementor.repository.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class PhysicalStockVerificationViewModel : ViewModel() {
    var materialBarcode: String? = ""
    var userId: Int = 0
    var logedInUsername: String? = ""

    val TAG = "PhysicalStockVerificationViewModel"
    val itemAuditSubmissionSuccessful: LiveData<Boolean> = MutableLiveData()
    val networkError: LiveData<Boolean> = MutableLiveData()
    val auditItems: LiveData<Array<AuditItem?>> = MutableLiveData()
    val invalidAuditItems: Array<AuditItem?> = arrayOf(null)
    val invalidAuditPutItems: LiveData<Array<AuditItemResponse?>> = MutableLiveData()
    val users: LiveData<Array<User?>> = MutableLiveData()
    var userResponseData: Array<User?> = arrayOf(null)
    var messageContent: String = ""


    fun handleSubmitAudit() {
        var auditItems = AuditItem()
        auditItems.materialBarcode = materialBarcode
        //auditItems.userId=userId
        // auditItems.userId = PrefRepository.singleInstance.getValueOrDefault(PrefConstants().USER_ID, "0").toInt()
        for (item in userResponseData) {
            if (item!!.username == logedInUsername) {
                Log.d(ContentValues.TAG, "item ----id " + item!!.id)
                auditItems.userId = item.id
            }
        }

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                (networkError as MutableLiveData<Boolean>).value = false
            }
        }

        RemoteRepository.singleInstance.postAuditsItems(auditItems, this::handleAuditItemsResponse, this::handleAuditItemsError)
    }
//            RemoteRepository.singleInstance.postAuditsItems(AuditItem,this::handleAuditItemsResponse, this::handleAuditItemsError)
//        }
    private fun handleAuditItemsResponse(auditItemResponse: AuditItemResponse?) {
        // (this.vendorItems as MutableLiveData<Array<VendorMaterialInward?>>).value = vendorItems
        // Log.d(TAG,"Data Putaway Put Response"+ postVendorResponse)
        Log.d(ContentValues.TAG, " response ----- " + auditItemResponse)

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                (itemAuditSubmissionSuccessful as MutableLiveData<Boolean>).value = true
            }
        }
    }

    private fun handleAuditItemsError(error: Throwable) {
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
            (this.auditItems as MutableLiveData<Array<AuditItem?>>).value = invalidAuditItems
            messageContent = "Oops something went wrong."

        }
    }

    fun getUsers() {
        RemoteRepository.singleInstance.getUsers(
                this::handleUserResponse, this::handleAuditItemsError)
    }

    private fun handleUserResponse(users: Array<User?>) {

        userResponseData = users
        (this.users as MutableLiveData<Array<User?>>).value = users
    }
}
