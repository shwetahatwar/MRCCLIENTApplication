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
import com.briot.mrclogistics.implementor.repository.remote.PutawayItems
import retrofit2.HttpException

class PutawayViewModel : ViewModel() {

    var rackBarcodeSerial: String? = ""
    var binBarcodeSerial: String? = ""
    var materialBarcodeSerial: String? = ""

    val TAG = "PutawayViewModel"

    val networkError: LiveData<Boolean> = MutableLiveData()
    val itemSubmissionSuccessful: LiveData<Boolean> = MutableLiveData()

    val putawayItems: LiveData<Array<PutawayItems?>> = MutableLiveData()
    val putawayScannedItems: LiveData<Array<PutawayItemsScanned?>> = MutableLiveData()
    val invalidPutawayItems: Array<PutawayItems?> = arrayOf(null)
    var responsePutawayLoadingItems: Array<PutawayItems?> = arrayOf(null)
    var getResponsePutwayData: Array<PutawayItems?> = arrayOf(null)
    var messageContent: String = ""


    fun loadPutawayItems() {
        (networkError as MutableLiveData<Boolean>).value = false
        (this.putawayItems as MutableLiveData<Array<PutawayItems?>>).value = emptyArray()
        RemoteRepository.singleInstance.getPutaway(this::handlePutawayItemsResponse, this::handlePutawayItemsError)
    }
    fun loadPutawayScannedItems() {
        Log.d(TAG,"submit call loadPutwayScanned --->")
        (networkError as MutableLiveData<Boolean>).value = false
        // (this.putawayScannedItems as MutableLiveData<Array<PutawayItemsScanned?>>).value = emptyArray()
        RemoteRepository.singleInstance.getPutawayScannedItems(this::handlePutawayScannedItemResponse,
                this::handlePutawayItemsError)
    }

    fun refreshPutawayScannedItems() {
        Log.d(TAG,"submit call refresh --->")
        (networkError as MutableLiveData<Boolean>).value = false
        // (this.putawayScannedItems as MutableLiveData<Array<PutawayItemsScanned?>>).value = emptyArray()
        RemoteRepository.singleInstance.getPutawayScannedItems(this::handlePutawayScannedItemResponse,
                this::handlePutawayItemsError)
    }

    private fun handlePutawayItemsResponse(putawayItems: Array<PutawayItems?>) {
        (this.putawayItems as MutableLiveData<Array<PutawayItems?>>).value = putawayItems
    }

    private fun handlePutawayScannedItemResponse(putawayScannedItems: Array<PutawayItemsScanned?>) {
        // Log.d(TAG, "response in api --> "+putawayScannedItems)
        Log.d(TAG, "response in api --> "+putawayScannedItems!!.size)
        (this.putawayScannedItems as MutableLiveData<Array<PutawayItemsScanned?>>).value = putawayScannedItems
    }

    private fun handlePutawayItemsError(error: Throwable) {
        Log.d(TAG, "eror----->"+error.localizedMessage)
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
            (this.putawayItems as MutableLiveData<Array<PutawayItems?>>).value = invalidPutawayItems
            messageContent = "Oops something went wrong."
        }
    }

    fun loadPutawayRefreshItems() {
        (networkError as MutableLiveData<Boolean>).value = false
        (this.putawayItems as MutableLiveData<Array<PutawayItems?>>).value = emptyArray()
        RemoteRepository.singleInstance.getPutaway(this::handlePutawayRefreshItemsResponse, this::handlePutawayRefreshItemsError)
    }

    private fun handlePutawayRefreshItemsResponse(putawayItems: Array<PutawayItems?>) {

        (this.putawayItems as MutableLiveData<Array<PutawayItems?>>).value = putawayItems
    }

    private fun handlePutawayRefreshItemsError(error: Throwable) {
        if (UiHelper.isNetworkError(error)) {
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.putawayItems as MutableLiveData<Array<PutawayItems?>>).value = invalidPutawayItems
        }
    }

    fun handleSubmitPutaway() {
        var putawayRequestObject = PutawayItems()
        putawayRequestObject.binBarcodeSerial = binBarcodeSerial
        putawayRequestObject.materialBarcodeSerial = materialBarcodeSerial
        putawayRequestObject.rackBarcodeSerial = rackBarcodeSerial

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                (networkError as MutableLiveData<Boolean>).value = false
            }
        }

        // put call for putaway
        RemoteRepository.singleInstance.putPutawayItems(putawayRequestObject,
                this::handlePutawayPutItemsResponse, this::handlePutawayPutItemsError)
    }

    private fun handlePutawayPutItemsResponse(putPutawayResponse: PutPutawayResponse?) {
        //Log.d(TAG, "Data Putaway Put Response -->" + putPutawayResponse)
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                (itemSubmissionSuccessful as MutableLiveData<Boolean>).value = true
            }
        }
    }

    fun handlePutawayPutItemsError(error: Throwable) {
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
            (this.putawayItems as MutableLiveData<Array<PutawayItems?>>).value = invalidPutawayItems
            messageContent = "Oops something went wrong."
        }
    }
}
