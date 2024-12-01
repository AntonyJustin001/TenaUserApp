package tena.health.care.screens.offlineCustomer

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.firestore.FirebaseFirestore
import tena.health.care.R
import tena.health.care.models.OfflineCustomer
import tena.health.care.screens.offlineCustomer.adapter.OfflineCustomerDetailsAdapter

class OfflineCustomerDetailsScreen(customerId:String) : Fragment() {

    private lateinit var backBtnHolder: LinearLayout
    private lateinit var rcOfflineCustomerDetails: RecyclerView
    lateinit var db: FirebaseFirestore
    private lateinit var progressBar: LottieAnimationView
    private var customerId = ""

    init {
        this.customerId = customerId
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_offline_customer_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        rcOfflineCustomerDetails = view.findViewById(R.id.rcOfflineCustomerDetails)
        rcOfflineCustomerDetails.layoutManager = LinearLayoutManager(context)
        backBtnHolder = view.findViewById(R.id.backBtnHolder)
        backBtnHolder.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        progressBar = view.findViewById(R.id.progressBar)

        val offlineCustomerDetailRef = db.collection("offline_customer").document(customerId)
        offlineCustomerDetailRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e)
                return@addSnapshotListener
            }


            if (snapshot != null && snapshot.exists()) {

                val customer = snapshot.toObject(OfflineCustomer::class.java)
                Log.e("Firestore", "customer Details - $customer")
                if(isAdded) {
                    if(customer != null){
                        rcOfflineCustomerDetails.adapter = OfflineCustomerDetailsAdapter(
                            requireContext(),
                            requireActivity(),
                            customer
                        )
                    } else {
                        Log.e("Firestore", "customer data is null")
                        //requireActivity().supportFragmentManager.popBackStack()
                    }
                }
                progressBar.visibility  = View.GONE

            } else {
                Log.e("Firestore", "customer data is null")
            }
        }
    }
}