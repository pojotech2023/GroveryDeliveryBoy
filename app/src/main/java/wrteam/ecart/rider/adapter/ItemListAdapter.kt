package wrteam.ecart.rider.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import wrteam.ecart.rider.R
import wrteam.ecart.rider.adapter.ItemListAdapter.HolderItems
import wrteam.ecart.rider.helper.AppController.Companion.toTitleCase
import wrteam.ecart.rider.helper.Constant
import wrteam.ecart.rider.helper.Session
import wrteam.ecart.rider.model.Items

class ItemListAdapter(var activity: Activity, private var items: ArrayList<Items>) :
    RecyclerView.Adapter<HolderItems>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderItems {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.lyt_order_item_list, parent, false)
        return HolderItems(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: HolderItems, position: Int) {
        val items1 = items[position]
        holder.tvProductListName.text = items1.name
        holder.tvSize.text = "(" + items1.unit + ")"
        holder.tvProductListPrice.text = Session(activity)
            .getData(Constant.CURRENCY) + items1.price
        holder.tvProductListQty.text = items1.quantity
        holder.tvProductListSubTotal.text = Session(activity)
            .getData(Constant.CURRENCY) + items1.subtotal

        Picasso.get().load(items1.product_image)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(holder.imgProduct)

        if (items1.active_status.equals(
                "cancelled",
                ignoreCase = true
            ) || items1.active_status.equals("returned", ignoreCase = true)
        ) {
            holder.tvActiveStatus.text = toTitleCase(items1.active_status)
            holder.tvActiveStatus.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class HolderItems(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvSize: TextView = itemView.findViewById(R.id.tvSize)
        var tvProductListName: TextView = itemView.findViewById(R.id.tvProductListName)
        var tvProductListPrice: TextView = itemView.findViewById(R.id.tvProductListPrice)
        var tvProductListQty: TextView = itemView.findViewById(R.id.tvProductListQty)
        var tvProductListSubTotal: TextView = itemView.findViewById(R.id.tvProductListSubTotal)
        var tvActiveStatus: TextView = itemView.findViewById(R.id.tvActiveStatus)
        var imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)

    }
}