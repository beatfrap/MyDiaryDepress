package com.masbin.myhealth.ui.signin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService
import com.masbin.myhealth.R
import kotlinx.android.synthetic.main.activity_account.*
import kotlinx.android.synthetic.main.fragment_profile.*
import okhttp3.*

class AccountActivity : AppCompatActivity() {

    private val TAG = "HuaweiIdActivity"
    private lateinit var mAuthManager: HuaweiIdAuthService
    private lateinit var mAuthParam: HuaweiIdAuthParams
    private val REQUEST_SIGN_IN_LOGIN = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        supportActionBar?.hide()
    }



}
