package tena.health.care.screens.notifications

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import tena.health.care.R
import tena.health.care.interfaces.ActivityActionListener
import tena.health.care.models.Notification
import tena.health.care.models.Order
import tena.health.care.screens.notifications.adapter.NotificationListAdapter
import tena.health.care.screens.orders.adapter.OrdersListAdapter

class NotificationListScreen : Fragment() {

    private lateinit var backBtnHolder: LinearLayout
    private lateinit var rcNotifications: RecyclerView
    lateinit var db: FirebaseFirestore
    lateinit var notificationsRef: CollectionReference
    private var activityActionListener: ActivityActionListener? = null
    private lateinit var progressBar: LottieAnimationView
    private lateinit var tvEmptyNotifications: TextView

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
        return inflater.inflate(R.layout.fragment_notificatios, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        rcNotifications = view.findViewById(R.id.rcNotifications)
        rcNotifications.layoutManager = LinearLayoutManager(context)

        backBtnHolder = view.findViewById(R.id.backBtnHolder)
        backBtnHolder.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        tvEmptyNotifications = view.findViewById(R.id.tvEmptyNotifications)
        activityActionListener?.showOrHideCart(false)
        progressBar = view.findViewById(R.id.progressBar)

        notificationsRef = db.collection("notifications")
        progressBar.visibility = View.VISIBLE
        notificationsRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                progressBar.visibility = View.GONE
                Log.w("Firestore", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshots != null) {
                val notifications = mutableListOf<Notification?>()
                for (document in snapshots.documents) {
                    val order = document.toObject(Notification::class.java)
                    notifications.add(order)
                }
                // Now cartItems holds the updated cart items
                Log.e("Firestore", "CartScreen - Real-time notifications: $notifications")
                if (isAdded) {
                    if (notifications.size > 0) {
                        rcNotifications.visibility = View.VISIBLE
                        tvEmptyNotifications.visibility = View.GONE
                        rcNotifications.adapter = NotificationListAdapter(
                            requireContext(),
                            requireActivity(),
                            notifications
                        )
                    } else {
                        rcNotifications.visibility = View.GONE
                        tvEmptyNotifications.visibility = View.VISIBLE
                    }
                    progressBar.visibility = View.GONE
                }
            }

        }
    }
}