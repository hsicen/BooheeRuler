package hsicen.ruler.inner

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import com.hsicen.extension.extensions.color
import com.hsicen.extension.extensions.dp2px
import hsicen.ruler.BooheeRuler
import hsicen.ruler.R
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
    val startY = 20.dp2px
    var toX = start

    var index = start
    while (index <= end) {
      val locationX = (index - mParent.minScale) * mParent.interval
      if (index >= mParent.minScale && index <= mParent.maxScale) {
        toX = locationX
        if (index % mCount == 0f) {
          val endY = startY + mParent.bigScaleLength
          if (index >= mParent.limitMinScale && index <= mParent.limitMaxScale) {
            mBigScalePaint.color = color(R.color.white)
            mTextPaint.color = color(R.color.white)
          } else {
            mBigScalePaint.color = color(R.color.white9)
            mTextPaint.color = color(R.color.white9)
          }

          canvas.drawLine(locationX, startY.toFloat(), locationX, endY.toFloat(), mBigScalePaint)
          canvas.drawText("${formatSpecial(index, mParent.factor)}x", locationX, (height - mParent.textMarginHead).toFloat(), mTextPaint)
        } else {
          val endY = startY + mParent.smallScaleLength
          if (index >= mParent.limitMinScale && index <= mParent.limitMaxScale) {
            mSmallScalePaint.color = color(R.color.white29)
          } else {
            mSmallScalePaint.color = color(R.color.white9)
          }
          canvas.drawLine(locationX, startY.toFloat(), locationX, endY.toFloat(), mSmallScalePaint)
        }
      }
      index++
    }

    val fromX = 0f - 10f.dp2px
    val fromY = 5f.dp2px
    val toY = fromY + 40f.dp2px
    mOutLinePaint.color = color(R.color.white4)
    mOutLinePaint.style = Paint.Style.STROKE
    canvas.drawRoundRect(fromX, fromY, toX + 10.dp2px, toY, 8f.dp2px, 8f.dp2px, mOutLinePaint)
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