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
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import tena.health.care.MainActivity
import tena.health.care.R
import tena.health.care.interfaces.ActivityActionListener
import tena.health.care.models.User
import tena.health.care.screens.cart.CartScreen
import tena.health.care.screens.notifications.NotificationListScreen
import tena.health.care.screens.orders.OrderListScreen
import tena.health.care.utils.USER_DETAILS
import tena.health.care.utils.loadImageFromUrl
import tena.health.care.utils.loadScreen
import tena.health.care.utils.prefs

class ProfileScreen : Fragment() {

    private lateinit var backBtnHolder: LinearLayout
    private lateinit var personalInfoHolder: RelativeLayout
    private lateinit var addressHolder: RelativeLayout
    private lateinit var cartHolder: RelativeLayout
    private lateinit var ordersHolder: RelativeLayout
    private lateinit var notificationsHolder: RelativeLayout
    private lateinit var aboutUsHolder: RelativeLayout
    private lateinit var logOutHolder: RelativeLayout
    private lateinit var ivProfilePic: ImageView
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileBio: TextView
    private lateinit var profileHolder: LinearLayout

    lateinit var db:FirebaseFirestore
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
        return inflater.inflate(R.layout.fragment_profile_screen, container, false)
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

        personalInfoHolder = view.findViewById(R.id.personalInfoHolder)
        personalInfoHolder.setOnClickListener {
            loadScreen(requireActivity(), PersonalInfoScreen())
        }

        addressHolder = view.findViewById(R.id.addressHolder)
        addressHolder.setOnClickListener {
            loadScreen(requireActivity(), MyAddressScreen())
        }

        cartHolder = view.findViewById(R.id.cartHolder)
        cartHolder.setOnClickListener {
            loadScreen(requireActivity(), CartScreen())
        }

        ordersHolder = view.findViewById(R.id.ordersHolder)
        ordersHolder.setOnClickListener {
            loadScreen(requireActivity(), OrderListScreen())
        }

        notificationsHolder = view.findViewById(R.id.notificationsHolder)
        notificationsHolder.setOnClickListener {
            loadScreen(requireActivity(), NotificationListScreen())
        }

        aboutUsHolder = view.findViewById(R.id.aboutUsHolder)
        aboutUsHolder.setOnClickListener {
            loadScreen(requireActivity(), AboutUsScreen())
        }

        logOutHolder = view.findViewById(R.id.logOutHolder)
        logOutHolder.setOnClickListener {
            // Sign out from Firebase
            prefs.remove(USER_DETAILS)
            prefs.remove(USER_DETAILS)
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish() // Finish the current activity
        }

        ivProfilePic = view.findViewById(R.id.ivProfilePic)
        tvProfileName = view.findViewById(R.id.tvProfileName)
        tvProfileBio = view.findViewById(R.id.tvProfileBio)
        profileHolder = view.findViewById(R.id.profileHolder)

        profileHolder.setOnClickListener {
            loadScreen(requireActivity(), PersonalInfoScreen())
        }

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
                loadImageFromUrl(requireContext(),user?.profilePic?:"",ivProfilePic)
            } else {
                Log.e("Firestore", "PersonalInfo data is null")
            }
        }

    }

}