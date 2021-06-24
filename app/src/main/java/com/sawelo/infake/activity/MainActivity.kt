package com.sawelo.infake.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.sawelo.infake.fragment.CreateFragment
import com.sawelo.infake.function.FlutterFunction
import com.sawelo.infake.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<CreateFragment>(R.id.fragment_container_view)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FlutterFunction().destroyFlutterEngine()
    }
}