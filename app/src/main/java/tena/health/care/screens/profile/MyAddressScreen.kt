package tena.health.care.screens.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.tasks.Tasks
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import tena.health.care.MainActivity
import tena.health.care.R
import tena.health.care.adapter.HomeAdapter
import tena.health.care.dialog.CustomDialog
import tena.health.care.interfaces.ActivityActionListener
import tena.health.care.models.CartItem
import tena.health.care.models.HomeSlider
import tena.health.care.models.Product
import tena.health.care.models.User
import tena.health.care.screens.cart.CartManager
import tena.health.care.screens.cart.CartScreen
import tena.health.care.screens.home.HomeScreen
import tena.health.care.screens.orders.OrderListScreen
import tena.health.care.utils.USER_DETAILS
import tena.health.care.utils.isNetworkAvailable
import tena.health.care.utils.loadImageFromUrl
import tena.health.care.utils.loadScreen
import tena.health.care.utils.prefs

class MyAddressScreen : Fragment() {

    private lateinit var backBtnHolder: LinearLayout

    private lateinit var tvHomeAddress: TextView
    private lateinit var ivEditHome: ImageView
    private lateinit var ivRemoveHome: ImageView
    private lateinit var ivConfirmHomeAddress: ImageView
    private lateinit var ivClearHomeAddress: ImageView
    private lateinit var etHomeAddressHolder: LinearLayout
    private lateinit var etHomeAddress: EditText
    private lateinit var ivEditWork: ImageView
    private lateinit var ivRemoveWork: ImageView
    private lateinit var ivConfirmWorkAddress: ImageView
    private lateinit var ivClearWorkAddress: ImageView
    private lateinit var tvWorkAddress: TextView
    private lateinit var etWorkAddressHolder: LinearLayout
    private lateinit var etWorkAddress: EditText
    private lateinit var progressBar: LottieAnimationView


