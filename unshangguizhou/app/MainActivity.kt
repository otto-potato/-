package com.yunshangguizhou.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.yunshangguizhou.app.ui.navigation.NavGraph
import com.yunshangguizhou.app.ui.theme.YunShangGuiZhouTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val locationPerms = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    private val notifPerms = if (Build.VERSION.SDK_INT >= 33) arrayOf(Manifest.permission.POST_NOTIFICATIONS) else emptyArray()

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* silently accept */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
        setContent {
            YunShangGuiZhouTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    NavGraph(rememberNavController())
                }
            }
        }
    }

    private fun requestPermissions() {
        val needLoc = locationPerms.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        val needNotif = notifPerms.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        val all = mutableListOf<String>()
        if (needLoc && !isPermanentlyDenied(locationPerms)) all.addAll(locationPerms)
        if (needNotif) all.addAll(notifPerms)
        if (all.isNotEmpty()) permLauncher.launch(all.toTypedArray())
    }

    private fun isPermanentlyDenied(perms: Array<String>): Boolean {
        return perms.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
                && !shouldShowRequestPermissionRationale(it)
        }
    }
}
