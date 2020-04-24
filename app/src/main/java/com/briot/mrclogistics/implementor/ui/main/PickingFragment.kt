package com.briot.mrclogistics.implementor.ui.main

import android.content.ContentValues.TAG
import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.briot.mrclogistics.implementor.MainApplication

import com.briot.mrclogistics.implementor.R
import com.briot.mrclogistics.implementor.UiHelper
import com.briot.mrclogistics.implementor.repository.local.PrefConstants
import com.briot.mrclogistics.implementor.repository.remote.PickingItems
import com.briot.mrclogistics.implementor.repository.remote.PutPickingResponse
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.material_details_scan_fragment.*
import kotlinx.android.synthetic.main.picking_fragment.*
import kotlinx.android.synthetic.main.picking_fragment.picking_materialBarcode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PickingFragment : Fragment() {

    companion object {
        fun newInstance() = PickingFragment()
    }

    lateinit var pickingMaterialTextValue: EditText
    lateinit var binMaterialTextValue: EditText
    lateinit var rackMaterialTextValue: EditText
    lateinit var materialBarcodeScanButton: Button
    lateinit var rackBarcodeScanButton: Button
    lateinit var binBarcodeScanButton: Button
    lateinit var picking_submitItemButton: Button

    private lateinit var viewModel: PickingViewModel
    private var progress: Progress? = null
    private var oldPickingItems: Array<PickingItems?>? = null
    lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.picking_fragment, container, false)
        this.recyclerView = rootView.findViewById(R.id.pickingItems)
        recyclerView.layoutManager = LinearLayoutManager(this.activity)
        pickingMaterialTextValue = rootView.findViewById(R.id.picking_materialBarcode)
        binMaterialTextValue = rootView.findViewById(R.id.picking_binBarcode)
        rackMaterialTextValue = rootView.findViewById(R.id.picking_rackBarcode)
        materialBarcodeScanButton = rootView.findViewById(R.id.picking_material_scanButton)
        rackBarcodeScanButton = rootView.findViewById(R.id.picking_bin_scanButton)
        binBarcodeScanButton = rootView.findViewById(R.id.picking_rack_scanButton)
        picking_submitItemButton = rootView.findViewById(R.id.picking_submit_button)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(PickingViewModel::class.java)
        // TODO: Use the ViewModel

        (this.activity as AppCompatActivity).setTitle("Picking")

        if (this.arguments != null) {
            viewModel.rackBarcodeSerial = this.arguments!!.getString("rackBarcodeSerial")
            viewModel.binBarcodeSerial = this.arguments!!.getString("binBarcodeSerial")
            viewModel.materialBarcodeSerial = this.arguments!!.getString("materialBarcodeSerial")
            viewModel.rackBarcodeSerial = binMaterialTextValue.getText().toString()
        }
        recyclerView.adapter = SimplePickingItemAdapter(recyclerView, viewModel.pickingItems,viewModel)
        viewModel.pickingItems.observe(viewLifecycleOwner, Observer<Array<PickingItems?>> {
            if (it != null) {
                UiHelper.hideProgress(this.progress)
                this.progress = null

                if (viewModel.pickingItems.value.orEmpty().isNotEmpty() && viewModel.pickingItems.value?.first() == null) {
                    UiHelper.showSomethingWentWrongSnackbarMessage(this.activity as AppCompatActivity)
                } else if (it != oldPickingItems) {
                    pickingItems.adapter?.notifyDataSetChanged()
                }
            }

            oldPickingItems = viewModel.pickingItems.value
        })

        viewModel.networkError.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it == true) {
                UiHelper.hideProgress(this.progress)
                this.progress = null
                // Log.d(TAG,"-----in if---"+viewModel.messageContent)
                if (viewModel.messageContent != null) {
                    // Log.d(TAG,"-----in if 2---"+viewModel.messageContent)
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, viewModel.messageContent)
                } else {
                    UiHelper.showNoInternetSnackbarMessage(this.activity as AppCompatActivity)
                }

            }
        })

        viewModel.itemSubmissionPickingSuccessful.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it == true) {
                UiHelper.hideProgress(this.progress)
                this.progress = null

                var thisObject = this
                AlertDialog.Builder(this.activity as AppCompatActivity, R.style.MyDialogTheme).create().apply {
                    setTitle("Success")
                    setMessage("Material updated successfully.")
                    setButton(AlertDialog.BUTTON_NEUTRAL, "Ok") { dialog, _ -> dialog.dismiss()
                        Navigation.findNavController(thisObject.recyclerView).popBackStack(R.id.materialPicking, false)
                        //      Navigation.findNavController(thisObject.recyclerView).popBackStack()
                    }
                    show()
                }
            }
        })

        picking_materialBarcode.setOnEditorActionListener { _, i, keyEvent ->
            var handled = false
            //  var value = loading_materialBarcode.text!!.toString()
            var materialBarcodeSerial = picking_materialBarcode.text!!.toString()

            if (keyEvent == null) {
                Log.d("picking: ", "event is null")
                UiHelper.showErrorToast(this.activity as AppCompatActivity, "event is null")
            }else if ((picking_materialBarcode.text != null && picking_materialBarcode.text!!.isNotEmpty()) && i
                    == EditorInfo.IME_ACTION_DONE || ((keyEvent.keyCode == KeyEvent.KEYCODE_ENTER || keyEvent.keyCode == KeyEvent.KEYCODE_TAB)
                            && keyEvent.action == KeyEvent.ACTION_DOWN)) {
                this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
                UiHelper.hideProgress(this.progress)
                this.progress = null
                handled = true
            }
            handled
        }

        this.progress = UiHelper.showProgressIndicator(activity!!, "Picking Items")
        viewModel.loadPickingItems()
        viewModel.loadPickingScannedItems()

        // On click on Material Barcode scan button, to get material barcode value
        picking_material_scanButton.setOnClickListener{
            var matchFlag: Boolean = false
            for (item in viewModel.pickingItems.value!!){
                // Database Material barcode value
                val cmpltMaterialBarcode = item!!.materialBarcodeSerial?.split(",")
                val splittedMaterialBarcode = (cmpltMaterialBarcode?.get(0)?:0)

                // User input Material barcode value
                val inputCmpltMaterialBarcode = pickingMaterialTextValue.getText().toString()?.split(",")
                val splittedInputMaterialBarcode = (inputCmpltMaterialBarcode?.get(0)?:0)

                if (splittedInputMaterialBarcode == ""){
                    UiHelper.showErrorToast(this.activity as AppCompatActivity,
                            "Please enter Material Barcode value")
                }
                // if (item!!.materialBarcodeSerial == pickingMaterialTextValue.getText().toString()){
                 if (splittedMaterialBarcode == splittedInputMaterialBarcode){
                     matchFlag = true
                     rackMaterialTextValue.setText(item!!.rackBarcodeSerial)
                     binMaterialTextValue.setText(item!!.binBarcodeSerial)
                     picking_binBarcode.requestFocus()
                 }
            }
            if (matchFlag == false){
                UiHelper.showErrorToast(this.activity as AppCompatActivity,
                        "Entered Material Barcode Serial does not matched, check entered value")
            }
        }

        // On click on BIN Barcode scan button
        picking_bin_scanButton.setOnClickListener {
            // User input BIN barcode value
            val inputBinBarcode = binMaterialTextValue.getText().toString()
            if (inputBinBarcode == "") {
                UiHelper.showErrorToast(this.activity as AppCompatActivity,
                        "Please enter BIN Barcode value")
                picking_binBarcode.requestFocus()
            }else{
            picking_rackBarcode.requestFocus()}
        }

        // On click on RACK Barcode scan button
        picking_rack_scanButton.setOnClickListener {
            // User input BIN barcode value
            val inputRACKBarcode = rackMaterialTextValue.getText().toString()
            if (inputRACKBarcode == "") {
                UiHelper.showErrorToast(this.activity as AppCompatActivity,
                        "Please enter RACK Barcode value")
                picking_rackBarcode.requestFocus()
            }
        }

        picking_submitItemButton.setOnClickListener {
            var thisObject = this
            var foundFlag: Boolean = false
            viewModel.binBarcodeSerial = binMaterialTextValue.getText().toString()
            viewModel.materialBarcodeSerial = pickingMaterialTextValue.getText().toString()
            viewModel.rackBarcodeSerial = rackMaterialTextValue.getText().toString()

            // Log.d(TAG, "item value fragment -->"+viewModel.pickingScannedItems.value!!)
            for (item in viewModel.pickingScannedItems.value!!) {
                    if (pickingMaterialTextValue.getText().toString() == item!!.materialBarcodeSerial &&
                            binMaterialTextValue.getText().toString() == item!!.binBarcodeSerial &&
                                rackMaterialTextValue.getText().toString() == item!!.rackBarcodeSerial){
                                UiHelper.showErrorToast(this.activity as AppCompatActivity, "Already Scanned item!!")
                                foundFlag = true
                        }
                    }

            if (foundFlag == false) {
                recyclerView.adapter = SimplePickingItemAdapter(recyclerView, viewModel.pickingItems, viewModel)
                if (pickingMaterialTextValue == null) {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, "Please scan the material!")
                        viewModel.messageContent = "Please scan the material"
                    } else {
                        GlobalScope.launch {
                            viewModel.handleSubmitPicking()
                        }
                        viewModel.loadPickingScannedItems()
                        viewModel.loadPickingItems()
                    }
                picking_materialBarcode.text?.clear()
                picking_binBarcode.text?.clear()
                picking_rackBarcode.text?.clear()
                picking_materialBarcode.requestFocus()
            }
        }

        //this.progress = UiHelper.showProgressIndicator(activity!!, "Loading dispatch slip Items")
        picking_materialBarcode.requestFocus()
    }
}

