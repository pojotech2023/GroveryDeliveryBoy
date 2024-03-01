package wrteam.ecart.rider.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import wrteam.ecart.rider.R
import wrteam.ecart.rider.model.Notification

class NotificationAdapter(var activity: Activity, private var notifications: ArrayList<Notification?>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    // for load more
    private val viewTypeItem = 0
    private val viewTypeLoading = 1
    private var isLoading = false
    var id: String? = "0"
    private var showMore = false

    fun add(position: Int, item: Notification?) {
        notifications.add(position, item)
        notifyItemInserted(position)
    }

    fun setLoaded() {
        isLoading = false
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == viewTypeItem) {
            NotificationHolderItems(LayoutInflater.from(activity).inflate(R.layout.lyt_notification_list, parent, false))
        } else {
            ViewHolderLoading(LayoutInflater.from(activity).inflate(R.layout.item_progressbar, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (notifications[position] == null) viewTypeLoading else viewTypeItem
    }

    override fun getItemId(position: Int): Long {
        val product = notifications[position]
        return if (product != null) product.id!!.toInt().toLong() else position.toLong()
    }

    internal class ViewHolderLoading(view: View) : RecyclerView.ViewHolder(view) {
        var progressBar: ProgressBar = view.findViewById(R.id.itemProgressbar)

    }

    class NotificationHolderItems(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        var tvMessageMore: TextView = itemView.findViewById(R.id.tvMessageMore)
        var tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
        var tvShowMore: TextView = itemView.findViewById(R.id.tvShowMore)
        var lytNotification: LinearLayout = itemView.findViewById(R.id.lytNotification)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NotificationHolderItems) {
            val notification = notifications[position]
            id = notification!!.id
            holder.tvTitle.text =
                activity.getString(R.string.order_number, notification.order_id)
            holder.tvMessage.text = notification.title
            holder.tvOrderDate.text = notification.date_created
            holder.tvMessageMore.text = notification.message

            holder.tvShowMore.setOnClickListener {
                if (!showMore) {
                    showMore = true
                    holder.tvMessageMore.visibility = View.GONE
                    holder.tvShowMore.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_show_more,
                        0
                    )
                } else {
                    showMore = false
                    holder.tvMessageMore.visibility = View.VISIBLE
                    holder.tvShowMore.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_show_less,
                        0
                    )
                }
            }

            holder.lytNotification.setOnClickListener {
                if (!showMore) {
                    showMore = true
                    holder.tvMessageMore.visibility = View.GONE
                    holder.tvShowMore.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_show_more,
                        0
                    )
                } else {
                    showMore = false
                    holder.tvMessageMore.visibility = View.VISIBLE
                    holder.tvShowMore.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_show_less,
                        0
                    )
                }
            }
        } else if (holder is ViewHolderLoading) {
            holder.progressBar.isIndeterminate = true
        }
    }
}