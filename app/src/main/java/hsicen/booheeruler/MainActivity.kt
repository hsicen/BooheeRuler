package hsicen.booheeruler

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hsicen.booheeruler.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.knlTopHead.bindRuler(binding.brBottomHead)

    binding.knlTopHead.configValue {
      textSize = 16f
    }

    binding.knlTopHead.configUnit {
      textSize = 12f
    }
  }
}