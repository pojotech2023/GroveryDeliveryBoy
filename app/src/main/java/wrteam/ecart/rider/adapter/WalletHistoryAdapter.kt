package wrteam.ecart.rider.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import wrteam.ecart.rider.R
import wrteam.ecart.rider.helper.Constant
import wrteam.ecart.rider.helper.Session
import wrteam.ecart.rider.model.WalletHistory

class WalletHistoryAdapter(var activity: Activity, private var histories: ArrayList<WalletHistory?>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    // for load more
    private val viewTypeItem = 0
    private val viewTypeLoading = 1
    private var isLoading = false
    var id: String? = "0"

    fun add(position: Int, item: WalletHistory?) {
        histories.add(position, item)
        notifyItemInserted(position)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == viewTypeItem) {
            WalletHistoryHolderItems(LayoutInflater.from(activity).inflate(R.layout.lyt_wallet_history_list, parent, false))
        } else {
            return ViewHolderLoading(LayoutInflater.from(activity).inflate(R.layout.item_progressbar, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return histories.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (histories[position] == null) viewTypeLoading else viewTypeItem
    }

    override fun getItemId(position: Int): Long {
        val product = histories[position]
        return if (product != null) product.id!!.toInt().toLong() else position.toLong()
    }

    internal class ViewHolderLoading(view: View) : RecyclerView.ViewHolder(view) {
        var progressBar: ProgressBar = view.findViewById(R.id.itemProgressbar)

    }

    class WalletHistoryHolderItems(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTxNo: TextView = itemView.findViewById(R.id.tvTxNo)
        var tvTxDateAndTime: TextView = itemView.findViewById(R.id.tvTxDateAndTime)
        var tvTxMessage: TextView = itemView.findViewById(R.id.tvTxMessage)
        var tvTxAmount: TextView = itemView.findViewById(R.id.tvTxAmount)
        var tvTxStatus: TextView = itemView.findViewById(R.id.tvTxStatus)
        var cardView: CardView = itemView.findViewById(R.id.cardView)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is WalletHistoryHolderItems) {
            val walletHistory = histories[position]
            id = walletHistory!!.id
            holder.tvTxNo.text = walletHistory.id
            holder.tvTxDateAndTime.text = walletHistory.date_created
            holder.tvTxMessage.text = walletHistory.message
            holder.tvTxAmount.text = activity.getString(R.string.amount_title,Session(activity).getData(Constant.CURRENCY),walletHistory.amount)
            when (walletHistory.status) {
                "SUCCESS" -> {
                    holder.tvTxStatus.text = walletHistory.status
                    holder.cardView.setBackgroundColor(activity.getColor(R.color.tx_success_bg))
                }
                "0" -> {
                    holder.tvTxStatus.text = "PENDING"
                    holder.cardView.setBackgroundColor(activity.getColor(R.color.shipped_status_bg))
                }
                "1" -> {
                    holder.tvTxStatus.text = "APPROVED"
                    holder.cardView.setBackgroundColor(activity.getColor(R.color.received_status_bg))
                }
                "2" -> {
                    holder.tvTxStatus.text = "CANCELLED"
                    holder.cardView.setBackgroundColor(activity.getColor(R.color.returned_and_cancel_status_bg))
                }
                else -> holder.cardView.setBackgroundColor(activity.getColor(R.color.tx_fail_bg))
            }
        } else if (holder is ViewHolderLoading) {
            holder.progressBar.isIndeterminate = true
        }
    }
}