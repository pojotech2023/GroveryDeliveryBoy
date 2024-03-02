package com.groger.rider.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.groger.rider.R
import com.groger.rider.activity.OrderDetailActivity
import com.groger.rider.helper.AppController
import com.groger.rider.helper.Constant
import com.groger.rider.model.OrderList

class OrderListAdapter(var activity: Activity, private var orderLists: ArrayList<OrderList?>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    // for load more
    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1
    private var isLoading = false
    var id: String? = "0"
    fun add(position: Int, item: OrderList?) {
        orderLists.add(position, item)
        notifyItemInserted(position)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            OrderHolderItems(LayoutInflater.from(activity).inflate(R.layout.lyt_order_list, parent, false))
        } else {
            ViewHolderLoading(LayoutInflater.from(activity).inflate(R.layout.item_progressbar, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return orderLists.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (orderLists[position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    override fun getItemId(position: Int): Long {
        val product = orderLists[position]
        return if (product != null) product.id!!.toInt().toLong() else position.toLong()
    }

    internal class ViewHolderLoading(view: View) : RecyclerView.ViewHolder(view) {
        var progressBar: ProgressBar = view.findViewById(R.id.itemProgressbar)

    }

    class OrderHolderItems(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        var tvCustomerMobile: TextView = itemView.findViewById(R.id.tvCustomerMobile)
        var tvCustomerOrderNo: TextView = itemView.findViewById(R.id.tvCustomerOrderNo)
        var tvCustomerPaymentMethod: TextView = itemView.findViewById(R.id.tvCustomerPaymentMethod)
        var tvCustomerOrderDate: TextView = itemView.findViewById(R.id.tvCustomerOrderDate)
        var tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        var cardView: CardView = itemView.findViewById(R.id.cardView)
        var lytOrderList: RelativeLayout = itemView.findViewById(R.id.lytOrderList)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is OrderHolderItems) {
            val orderList = orderLists[position]
            id = orderList!!.id
            holder.tvCustomerOrderNo.text = activity.getString(R.string.order_number,orderList.id)
            holder.tvCustomerOrderDate.text =
                activity.getString(R.string.order_on,orderList.date_added)
            holder.tvCustomerName.text = orderList.name
            holder.tvCustomerMobile.text = orderList.mobile
            if (orderList.payment_method == "cod") {
                holder.tvCustomerPaymentMethod.text = activity.getString(R.string.via,"C.O.D.")
            } else {
                holder.tvCustomerPaymentMethod.text =
                    activity.getString(R.string.via,orderList.payment_method)
            }
            if (orderList.active_status.equals(Constant.RECEIVED, ignoreCase = true)) {
                holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        activity,
                        R.color.received_status_bg
                    )
                )
            } else if (orderList.active_status.equals(Constant.PROCESSED, ignoreCase = true)) {
                holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        activity,
                        R.color.processed_status_bg
                    )
                )
            } else if (orderList.active_status.equals(Constant.SHIPPED, ignoreCase = true)) {
                holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        activity,
                        R.color.shipped_status_bg
                    )
                )
            } else if (orderList.active_status.equals(Constant.DELIVERED, ignoreCase = true)) {
                holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        activity,
                        R.color.delivered_status_bg
                    )
                )
            } else if (orderList.active_status.equals(
                    Constant.CANCELLED,
                    ignoreCase = true
                ) || orderList.active_status.equals(
                    Constant.RETURNED, ignoreCase = true
                )
            ) {
                holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        activity,
                        R.color.returned_and_cancel_status_bg
                    )
                )
            }
            holder.tvStatus.text = AppController.toTitleCase(orderList.active_status)
            holder.lytOrderList.setOnClickListener {
                Constant.Position_Value = position
                activity.startActivity(
                    Intent(activity, OrderDetailActivity::class.java).putExtra(
                        Constant.ORDER_ID, orderList.id
                    )
                )
            }
        } else if (holder is ViewHolderLoading) {
            holder.progressBar.isIndeterminate = true
        }
    }
}