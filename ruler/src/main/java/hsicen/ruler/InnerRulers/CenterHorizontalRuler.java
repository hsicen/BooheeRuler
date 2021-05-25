package hsicen.ruler.InnerRulers;

import android.content.Context;
import android.graphics.Canvas;

import hsicen.ruler.BooheeRuler;
import hsicen.ruler.RulerStringUtil;


/**
 * 作者：hsicen  5/25/21 19:27
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：刻度在中间
 */
public class CenterHorizontalRuler extends HorizontalRuler {
  public CenterHorizontalRuler(Context context, BooheeRuler booheeRuler) {
    super(context, booheeRuler);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    drawScale(canvas);
    drawEdgeEffect(canvas);
  }

  //画刻度和字
  private void drawScale(Canvas canvas) {
    float start = (getScrollX() - mDrawOffset) / mParent.getInterval() + mParent.getMinScale();
    float end = (getScrollX() + canvas.getWidth() + mDrawOffset) / mParent.getInterval() + mParent.getMinScale();
    int height = canvas.getHeight();

    for (float i = start; i <= end; i++) {
      float locationX = (i - mParent.getMinScale()) * mParent.getInterval();

      if (i >= mParent.getMinScale() && i <= mParent.getMaxScale()) {
        if (i % mCount == 0) {
          //整数刻度  绘制线和文字
          int startY = height / 2 - mParent.getBigScaleLength() / 2;
          int endY = height / 2 + mParent.getBigScaleLength() / 2;
          canvas.drawLine(locationX, startY, locationX, endY, mBigScalePaint);
          canvas.drawText(RulerStringUtil.formatValue(i, mParent.getFactor()), locationX, height - mParent.getTextMarginHead(), mTextPaint);
        } else {
          //小数刻度  绘制线
          int startY = height / 2 - mParent.getSmallScaleLength() / 2;
          int endY = height / 2 + mParent.getSmallScaleLength() / 2;
          canvas.drawLine(locationX, startY, locationX, endY, mSmallScalePaint);
        }
      }
    }

    //画轮廓线
    //canvas.drawLine(getScrollX(), canvas.getHeight(), getScrollX() + canvas.getWidth(), canvas.getHeight(), mOutLinePaint);
  }

  //画边缘效果
  private void drawEdgeEffect(Canvas canvas) {
    if (mParent.canEdgeEffect()) {
      if (!mStartEdgeEffect.isFinished()) {
        int count = canvas.save();
        canvas.rotate(270);
        canvas.translate(-getHeight(), 0);
        if (mStartEdgeEffect.draw(canvas)) {
          postInvalidateOnAnimation();
        }
        canvas.restoreToCount(count);
      } else {
        mStartEdgeEffect.finish();
      }
      if (!mEndEdgeEffect.isFinished()) {
        int count = canvas.save();
        canvas.rotate(90);
        canvas.translate((getHeight() - mParent.getCursorHeight()), -mLength);
        if (mEndEdgeEffect.draw(canvas)) {
          postInvalidateOnAnimation();
        }
        canvas.restoreToCount(count);
      } else {
        mEndEdgeEffect.finish();
      }
    }
  }
}