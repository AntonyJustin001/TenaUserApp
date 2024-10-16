package tena.health.care.screens.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.android.gms.tasks.Tasks
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

class PersonalInfoScreen : Fragment() {

    private lateinit var backBtnHolder: LinearLayout
    private lateinit var ivProfilePic: ImageView
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileBio: TextView
    private lateinit var ivEdit: TextView
    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhoneNumber: TextView

    lateinit var db: FirebaseFirestore
    private var activityActionListener: ActivityActionListener? = null
    lateinit var userDetails: User

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
        return inflater.inflate(R.layout.fragment_personal_info_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        activityActionListener?.showOrHideCart(false)
        userDetails = Gson().fromJson(prefs.get(USER_DETAILS, ""), User::class.java)

        backBtnHolder = view.findViewById(R.id.backBtnHolder)
        backBtnHolder.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        ivProfilePic = view.findViewById(R.id.ivProfilePic)
        tvProfileName = view.findViewById(R.id.tvProfileName)
        tvProfileBio = view.findViewById(R.id.tvProfileBio)
        ivEdit = view.findViewById(R.id.ivEdit)
        tvFullName = view.findViewById(R.id.tvFullName)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvPhoneNumber = view.findViewById(R.id.tvPhoneNumber)

        val userRef = db.collection("users")
            .document(userDetails.userId)
        userRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                tvProfileName.text = user?.name
                tvProfileBio.text = user?.bio
                tvFullName.text = user?.name
                tvEmail.text = user?.emailId
                tvPhoneNumber.text = user?.mobileNo
                loadImageFromUrl(requireContext(), user?.profilePic?:"", ivProfilePic)
            } else {
                Log.e("Firestore", "PersonalInfo data is null")
            }
        }

        ivEdit.setOnClickListener {
            loadScreen(requireActivity(), EditProfileScreen())
        }

    }

}