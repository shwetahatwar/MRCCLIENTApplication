package com.briot.mrclogistics.implementor.ui.main

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import io.github.pierry.progress.Progress
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.briot.mrclogistics.implementor.R
import com.briot.mrclogistics.implementor.UiHelper
import com.briot.mrclogistics.implementor.repository.local.PrefConstants
import com.briot.mrclogistics.implementor.repository.local.PrefRepository
import com.briot.mrclogistics.implementor.repository.remote.VendorMaterialInward
import kotlinx.android.synthetic.main.vendor_material_scan_fragment.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.briot.mrclogistics.implementor.ui.main.SimpleVendorItemAdapter.ViewHolder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.login_fragment.*
import kotlinx.android.synthetic.main.picking_row.*
import kotlinx.android.synthetic.main.putaway_fragment.*


class VendorMaterialScanFragment : Fragment() {

    companion object {
        fun newInstance() = VendorMaterialScanFragment()
    }

    lateinit var vendorMaterialTextValue: EditText
    lateinit var vendorsubmit: Button
    private lateinit var viewModel: VendorMaterialScanViewModel
    private var oldVendorItems: Array<VendorMaterialInward?>? = null
    var inputData = VendorMaterialInward()
    private var progress: Progress? = null
    lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.vendor_material_scan_fragment, container, false)
        this.recyclerView = rootView.findViewById(R.id.vendorItems)
        recyclerView.layoutManager = LinearLayoutManager(this.activity)
        vendorMaterialTextValue = rootView.findViewById(R.id.vendor_materialBarcode)
        vendorsubmit = rootView.findViewById(R.id.vendor_items_submit_button)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(VendorMaterialScanViewModel::class.java)

        viewModel.getUsers()
        (this.activity as AppCompatActivity).setTitle("Vendor Material Scan")

        if (this.arguments != null) {
            viewModel.materialBarcode = this.arguments!!.getString("materialBarcode")
            viewModel.materialBarcode = vendorMaterialTextValue.getText().toString()
        }


        viewModel.networkError.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it == true) {
                UiHelper.hideProgress(this.progress)
                this.progress = null
                if (viewModel.messageContent != null) {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, viewModel.messageContent)
                } else {
                    UiHelper.showNoInternetSnackbarMessage(this.activity as AppCompatActivity)
                }

            }
        })

        viewModel.itemVendorSubmissionSuccessful.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it == true) {
                UiHelper.hideProgress(this.progress)
                this.progress = null

                var thisObject = this
                AlertDialog.Builder(this.activity as AppCompatActivity, R.style.MyDialogTheme).create().apply {
                    setTitle("Success")
                    setMessage("Vendor Material post successfully.")
                    setButton(AlertDialog.BUTTON_NEUTRAL, "Ok", {
                        dialog, _ -> dialog.dismiss()
                        Navigation.findNavController(thisObject.recyclerView).popBackStack(R.id.vendorMaterialScan, false)
                        //      Navigation.findNavController(thisObject.recyclerView).popBackStack()
                    })
                    show()
                }
            }
        })

        vendor_scanButton.setOnClickListener {
            // User input MATERIAL barcode value
            val inputVendorBarcode = vendorMaterialTextValue.getText().toString()
            if (inputVendorBarcode == "") {
                UiHelper.showErrorToast(this.activity as AppCompatActivity,
                        "Please enter VENDOR MATERIAL value")
                vendor_materialBarcode.requestFocus()
            }
        }


        vendor_items_submit_button.setOnClickListener {
            viewModel.materialBarcode = vendorMaterialTextValue.getText().toString()

            val logedInUsername = PrefRepository.singleInstance.getValueOrDefault(PrefConstants().username, "")
            // Log.d(ContentValues.TAG, "get value ----" + logedInUsername)
            viewModel.logedInUsername = logedInUsername
            // viewModel.getUsers()
            // Log.d(ContentValues.TAG, "api get response....." + v)


            if (vendorMaterialTextValue == null) {
                UiHelper.showErrorToast(this.activity as AppCompatActivity, "Please scan the material!")
                viewModel.messageContent = "Please scan the material"
            } else {
                GlobalScope.launch {
                    viewModel.handleSubmitVendor()
                }
            }
            vendor_materialBarcode.text?.clear()
            vendor_materialBarcode.requestFocus()
        }

        //this.progress = UiHelper.showProgressIndicator(activity!!, "Loading dispatch slip Items")
        vendor_materialBarcode.requestFocus()
    }
}

open class SimpleVendorItemAdapter(private val recyclerView: androidx.recyclerview.widget.RecyclerView,
                                   private val vendorItems: LiveData<Array<VendorMaterialInward?>>,
                                   private val viewModel: VendorMaterialScanViewModel) :
        androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.vendor_material_scan_fragment, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
        val vendorItems = vendorItems.value!![position]!!
    }

    override fun getItemCount(): Int {
        //Log.d(ContentValues.TAG, "getItemCount" + vendorItems.value)
        return vendorItems.value?.size ?: 0
    }

    open inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        protected val materialBarcode: TextView

        init {
            materialBarcode = itemView.findViewById(R.id.vendor_materialBarcode)
        }

        fun bind() {
            val item = vendorItems.value!![adapterPosition]!!
            materialBarcode.text = item.materialBarcode
        }
    }
}