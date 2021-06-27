package com.sawelo.infake.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.sawelo.infake.R
import com.sawelo.infake.fragment.CreateFragment
import com.sawelo.infake.function.FlutterFunction

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Prevent Night Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<CreateFragment>(R.id.activity_main_fragment_container)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FlutterFunction().destroyFlutterEngine()
    }
}