open class SimplePickingItemAdapter(private val recyclerView: androidx.recyclerview.widget.RecyclerView,
                                    private val pickingItems: LiveData<Array<PickingItems?>>,
                                    private val viewModel: PickingViewModel ) :
        androidx.recyclerview.widget.RecyclerView.Adapter<SimplePickingItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.picking_row, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()

        val pickingItems = pickingItems.value!![position]!!
        // Log.d(TAG, "position" + position)
        holder.itemView.setOnClickListener{

            if (viewModel.pickingItems.toString().toLowerCase().contains("complete")) {
                return@setOnClickListener
            }
        }

    }

    override fun getItemCount(): Int {
        return pickingItems.value?.size ?: 0
    }


    open inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        protected val rackBarcodeSerial: TextView
        protected val binBarcodeSerial: TextView
        protected val materialBarcodeSerial: TextView
        protected val linearLayout: LinearLayout



        init {
            // Log.d(TAG, "..............rack_barcode" + R.id.rack_barcode)
            rackBarcodeSerial = itemView.findViewById(R.id.rack_barcode)
            binBarcodeSerial = itemView.findViewById(R.id.bin_barcode)
            materialBarcodeSerial = itemView.findViewById(R.id.material_barcode)
            linearLayout = itemView.findViewById(R.id.dispatch_slip_layout)

        }

        fun bind() {
            val pickingItems = pickingItems.value!![adapterPosition]!!
            // Log.d(TAG, "..............." + pickingItems.toString())

            rackBarcodeSerial.text = pickingItems.rackBarcodeSerial
            binBarcodeSerial.text = pickingItems.binBarcodeSerial

            val barcodeComplete = pickingItems.materialBarcodeSerial
            val barcodeValue = barcodeComplete?.split(",");

            val scannedMaterialBarcodeValue = viewModel.materialBarcodeSerial
            val scannedSplitedValue = scannedMaterialBarcodeValue?.split(",")

            materialBarcodeSerial.text = (barcodeValue?.get(0) ?:"NA")

            if (viewModel.rackBarcodeSerial == pickingItems!!.rackBarcodeSerial  &&
                    viewModel.binBarcodeSerial == pickingItems!!.binBarcodeSerial &&
                    (scannedSplitedValue?.get(0) ?: "NA") == (barcodeValue?.get(0) ?: "NA")){
                linearLayout.setBackgroundColor(PrefConstants().lightGreenColor)
            }else{
                linearLayout.setBackgroundColor(PrefConstants().lightGrayColor)
            }
        }
    }
}

