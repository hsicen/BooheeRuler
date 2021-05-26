package hsicen.ruler.inner

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.hsicen.extension.extensions.dp2px
import hsicen.ruler.BooheeRuler
import hsicen.ruler.RulerStringUtil.formatSpecial

/**
 * 作者：hsicen  5/25/21 19:27
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：刻度在中间
 */
class CenterHorizontalRuler @JvmOverloads constructor(
  context: Context,
  booheeRuler: BooheeRuler? = null,
  attrs: AttributeSet? = null
) : HorizontalRuler(context, booheeRuler) {

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    drawScale(canvas)
    drawEdgeEffect(canvas)
  }

  private fun drawScale(canvas: Canvas) {
    val start = ((scrollX - mDrawOffset) / mParent.interval + mParent.minScale).toFloat()
    val end = ((scrollX + canvas.width + mDrawOffset) / mParent.interval + mParent.minScale).toFloat()
    val height = canvas.height
    val startY = 21.dp2px

    var index = start
    while (index <= end) {
      val locationX = (index - mParent.minScale) * mParent.interval
      if (index >= mParent.minScale && index <= mParent.maxScale) {
        if (index % mCount == 0f) {
          //整数刻度  绘制线和文字
          val endY = startY + mParent.bigScaleLength
          canvas.drawLine(locationX, startY.toFloat(), locationX, endY.toFloat(), mBigScalePaint)
          canvas.drawText("${formatSpecial(index, mParent.factor)}x", locationX, (height - mParent.textMarginHead).toFloat(), mTextPaint)
        } else {
          //小数刻度  绘制线
          val endY = startY + mParent.smallScaleLength
          canvas.drawLine(locationX, startY.toFloat(), locationX, endY.toFloat(), mSmallScalePaint)
        }
      }
      index++
    }

    //画轮廓线
    //canvas.drawLine(getScrollX(), canvas.getHeight(), getScrollX() + canvas.getWidth(), canvas.getHeight(), mOutLinePaint);
  }

  //画边缘效果
  private fun drawEdgeEffect(canvas: Canvas) {
    if (mParent.canEdgeEffect) {
      if (!mStartEdgeEffect.isFinished) {
        val count = canvas.save()
        canvas.rotate(270f)
        canvas.translate(-height.toFloat(), 0f)
        if (mStartEdgeEffect.draw(canvas)) {
          postInvalidateOnAnimation()
        }
        canvas.restoreToCount(count)
      } else {
        mStartEdgeEffect.finish()
      }

      if (!mEndEdgeEffect.isFinished) {
        val count = canvas.save()
        canvas.rotate(90f)
        canvas.translate((height - mParent.cursorHeight).toFloat(), -mLength.toFloat())
        if (mEndEdgeEffect.draw(canvas)) {
          postInvalidateOnAnimation()
        }
        canvas.restoreToCount(count)
      } else {
        mEndEdgeEffect.finish()
      }
    }
  }
}