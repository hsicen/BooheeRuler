package hsicen.ruler

import kotlin.math.roundToInt

/**
 * 作者：黄思程  5/25/21 19:03
 * 邮箱：huangsicheng@camera360.com
 * 功能：
 * 描述：刻度尺字符串处理
 */
object RulerStringUtil {
  private var mFactorCache = 0f
  private var mDividerCache = 1f

  /**
   * 用于计算刻度值实际显示数值并转化为String，
   * 使用分条件处理是因为浮点运算不是很准确，所以显示float值的时候使用除法而不用乘法，避免了72.0显示成72.00005的情况。
   *
   * @param input  输入值
   * @param factor 乘积因子，限制为正数
   * @return 返回结果字符串
   */
  @JvmStatic
  fun formatValue(input: Float, factor: Float): String {
    return when {
      factor >= 1 -> "${(input * factor).toInt()}"
      factor > 0 -> "${format(input, factor)}"
      else -> ""
    }
  }

  fun formatSpecial(input: Float, factor: Float): String {

    return when (val count = input.toInt()) {
      in 0..10 -> {
        if (count == 10) return "1"
        val tmp = 0.2 + format(input, 0.08f)
        runCatching {
          "${(tmp * 1000).roundToInt() / 1000f}"
        }.getOrNull() ?: "1"
      }
      in 10..20 -> {
        if (count == 20) return "2"
        "${1 + format(input - 10, 0.1f)}"
      }
      in 20..30 -> {
        if (count == 25) return "3"
        if (count == 30) return "4"
        "${2 + format(input - 20, 0.2f)}"
      }
      else -> formatValue(input, factor)
    }
  }

  //小数格式化
  private fun format(input: Float, factor: Float): Float {
    if (mFactorCache != factor) {
      mFactorCache = factor
      mDividerCache = 1 / factor
    }

    return input / mDividerCache
  }
}
