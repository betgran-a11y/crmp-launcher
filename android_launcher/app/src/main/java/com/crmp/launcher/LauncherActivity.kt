package com.crmp.launcher

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.crmp.launcher.databinding.ActivityLauncherBinding
import com.crmp.launcher.network.ApiService
import java.util.concurrent.Executors

class LauncherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLauncherBinding
    private val executor = Executors.newSingleThreadExecutor()

    companion object {
        // Package name вашей основной Unity-игры
        private const val UNITY_GAME_PACKAGE = "com.crmp.game3d"
        private const val SERVER_ADDR = "127.0.0.1:7777"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nick = intent.getStringExtra("nickname").orEmpty()
        val token = intent.getStringExtra("token").orEmpty()

        binding.tvPlayerNick.text = nick
        binding.tvServerInfo.text = "Сервер #1 • $SERVER_ADDR"

        checkGameVersion()

        binding.btnPlay.setOnClickListener {
            val launchIntent = packageManager.getLaunchIntentForPackage(UNITY_GAME_PACKAGE)
            if (launchIntent != null) {
                // Передаём токен, сервер и ник напрямую в Unity-клиент
                launchIntent.putExtra("token", token)
                launchIntent.putExtra("server", SERVER_ADDR)
                launchIntent.putExtra("nickname", nick)
                startActivity(launchIntent)
            } else {
                Toast.makeText(
                    this,
                    "APK игры ($UNITY_GAME_PACKAGE) не установлен на устройстве.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.btnLogout.setOnClickListener {
            getSharedPreferences("crmp_auth", MODE_PRIVATE).edit().clear().apply()
            startActivity(Intent(this, MainActivity::class.java).putExtra("force_auth", true))
            finish()
        }
    }

    private fun checkGameVersion() {
        executor.execute {
            val info = ApiService.fetchVersion()
            runOnUiThread {
                binding.tvCacheStatus.text = if (info != null) {
                    "Актуальная версия клиента: ${info.latestVersion}"
                } else {
                    "Не удалось проверить версию (API недоступен)"
                }
            }
        }
    }
}
