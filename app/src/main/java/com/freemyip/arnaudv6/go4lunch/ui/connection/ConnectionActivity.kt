package com.freemyip.arnaudv6.go4lunch.ui.connection

import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import net.openid.appauth.AuthorizationService


@AndroidEntryPoint
class ConnectionActivity : AppCompatActivity() {
    private val viewModel: ConnectionViewModel by viewModels()

    private val authService = AuthorizationService(this)

    init {
        viewModel.authorizationRequestLiveData.observe(this) { authorizationRequest ->
            // startActivityForResult() being deprecated
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val value = it.data?.getStringExtra("input")
                }
            }.launch(authService.getAuthorizationRequestIntent(authorizationRequest))
        }
    }

}


