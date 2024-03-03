package com.groger.rider.helper

object Constant {

    //MODIFICATION PART

    //Admin panel url with it would be necessary to put "/"(slash) at end of the url (https://admin.panel.url/)
    //private var BASE_URL = "admin_panel_url_here"
    private var BASE_URL = "https://grocery.pojotech.in/delivery-boy/api/"

    //set your jwt secret key here...key must same in PHP and Android
    var JWT_KEY = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3MDk0MzkxNDAsImlzcyI6ImVLYXJ0IiwiZXhwIjoxNzA5NDQwOTQwLCJzdWIiOiJlS2FydCBBdXRoZW50aWNhdGlvbiIsIndlYiI6IjI5MjYwNDE4IiwiY3VzdCI6IjIyMDE1OTExIiwiYWRtaW4iOiIzMDI4ODQ0MiJ9.7SMYGkJSX-8Pmzjn0rMemzXpXoV4NCWn4PcFowmlhHE"

    var LANGUAGE: String = "en"

    const val LOAD_ITEM_LIMIT: Int = 10


    //MODIFICATION PART END

    private const val SUB_URL = "delivery-boy/"
    val MAIN_URL = BASE_URL + SUB_URL + "api/api-v1.php"
    val DELIVERY_BOY_POLICY = BASE_URL + "delivery-boy-play-store-privacy-policy.php"
    val DELIVERY_BOY_TERMS = BASE_URL + "delivery-boy-play-store-terms-conditions.php"
    const val AccessKey = "accesskey"
    const val AccessKeyVal = "90336"
    const val GetVal = "1"
    const val LOGIN = "login"
    const val GET_DELIVERY_BOY_BY_ID = "get_delivery_boy_by_id"
    const val UPDATE_DELIVERY_BOY_IS_AVAILABLE = "update_delivery_boy_is_available"
    const val IS_AVAILABLE = "is_available"
    const val GET_ORDERS_BY_DELIVERY_BOY_ID = "get_orders_by_delivery_boy_id"
    const val UPDATE_DELIVERY_BOY_PROFILE = "update_delivery_boy_profile"
    const val UPDATE_ORDER_STATUS = "update_order_status"
    const val GET_WITHDRAWAL_REQUEST = "get_withdrawal_requests"
    const val SEND_WITHDRAWAL_REQUEST = "send_withdrawal_request"
    const val TYPE = "type"
    const val DELIVERY_BOY = "delivery_boy"
    const val DELIVERY_BOY_FORGOT_PASSWORD = "delivery_boy_forgot_password"
    const val GET_NOTIFICATION = "get_notifications"
    const val UPDATE_DELIVERY_BOY_FCM_ID = "update_delivery_boy_fcm_id"
    const val CHECK_DELIVERY_BOY_BY_MOBILE = "check_delivery_boy_by_mobile"
    const val ID = "id"
    var filtervalues = arrayOf<CharSequence>("Show Wallet Transactions", "Show Wallet Requests")
    const val WITHDRAWAL_REQUEST = "withdrawal_requests"
    const val FUND_TRANSFERS = "fund_transfers"
    const val TYPE_ID = "type_id"
    const val DELIVERY_BOY_ID = "delivery_boy_id"
    const val NAME = "name"
    const val MOBILE = "mobile"
    const val PASSWORD = "password"
    const val ADDRESS = "address"
    const val BONUS = "bonus"
    const val BALANCE = "balance"
    const val CURRENCY = "currency"
    const val UPDATED_BALANCE = "updated_balance"
    const val STATUS = "status"
    const val CREATED_AT = "date_created"
    const val DATA = "data"
    const val DATA_TYPE = "data_type"
    const val TOTAL = "total"
    const val MESSAGE = "message"
    const val AMOUNT = "amount"
    const val FROM = "from"
    const val ORDER_ID = "order_id"
    const val FCM_ID = "fcm_id"
    const val DELIVERY_CHARGE = "delivery_charge"
    const val DELIVERY_TIME = "delivery_time"
    const val LATITUDE = "latitude"
    const val LONGITUDE = "longitude"
    const val PAYMENT_METHOD = "payment_method"
    const val FINAL_TOTAL = "final_total"
    const val DISCOUNT = "discount"
    const val PROMO_DISCOUNT = "promo_discount"
    const val TAX = "tax"
    const val DATE_ADDED = "date_added"
    const val OTP = "otp"
    const val STR_WALLET_BALANCE = "wallet_balance"
    const val ITEMS = "items"
    const val OFFSET = "offset"
    const val LIMIT = "limit"
    const val ACTIVE_STATUS = "active_status"
    const val RECEIVED = "Received"
    const val PROCESSED = "Processed"
    const val SHIPPED = "Shipped"
    const val DELIVERED = "Delivered"
    const val CANCELLED = "Cancelled"
    const val RETURNED = "Returned"
    const val SHOW = "show"
    const val HIDE = "hide"
    const val ERROR = "error"
    var PRODUCT_LOAD_LIMIT = "10"
    var Position_Value: Int = 0
    var CLICK = false
}