package tena.health.care.screens.offlineCustomer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import tena.health.care.R
import tena.health.care.models.OfflineCustomer
import tena.health.care.models.User
import tena.health.care.utils.USER_DETAILS
import tena.health.care.utils.prefs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


class AddOfflineCustomer() : Fragment() {

    private lateinit var etOfflineCustomerName: EditText
    private lateinit var etMobile: EditText
    private lateinit var etEmail: EditText
    private lateinit var etDateOfBirth: EditText
    private lateinit var etSoldProduct: EditText
    private lateinit var btnStore: Button
    private lateinit var progressBar: LottieAnimationView
    private var type = ""
    private lateinit var ivBack: LinearLayout

    lateinit var db:FirebaseFirestore
    lateinit var offlineCustomerRef:CollectionReference
    lateinit var userDetails:User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        type = arguments?.getString("Type") ?: ""
        return inflater.inflate(R.layout.fragment_offline_customer_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userDetails = Gson().fromJson(prefs.get(USER_DETAILS, ""), User::class.java)
        db = FirebaseFirestore.getInstance()
        offlineCustomerRef = db.collection("users").document(userDetails.userId).collection("offline_customer")
        progressBar = view.findViewById<LottieAnimationView>(R.id.progressBar)
        etOfflineCustomerName = view.findViewById(R.id.etOfflineCustomerName)
        etMobile = view.findViewById(R.id.etMobile)
        etEmail = view.findViewById(R.id.etEmail)
        etSoldProduct = view.findViewById(R.id.etSoldProduct)
        etDateOfBirth = view.findViewById(R.id.etDateOfBirth)
        btnStore = view.findViewById(R.id.btnSave)

        btnStore.setOnClickListener {
            if (etOfflineCustomerName.text.toString() != "") {
                if (etMobile.text.toString() != "") {
                    if (etEmail.text.toString() != "") {
                        if (etDateOfBirth.text.toString() != "") {
                                if (etSoldProduct.text.toString() != "") {
                                    progressBar.visibility = View.VISIBLE
                                    addOfflineCustomer(
                                        OfflineCustomer(
                                            userId = UUID.randomUUID().toString(),
                                            name = etOfflineCustomerName.text.toString(),
                                            emailId = etEmail.text.toString(),
                                            mobileNo = etMobile.text.toString(),
                                            dateOfBirth = etDateOfBirth.text.toString(),
                                            soldProduct = etSoldProduct.text.toString(),
                                            addedDate = getCurrentDate()
                                        )
                                    )
                                } else {
                                    Snackbar.make(
                                        requireView(),
                                        "Please Enter Product Count",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                }
                        } else {
                            Snackbar.make(
                                requireView(),
                                "Please Enter DateOfBirth",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Snackbar.make(
                            requireView(),
                            "Please Enter Email Address",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Snackbar.make(
                        requireView(),
                        "Please Enter Mobile Number",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } else {
                Snackbar.make(requireView(), "Please Enter Offline Customer Name", Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        ivBack = view.findViewById(R.id.backBtnHolder)
        ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

    }

    fun addOfflineCustomer(offlineCustomer: OfflineCustomer) {
        offlineCustomerRef
            .document(offlineCustomer.userId)
            .set(offlineCustomer)
            .addOnSuccessListener {
                Snackbar.make(requireView(), "Offline Customer Added Successfully", Snackbar.LENGTH_LONG)
                    .show()
                progressBar.visibility = View.GONE
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Snackbar.make(
                    requireView(),
                    "Something went wrong try again later",
                    Snackbar.LENGTH_LONG
                ).show()
                progressBar.visibility = View.GONE
                parentFragmentManager.popBackStack()
            }
    }
    override fun onDestroyView() {
        super.onDestroyView()
    }

    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = Date()
        return sdf.format(date)
    }

}