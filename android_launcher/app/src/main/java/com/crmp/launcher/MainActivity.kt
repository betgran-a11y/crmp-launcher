package com.crmp.launcher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.crmp.launcher.databinding.ActivityMainBinding
import com.crmp.launcher.network.ApiService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isRegisterMode = true
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("crmp_auth", MODE_PRIVATE)
        val savedToken = prefs.getString("token", null)
        val savedNick = prefs.getString("nickname", null)
        if (!savedToken.isNullOrEmpty() && !savedNick.isNullOrEmpty() && !intent.getBooleanExtra("force_auth", false)) {
            openLauncher(savedNick, savedToken)
            return
        }

        binding.btnModeRegister.setOnClickListener {
            isRegisterMode = true
            updateMode()
        }
        binding.btnModeLogin.setOnClickListener {
            isRegisterMode = false
            updateMode()
        }

        binding.btnSubmit.setOnClickListener { submitAuth() }
        updateMode()
    }

    private fun updateMode() {
        binding.tvTitle.text = if (isRegisterMode) "Регистрация" else "Вход в игру"
        binding.tilConfirmPassword.visibility = if (isRegisterMode) View.VISIBLE else View.GONE
        binding.tilEmail.visibility = if (isRegisterMode) View.VISIBLE else View.GONE
        binding.tilReferral.visibility = if (isRegisterMode) View.VISIBLE else View.GONE
        binding.btnSubmit.text = if (isRegisterMode) "Создать аккаунт" else "Войти"
    }

    private fun submitAuth() {
        val nick = binding.etNick.text?.toString()?.trim().orEmpty()
        val pass = binding.etPassword.text?.toString().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val ref = binding.etReferral.text?.toString()?.trim().orEmpty()

        if (isRegisterMode && pass != binding.etConfirmPassword.text?.toString().orEmpty()) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSubmit.isEnabled = false
        executor.execute {
            val result = if (isRegisterMode) {
                ApiService.register(nick, email, pass, ref)
            } else {
                ApiService.login(nick, pass)
            }
            runOnUiThread {
                binding.btnSubmit.isEnabled = true
                if (result.success) {
                    getSharedPreferences("crmp_auth", MODE_PRIVATE).edit()
                        .putString("token", result.token)
                        .putString("nickname", result.nickname)
                        .apply()
                    openLauncher(result.nickname, result.token)
                } else {
                    Toast.makeText(this, result.error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun openLauncher(nick: String, token: String) {
        val intent = Intent(this, LauncherActivity::class.java).apply {
            putExtra("nickname", nick)
            putExtra("token", token)
        }
        startActivity(intent)
        finish()
    }
}
