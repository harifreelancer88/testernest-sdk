package com.testernest.example

import android.os.Bundle
import android.util.Log
import com.facebook.react.ReactActivity

class MainActivity : ReactActivity() {
  override fun getMainComponentName(): String = "TesternestExample"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Log.i("TesternestRNExample", "MainActivity onCreate")
  }

  override fun onResume() {
    super.onResume()
    Log.i("TesternestRNExample", "MainActivity onResume")
  }
}