    lateinit var currentUserDetails:User
    lateinit var db:FirebaseFirestore
    private var activityActionListener: ActivityActionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ActivityActionListener) {
            activityActionListener = context
        } else {
            throw RuntimeException("$context must implement ActivityActionListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_address_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityActionListener?.showOrHideCart(false)

        backBtnHolder = view.findViewById(R.id.backBtnHolder)
        backBtnHolder.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        tvHomeAddress = view.findViewById(R.id.tvHomeAddress)
        ivEditHome = view.findViewById(R.id.ivEditHome)
        ivRemoveHome = view.findViewById(R.id.ivRemoveHome)
        ivConfirmHomeAddress = view.findViewById(R.id.ivConfirmHomeAddress)
        ivClearHomeAddress = view.findViewById(R.id.ivClearHomeAddress)
        etHomeAddressHolder = view.findViewById(R.id.etHomeAddressHolder)
        etHomeAddress = view.findViewById(R.id.etHomeAddress)
        ivEditWork = view.findViewById(R.id.ivEditWork)
        ivRemoveWork = view.findViewById(R.id.ivRemoveWork)
        ivConfirmWorkAddress = view.findViewById(R.id.ivConfirmWorkAddress)
        ivClearWorkAddress = view.findViewById(R.id.ivClearWorkAddress)
        tvWorkAddress = view.findViewById(R.id.tvWorkAddress)
        etWorkAddressHolder = view.findViewById(R.id.etWorkAddressHolder)
        etWorkAddress = view.findViewById(R.id.etWorkAddress)
        progressBar = view.findViewById(R.id.progressBar)

        currentUserDetails = Gson().fromJson(prefs.get(USER_DETAILS, ""), User::class.java)

        ivEditHome.setOnClickListener {
            etHomeAddressHolder.visibility = View.VISIBLE
            tvHomeAddress.visibility = View.GONE
            ivClearHomeAddress.visibility = View.VISIBLE
            ivConfirmHomeAddress.visibility = View.VISIBLE
            ivEditHome.visibility = View.GONE
            ivRemoveHome.visibility = View.GONE
        }

        ivRemoveHome.setOnClickListener {
            val updatedDetails = hashMapOf(
                "address" to "",
            )
            updateHomeAddressEmail(currentUserDetails.emailId, updatedDetails,
                onSuccessListener = {
                    progressBar.visibility = View.GONE
                    etHomeAddressHolder.visibility = View.VISIBLE
                    tvHomeAddress.visibility = View.GONE
                    ivClearHomeAddress.visibility = View.VISIBLE
                    ivConfirmHomeAddress.visibility = View.VISIBLE
                    ivEditHome.visibility = View.GONE
                    ivRemoveHome.visibility = View.GONE
                    etHomeAddress.setText("")
                    tvHomeAddress.text = ""
                },
                onFailureListener = {
                    progressBar.visibility = View.GONE
                    tvHomeAddress.text = etHomeAddress.text
                    tvHomeAddress.visibility = View.GONE
                    etHomeAddressHolder.visibility = View.VISIBLE
                })
        }

        ivConfirmHomeAddress.setOnClickListener {
            val updatedDetails = hashMapOf(
                "address" to etHomeAddress.text.toString(),
            )
            updateHomeAddressEmail(currentUserDetails.emailId, updatedDetails,
                onSuccessListener = {
                    progressBar.visibility = View.GONE
                    tvHomeAddress.text = etHomeAddress.text
                    tvHomeAddress.visibility = View.VISIBLE
                    etHomeAddressHolder.visibility = View.GONE
                },
                onFailureListener = {
                    progressBar.visibility = View.GONE
                    tvHomeAddress.text = etHomeAddress.text
                    tvHomeAddress.visibility = View.GONE
                    etHomeAddressHolder.visibility = View.VISIBLE
                })
        }

        ivClearHomeAddress.setOnClickListener {
            etHomeAddress.setText("")
            tvHomeAddress.text = ""
            tvHomeAddress.visibility = View.GONE
            etHomeAddressHolder.visibility = View.VISIBLE
            ivClearHomeAddress.visibility = View.VISIBLE
            ivConfirmHomeAddress.visibility = View.VISIBLE
            ivEditHome.visibility = View.GONE
            ivRemoveHome.visibility = View.GONE
        }




    }

    fun updateHomeAddressEmail(emailId: String, updatedDetails: Map<String, Any>) {
        val db = FirebaseFirestore.getInstance()
        progressBar.visibility = View.VISIBLE
        db.collection("users")
            .whereEqualTo("emailId", emailId) // Query where name matches
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    for (document in documents) {
                        // Update the document found with new details
                        db.collection("users")
                            .document(emailId)
                            .update(updatedDetails)
                            .addOnSuccessListener {
                                Snackbar.make(requireView(), "product Edited Successfully", Snackbar.LENGTH_LONG).show()

                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error updating document", e)
                                Snackbar.make(requireView(), "Something went wrong please try again", Snackbar.LENGTH_LONG).show()
                                progressBar.visibility = View.GONE
                                tvHomeAddress.text = etHomeAddress.text
                                tvHomeAddress.visibility = View.GONE
                                etHomeAddressHolder.visibility = View.VISIBLE
                            }
                    }
                } else {
                    Log.d("Firestore", "No such document found!")
                    Snackbar.make(requireView(), "Something went wrong please try again", Snackbar.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                    tvHomeAddress.text = etHomeAddress.text
                    tvHomeAddress.visibility = View.GONE
                    etHomeAddressHolder.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
                Snackbar.make(requireView(), "Something went wrong please try again", Snackbar.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
                tvHomeAddress.text = etHomeAddress.text
                tvHomeAddress.visibility = View.GONE
                etHomeAddressHolder.visibility = View.VISIBLE
            }
    }

    fun updateHomeAddressEmail(emailId: String, updatedDetails: Map<String, Any>,onSuccessListener: () -> Unit = {}
                               ,onFailureListener: () -> Unit = {}) {
        val db = FirebaseFirestore.getInstance()
        progressBar.visibility = View.VISIBLE
        db.collection("users")
            .whereEqualTo("emailId", emailId) // Query where name matches
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    for (document in documents) {
                        // Update the document found with new details
                        db.collection("users")
                            .document(emailId)
                            .update(updatedDetails)
                            .addOnSuccessListener {
                                Snackbar.make(requireView(), "product Edited Successfully", Snackbar.LENGTH_LONG).show()
                                onSuccessListener()
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error updating document", e)
                                Snackbar.make(requireView(), "Something went wrong please try again", Snackbar.LENGTH_LONG).show()
                                onFailureListener()
                            }
                    }
                } else {
                    Log.d("Firestore", "No such document found!")
                    Snackbar.make(requireView(), "Something went wrong please try again", Snackbar.LENGTH_LONG).show()
                    onFailureListener()
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
                Snackbar.make(requireView(), "Something went wrong please try again", Snackbar.LENGTH_LONG).show()
                onFailureListener()
            }
    }

}