package tena.health.care.screens.notifications.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import tena.health.care.R
import tena.health.care.models.Notification
import tena.health.care.models.Order
import tena.health.care.models.Total
import tena.health.care.screens.orders.OrderDetailsScreen
import tena.health.care.utils.loadScreen

class NotificationListAdapter(val context: Context, activity: FragmentActivity, private val items: List<Notification?>) :
    RecyclerView.Adapter<NotificationListAdapter.ItemViewHolder>() {

    var activity: FragmentActivity
    var db: FirebaseFirestore

    init {
        this.activity = activity
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rc_item_notification, parent, false)
        return ItemViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position]?: Notification())
    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(notification: Notification) {
            tvTitle.text = notification.notificationTitle
            tvContent.text = notification.notificationMessage
            tvDate.text = notification.receivedDate
        }
    }
}