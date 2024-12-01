package tena.health.care.screens.offlineCustomer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tena.health.care.R
import tena.health.care.models.OfflineCustomer

class OfflineCustomerDeleteBottomSheet(offlineCustomer : OfflineCustomer): BottomSheetDialogFragment() {

    private var offlineCustomer: OfflineCustomer
    init {
        this.offlineCustomer = offlineCustomer
    }

    private lateinit var btnNo: Button
    private lateinit var btnRemoveOfflineCustomer: Button

    // Define the interface
    interface OnButtonClickListener {
        fun onButtonClicked(offlineCustomer : OfflineCustomer)
    }

    private var listener: OnButtonClickListener? = null

    // Attach the listener in onAttach
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = targetFragment as OnButtonClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnButtonClickListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.delete_offline_customer_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up views and listeners here
        btnNo = view.findViewById<Button>(R.id.btnNo)
        btnNo.setOnClickListener {
            // Handle button click
            dismiss() // Dismiss the BottomSheet
        }

        btnRemoveOfflineCustomer = view.findViewById<Button>(R.id.btnRemoveproduct)
        btnRemoveOfflineCustomer.setOnClickListener {
            listener?.onButtonClicked(offlineCustomer)
            dismiss() // Dismiss the BottomSheet
        }
    }

    // Clean up listener to avoid memory leaks
    override fun onDetach() {
        super.onDetach()
        listener = null
    }

}