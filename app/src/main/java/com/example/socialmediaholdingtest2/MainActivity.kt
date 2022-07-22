package com.example.socialmediaholdingtest2

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.socialmediaholdingtest2.ui.theme.SocialMediaHoldingTest2Theme
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewNavigator
import com.google.accompanist.web.rememberWebViewState

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private var canBack = false
    private lateinit var shPref: SharedPreferences
    private lateinit var currentUrl: MutableState<String>

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shPref = getSharedPreferences("prefs", Context.MODE_PRIVATE)

        val packageManager = this.packageManager
        setContent {
            SocialMediaHoldingTest2Theme {
                // A surface container using the 'background' color from the theme
                val nav = rememberWebViewNavigator()
                canBack = nav.canGoBack

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    currentUrl = rememberSaveable {
                        mutableStateOf(
                            if (shPref.contains("link")) shPref.getString(
                                "link",
                                ""
                            )!! else "https://yandex.ru/"
                        )
                    }
                    val state = rememberWebViewState(url = currentUrl.value)
                    val webClient = remember {

                        object : AccompanistWebViewClient() {
                            override fun onPageStarted(
                                view: WebView?,
                                url: String?,
                                favicon: Bitmap?
                            ) {
                                super.onPageStarted(view, url, favicon)
                                Log.d(TAG, "onPageStarted: $url")
                                when (url) {
                                    "https://yandex.ru/maps/?utm_source=main_new" -> {
                                        val uri = Uri.parse("yandexmaps://maps.yandex.ru/")
                                        try {
                                            startActivity(Intent(Intent.ACTION_VIEW, uri))
                                            Log.d(TAG, "onPageStarted: try")
                                            currentUrl.value = "https://yandex.ru/"
                                        } catch (e: java.lang.Exception) {
                                            val uriBrowser = Uri.parse(url)
                                            startActivity(Intent(Intent.ACTION_VIEW, uriBrowser))
                                            Log.d(TAG, "onPageStarted: catch")
                                            currentUrl.value = "https://yandex.ru/"
                                        }
                                    }
                                    "https://yandex.ru/pogoda/213?utm_source=home&utm_content=main_informer&utm_medium=web&utm_campaign=informer" -> {
                                        val intent = Intent()
                                        intent.setPackage("ru.yandex.weatherplugin")
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        try {
                                            startActivity(intent)
                                            Log.d(TAG, "onPageStarted: succues")
                                            currentUrl.value = "https://yandex.ru/"
                                        } catch (e: Exception) {
                                            val uriWeather = Uri.parse("https://yandex.ru/pogoda/213?utm_source=home&utm_content=main_informer&utm_medium=web&utm_campaign=informer")
                                            Log.d(TAG, "onPageStarted: error")
                                            startActivity(Intent(Intent.ACTION_VIEW, uriWeather))
                                            currentUrl.value = "https://yandex.ru/"
                                        }

                                    }
                                }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                currentUrl = mutableStateOf(url ?: "https://yandex.ru/")
                            }
                        }
                    }
                    WebView(
                        state = state,
                        modifier = Modifier.fillMaxSize(),
                        onCreated = {
                            it.settings.javaScriptEnabled = true
                            it.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                            CookieManager.getInstance().setAcceptCookie(true)
                            CookieManager.getInstance().setAcceptThirdPartyCookies(it, true)
                        },
                        navigator = nav,
                        client = webClient
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        if (canBack) {
            super.onBackPressed()
        } else {
            AlertDialog.Builder(this)
                .setMessage("Вы действительно хотите покинуть программу?")
                .setCancelable(false)
                .setPositiveButton(
                    "Да",
                    { _: DialogInterface, _: Int -> super.onBackPressed() }
                )
                .setNegativeButton("Нет", null).show();
        }
    }

    override fun onStop() {
        super.onStop()
        shPref.edit().putString("link", currentUrl.value).apply()
        Log.d(TAG, "onDestroy: ")
    }
}


