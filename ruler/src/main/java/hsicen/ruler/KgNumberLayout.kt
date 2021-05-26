package hsicen.ruler

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.hsicen.extension.extensions.color
import com.hsicen.extension.extensions.sp2px
import hsicen.ruler.databinding.LayoutKgNumberBinding

/**
 * 作者：hsicen  5/25/21 16:57
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：用于包着显示具体数字刻度Layout
 */
class KgNumberLayout @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), RulerCallback {
  private lateinit var binding: LayoutKgNumberBinding

  //字体大小
  private var mScaleTextSize = 12f.sp2px
  private var mKgTextSize = 12f.sp2px

  //字体颜色
  private var mScaleTextColor = color(R.color.yellow)
  private var mKgTextColor = color(R.color.yellow)
  private var scaleSpecail = false

  //kg单位文字
  private var mUnitText = "kg"
  private var mRuler: BooheeRuler? = null
  private val mCallbacks = ArrayList<RulerCallback>()

  init {
    attrs?.let {
      val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.KgNumberLayout, 0, 0)
      mScaleTextSize = typedArray.getDimension(R.styleable.KgNumberLayout_scaleTextSize, mScaleTextSize)
      mKgTextSize = typedArray.getDimension(R.styleable.KgNumberLayout_kgTextSize, mKgTextSize)
      mScaleTextColor = typedArray.getColor(R.styleable.KgNumberLayout_scaleTextColor, mScaleTextColor)
      mKgTextColor = typedArray.getColor(R.styleable.KgNumberLayout_kgTextColor, mKgTextColor)
      mUnitText = typedArray.getString(R.styleable.KgNumberLayout_kgUnitText) ?: mUnitText
      scaleSpecail = typedArray.getBoolean(R.styleable.KgNumberLayout_scaleSpecial, false)

      typedArray.recycle()
    }
    init(context)
  }

  private fun init(context: Context) {
    val rootView = LayoutInflater.from(context).inflate(R.layout.layout_kg_number, this)
    binding = LayoutKgNumberBinding.bind(rootView)

    binding.tvScale.setTextSize(TypedValue.COMPLEX_UNIT_PX, mScaleTextSize)
    binding.tvScale.setTextColor(mScaleTextColor)
    binding.tvKg.setTextSize(TypedValue.COMPLEX_UNIT_PX, mKgTextSize)
    binding.tvKg.setTextColor(mKgTextColor)
    binding.tvKg.text = mUnitText
  }

  override fun onScaleChanging(scale: Float) {
    mRuler?.let {
      val tmp = if (scaleSpecail) RulerStringUtil.formatSpecial(scale, it.factor) else RulerStringUtil.formatValue(scale, it.factor)
      if (tmp != binding.tvScale.text.toString()) {
        binding.tvScale.text = tmp
        mCallbacks.forEach { callback ->
          callback.onScaleChanging(tmp.toFloat())
        }
      }
    }
  }

  fun bindRuler(booheeRuler: BooheeRuler) {
    mRuler = booheeRuler
    booheeRuler.setCallback(this)
  }

  fun configValue(config: TextView.() -> Unit) {
    binding.tvScale.also(config)
  }

  fun configUnit(config: TextView.() -> Unit) {
    binding.tvKg.also(config)
  }

  fun addCallback(callback: RulerCallback) {
    mCallbacks.add(callback)
  }

  fun removeCallback(callback: RulerCallback) {
    mCallbacks.remove(callback)
  }

}