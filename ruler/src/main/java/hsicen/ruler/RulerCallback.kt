package hsicen.ruler

/**
 * 作者：hsicen  5/25/21 18:59
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：刻度尺刻度变化回调
 */
interface RulerCallback {
  //选取刻度变化的时候回调
  fun onScaleChanging(scale: Float)

  //选取刻度变化完成的时候回调
  fun afterScaleChanged(scale: Float)
}