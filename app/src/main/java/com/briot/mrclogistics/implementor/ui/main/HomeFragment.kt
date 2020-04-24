package com.briot.mrclogistics.implementor.ui.main

//import com.briot.mrclogistics.implementor.repository.remote.RoleAccessRelation

import android.Manifest
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.briot.mrclogistics.implementor.R
import kotlinx.android.synthetic.main.home_fragment.*
// import java.util.UUID;
import android.os.Build;
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.briot.mrclogistics.implementor.UiHelper
import com.briot.mrclogistics.implementor.repository.remote.PickingsDashboardData
import com.briot.mrclogistics.implementor.repository.remote.PutawayDashboardData
import com.briot.mrclogistics.implementor.repository.remote.PutawayItems
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.putaway_fragment.*

class HomeFragment : androidx.fragment.app.Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel
//    lateinit var totalTextValue: TextView
//    lateinit var putawayTextValue: TextView
//    lateinit var pendingTextValue: TextView

    private var progress: Progress? = null
    private var oldPutawayDashboardItems: PutawayDashboardData? = null
    private var oldPickingsDashboardData: PickingsDashboardData? = null
    var inputData1 = PickingsDashboardData()
    var inputData = PutawayDashboardData()

//    lateinit var cardView: CardView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.home_fragment, container, false)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        viewModel.loadPickingsDashboardItems()
        viewModel.loadPutawayDashboardItems()

//        totalTextValue = rootView.findViewById(R.id.totalText)
//        putawayTextValue = rootView.findViewById(R.id.putawayText)
//        pendingTextValue = rootView.findViewById(R.id.pendingText)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
//
//        viewModel.loadPutawayDashboardItems()
//        viewModel.loadPickingsDashboardItems()

        (this.activity as AppCompatActivity).setTitle("Dashboard")

        if (this.arguments != null) {
            viewModel.totalCount = this.arguments!!.getInt("totalCount")
            viewModel.pendingCount = this.arguments!!.getInt("pendingCount")
            viewModel.putawayCount = this.arguments!!.getInt("putawayCount")

            viewModel.pickedCount = this.arguments!!.getInt("pickedCount")
            viewModel.pickedPendingCount = this.arguments!!.getInt("pickedPendingCount")
            viewModel.pickedTotalCount = this.arguments!!.getInt("pickedTotalCount")
        }
        // recyclerView.adapter = SimpleDashboardItemAdapter(recyclerView, viewModel.putawayDashboardData, viewModel)
        viewModel.putawayDashboardData.observe(viewLifecycleOwner, Observer<PutawayDashboardData?> {
            if (it != null) {
                UiHelper.hideProgress(this.progress)
                this.progress = null

//                Log.d(TAG, "-----in homeFragment"+ viewModel.putawayDashboardData)
//                Log.d(TAG, "-----in putawayCount"+ viewModel.putawayCount)

//                totalPickingValue.text = viewModel.pickedTotalCount.toString()
//                pickingValue.text = viewModel.pickedCount.toString()
//                pickingPendingValue.text = viewModel.pickedPendingCount.toString()

                putawayText.text = viewModel.putawayCount.toString()
                pendingText.text = viewModel.pendingCount.toString()
                totalText.text = viewModel.totalCount.toString()


                if ( viewModel.putawayDashboardData.value == null) {
                    UiHelper.showSomethingWentWrongSnackbarMessage(this.activity as AppCompatActivity)
                } else if (it != oldPutawayDashboardItems) {
                    Log.d(TAG, "oldPutwayDashboard data")
                    //putawayItems.adapter?.notifyDataSetChanged()
                }
            }
            oldPutawayDashboardItems = viewModel.putawayDashboardData.value
        })

//-------------------------------

        viewModel.pickingsDashboardData.observe(viewLifecycleOwner, Observer<PickingsDashboardData?> {
            if (it != null) {
                UiHelper.hideProgress(this.progress)
                this.progress = null

//                Log.d(TAG, "-----in homeFragment"+ viewModel.putawayDashboardData)
//                Log.d(TAG, "-----in putawayCount"+ viewModel.putawayCount)

                totalPickingValue.text = viewModel.pickedTotalCount.toString()
                pickingValue.text = viewModel.pickedCount.toString()
                pickingPendingValue.text = viewModel.pickedPendingCount.toString()

//                putawayText.text = viewModel.putawayCount.toString()
//                pendingText.text = viewModel.pendingCount.toString()
//                totalText.text = viewModel.totalCount.toString()


                if ( viewModel.pickingsDashboardData.value == null) {
                    UiHelper.showSomethingWentWrongSnackbarMessage(this.activity as AppCompatActivity)
                } else if (it != oldPickingsDashboardData) {
                    Log.d(TAG, "oldPutwayDashboard data")
                    //putawayItems.adapter?.notifyDataSetChanged()
                }
            }
            oldPickingsDashboardData = viewModel.pickingsDashboardData.value
        })




        //-------------------------------------
        materialPutaway.setOnClickListener { Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_putawayFragment) }
        materialPicking.setOnClickListener { Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_pickingFragment) }
        vendorMaterialScan.setOnClickListener { Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_vendorMaterialScanFragment) }
        physicalStock.setOnClickListener { Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_physicalStockVerificationFragment) }
    }
}