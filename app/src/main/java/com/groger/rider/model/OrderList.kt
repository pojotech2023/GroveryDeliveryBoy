package com.groger.rider.model

import java.util.ArrayList

class OrderList {
    var id: String? = null
    var user_id: String? = null
    var name: String? = null
    var mobile: String? = null
    var delivery_charge: String? = null
    var total: String? = null
    var tax: String? = null
    var promo_discount: String? = null
    var wallet_balance: String? = null
    var discount: String? = null
    var qty: String? = null
    var final_total: String? = null
    var promo_code: String? = null
    var deliver_by: String? = null
    var payment_method: String? = null
    var address: String? = null
    var delivery_time: String? = null
    var active_status: String? = null
    var date_added: String? = null
    var latitude: String? = null
    var longitude: String? = null
    var items: ArrayList<Items>? = null
}