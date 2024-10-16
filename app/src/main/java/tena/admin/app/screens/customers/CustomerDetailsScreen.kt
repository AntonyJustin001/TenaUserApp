package tena.admin.app.screens.customers

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
import tena.admin.app.R
import tena.admin.app.models.Customer
import tena.admin.app.screens.customers.adapter.CustomerDetailsAdapter

class CustomerDetailsScreen(customerId:String) : Fragment() {

    private lateinit var backBtnHolder: LinearLayout
    private lateinit var rccustomerDetails: RecyclerView
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
        return inflater.inflate(R.layout.fragment_customer_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        rccustomerDetails = view.findViewById(R.id.rccustomerDetails)
        rccustomerDetails.layoutManager = LinearLayoutManager(context)
        backBtnHolder = view.findViewById(R.id.backBtnHolder)
        backBtnHolder.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        progressBar = view.findViewById(R.id.progressBar)

        val customerDetailRef = db.collection("users").document(customerId)
        customerDetailRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e)
                return@addSnapshotListener
            }


            if (snapshot != null && snapshot.exists()) {

                val customer = snapshot.toObject(Customer::class.java)
                Log.e("Firestore", "customer Details - $customer")
                if(isAdded) {
                    if(customer != null){
                        rccustomerDetails.adapter = CustomerDetailsAdapter(
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