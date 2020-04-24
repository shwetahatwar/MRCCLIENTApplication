package com.briot.mrclogistics.implementor.repository.remote

import io.reactivex.Observable
import retrofit2.http.*


class SignInRequest {
    var username: String? = null
    var password: String? = null
    var deviceId: String? = null
}

class SignInResponse {
    var userId: Number? = null
    var token: String? =  null
    var id: String? = null
    var username: String? = null
    var password: String? = null
    var deviceId: String? = null
    var status: Number? = null
}

class Role {
    var id: Number? = null
    var roleName:  String? = null
}

class User {
    var username: String? = null
    var id: Number? = null
    var token: String? = null
}

class Material {
    var materialType: String? = null
    var materialCode: String? = null
    var materialDescription: String? = null
    var genericName: String? = null
    var packingType: String? = null
    var packSize: String? = null
    var netWeight: String? = null
    var grossWeight: String? = null
    var tareWeight: String? = null
    var UOM: String? = null
    var batchCode: String?  = null
    var status: String? = null
//    var createdBy: User? = null
//    var updatedBy: User? = null
}
class PutawayItems {
    var rackBarcodeSerial: String? = null
    var binBarcodeSerial: String? = null
    var materialBarcodeSerial: String? = null
}

class PutawayItemsScanned{
    var rackBarcodeSerial: String? = null
    var binBarcodeSerial: String? = null
    var materialBarcodeSerial: String? = null
}

class VendorMaterialInward{
    var materialBarcode: String? = null
    var userId: String? = null
}

class PutPutawayResponse {
    var message: String? = null
}

class PostVendorResponse{
    var message: String? = null

}

class PickingItems {
    var rackBarcodeSerial: String? = null
    var binBarcodeSerial: String? = null
    var materialBarcodeSerial: String? = null
}

class PickingItemsScanned {
    var rackBarcodeSerial: String? = null
    var binBarcodeSerial: String? = null
    var materialBarcodeSerial: String? = null
}

class PutPickingResponse {
    var message: String? = null
}

class AuditItem {
    var materialBarcode: String? = null
    var userId: Number? = null
}
class AuditItemResponse {
    var message: String? = null
}

class PutawayDashboardData{
    var totalCount: Number? = null
    var putawayCount: Number? = null
    var pendingCount: Number? = null
}
class PickingsDashboardData{
    var totalCount: Number? = null
    var pickedCount: Number? = null
    var pendingCount: Number? = null
}

interface ApiInterface {
    @POST("users/sign_in")
    fun login(@Body signInRequest: SignInRequest): Observable<SignInResponse>

    @GET("users")
    fun getUsers(): Observable<Array<User?>>

    @GET("putaways/get/dashboardCount")
    fun getPutawayCount(): Observable<PutawayDashboardData?>

    @GET("pickings/get/dashboardCount")
    fun getPickingsCount(): Observable<PickingsDashboardData?>


    @GET("putaways")
    fun getPutaway(): Observable<Array<PutawayItems?>>

    @GET("putaways/scanned")
    fun getPutawayScannedItems(): Observable<Array<PutawayItemsScanned?>>

    @GET("pickings/picked")
    fun getPickingScannedItems(): Observable<Array<PickingItemsScanned?>>

//    @PUT("putaways/{id}")
//    fun putPutawayItems(@Path("id") id: Int, @Body requestbody: PutawayItems): Observable<PutPutawayResponse?>

    @PUT("putaways/1")
    fun putPutawayItems(@Body requestbody: PutawayItems): Observable<PutPutawayResponse?>

    @GET("pickings")
    fun getPickingItems():Observable<Array<PickingItems?>>

    @PUT("pickings/1")
    fun putPickingItems(@Body requestbody: PickingItems): Observable<PutPickingResponse?>

//    @POST("/materialinwards")
//    fun postMaterialInwards(@Path("userId") userId: String,@Body requestbody: VendorMaterialInward): Observable<Array<VendorMaterialInward?>>

    @POST("/materialinwards")
    fun postMaterialInwards(@Body requestbody: VendorMaterialInward):
            Observable<VendorMaterialInward?>

    @POST("/audits")
    fun postAuditsItems(@Body requestbody: AuditItem): Observable<AuditItemResponse?>
}

