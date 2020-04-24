package com.briot.mrclogistics.implementor.ui.main

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

class PickingViewModel : ViewModel() {

    var id: Int = 0
    var rackBarcodeSerial: String? = ""
    var binBarcodeSerial: String? = ""
    var materialBarcodeSerial: String? = ""

    val TAG = "PickingViewModel"

    val networkError: LiveData<Boolean> = MutableLiveData()
    val pickingItems: LiveData<Array<PickingItems?>> = MutableLiveData()
    val invalidPickingItems: Array<PickingItems?> = arrayOf(null)
    val itemSubmissionPickingSuccessful: LiveData<Boolean> = MutableLiveData()
    val pickingScannedItems: LiveData<Array<PickingItemsScanned?>> = MutableLiveData()
    var responsePickingLoadingItems: Array<PickingItems?> = arrayOf(null)

    var messageContent: String = ""

    fun loadPickingItems() {
        (networkError as MutableLiveData<Boolean>).value = false
        (this.pickingItems as MutableLiveData<Array<PickingItems?>>).value = emptyArray()
        RemoteRepository.singleInstance.getPickingItems(this::handlePickingItemsResponse, this::handlePickingItemsError)
    }

    fun loadPickingScannedItems() {
        Log.d(TAG,"submit call loadPutwayScanned --->")
        (networkError as MutableLiveData<Boolean>).value = false
        // (this.putawayScannedItems as MutableLiveData<Array<PutawayItemsScanned?>>).value = emptyArray()
        RemoteRepository.singleInstance.getPickingScannedItems(this::handlePickingScannedItemResponse,
                this::handlePickingItemsError)
    }
     fun handlePickingScannedItemResponse(pickingScannedItems: Array<PickingItemsScanned?>) {
         // Log.d(TAG, "picking API res -->"+pickingScannedItems)
        (this.pickingScannedItems as MutableLiveData<Array<PickingItemsScanned?>>).value = pickingScannedItems

    }
    private fun handlePickingItemsResponse(pickingItems: Array<PickingItems?>) {
        responsePickingLoadingItems = pickingItems
        (this.pickingItems as MutableLiveData<Array<PickingItems?>>).value = pickingItems

    }

    private fun handlePickingItemsError(error: Throwable) {
        Log.d(TAG, error.localizedMessage)
        if (UiHelper.isNetworkError(error)) {
            (networkError as MutableLiveData<Boolean>).value = true
            messageContent = "Not able to connect to the server."
        } else if (error is HttpException) {
            if (error.code() >= 401) {
                var msg = error.response()?.errorBody()?.string()
                if (msg != null && msg.isNotEmpty()) {
                    messageContent = msg
                } else {
                    messageContent = error.message()
                }
            }
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.pickingItems as MutableLiveData<Array<PickingItems?>>).value = invalidPickingItems
            messageContent = "Oops something went wrong."
        }
    }

    suspend fun handleSubmitPicking() {
        var pickingRequestObject = PickingItems()
        pickingRequestObject.binBarcodeSerial = binBarcodeSerial
        pickingRequestObject.materialBarcodeSerial = materialBarcodeSerial
        pickingRequestObject.rackBarcodeSerial = rackBarcodeSerial

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                (networkError as MutableLiveData<Boolean>).value = false
            }
        }

        RemoteRepository.singleInstance.putPickingItems(pickingRequestObject,
                this::handlePickingPutItemsResponse, this::handlePickingPutItemsError)
    }

    private fun handlePickingPutItemsResponse(putPickingResponse: PutPickingResponse?) {
        Log.d(TAG, "Data Picking Put Response" + putPickingResponse)
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                (itemSubmissionPickingSuccessful as MutableLiveData<Boolean>).value = true
            }
        }
    }
    private fun handlePickingPutItemsError(error: Throwable) {
        Log.d(TAG, error.localizedMessage)
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
            (this.pickingItems as MutableLiveData<Array<PickingItems?>>).value = invalidPickingItems
            messageContent = "Oops something went wrong."
        }
    }
}
