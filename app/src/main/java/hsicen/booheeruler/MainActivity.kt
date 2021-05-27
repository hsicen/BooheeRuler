package hsicen.booheeruler

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hsicen.extension.log.KLog
import hsicen.booheeruler.databinding.ActivityMainBinding
import hsicen.ruler.RulerCallback

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
      //binding.brBottomHead.limitMinScale = 3
      //binding.brBottomHead.limitMaxScale = 27
      binding.brBottomHead.refreshRuler()
    }

    binding.knlTopHead.addCallback(object : RulerCallback {
      override fun onScaleChanging(scale: Float) {
        KLog.d("主滑动中：$scale")
      }

      override fun afterScaleChanged(scale: Float) {
        KLog.d("主滑动结束: $scale")
      }
    })
  }
}