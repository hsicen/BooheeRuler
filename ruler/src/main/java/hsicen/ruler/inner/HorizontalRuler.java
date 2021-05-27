package hsicen.ruler.inner;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewGroup;

import androidx.annotation.Px;

import com.hsicen.extension.log.KLog;

import hsicen.ruler.BooheeRuler;

/**
 * 作者：hsicen  5/26/21 18:33
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：水平尺子抽象类
 */
public abstract class HorizontalRuler extends InnerRuler {
  private float mLastX = 0;
  protected int mHalfWidth = 0;

  public HorizontalRuler(Context context, BooheeRuler booheeRuler) {
    super(context, booheeRuler);
  }

  //处理滑动，主要是触摸的时候通过计算现在的event坐标和上一个的位移量来决定scrollBy()的多少
  //滑动完之后计算速度是否满足Fling，满足则使用OverScroller来计算Fling滑动
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    //开始速度检测
    if (mVelocityTracker == null) {
      mVelocityTracker = VelocityTracker.obtain();
    }

    mVelocityTracker.addMovement(event);
    ViewGroup parent = (ViewGroup) getParent();//为了解决刻度尺在scrollview这种布局里面滑动冲突问题
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        touchComplete = false;
        touchMove = false;

        //记录首个触控点的id
        mActivePointerId = event.findPointerIndex(event.getActionIndex());
        if (!mOverScroller.isFinished()) {
          mOverScroller.abortAnimation();
        }

        mLastX = event.getX();
        parent.requestDisallowInterceptTouchEvent(true);//按下时开始让父控件不要处理任何touch事件
        break;
      case MotionEvent.ACTION_MOVE:
        touchMove = true;

        if (mActivePointerId == INVALID_ID || event.findPointerIndex(mActivePointerId) == INVALID_ID) {
          break;
        }
        float moveX = mLastX - event.getX(mActivePointerId);
        mLastX = event.getX(mActivePointerId);
        scrollBy((int) (moveX), 0);
        break;

      case MotionEvent.ACTION_UP:
        touchComplete = true;
        mActivePointerId = INVALID_ID;
        mLastX = 0;
        //处理松手后的Fling
        mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
        int velocityX = (int) mVelocityTracker.getXVelocity();
        if (Math.abs(velocityX) > mMinimumVelocity) {
          fling(-velocityX);
        } else {
          scrollBackToCurrentScale();
        }
        //VelocityTracker回收
        if (mVelocityTracker != null) {
          mVelocityTracker.recycle();
          mVelocityTracker = null;
        }
        releaseEdgeEffects();
        parent.requestDisallowInterceptTouchEvent(false);//up或者cancel的时候恢复
        if (scrollComplete && touchMove) scrollComplete();
        break;

      case MotionEvent.ACTION_CANCEL:
        touchComplete = true;
        mActivePointerId = INVALID_ID;
        mLastX = 0;
        if (!mOverScroller.isFinished()) {
          mOverScroller.abortAnimation();
        }
        //回滚到整点刻度
        scrollBackToCurrentScale();
        //VelocityTracker回收
        if (mVelocityTracker != null) {
          mVelocityTracker.recycle();
          mVelocityTracker = null;
        }
        releaseEdgeEffects();
        parent.requestDisallowInterceptTouchEvent(false);//up或者cancel的时候恢复
        if (scrollComplete && touchMove) scrollComplete();
        break;
    }
    return true;
  }

  private void fling(int vX) {
    mOverScroller.fling(getScrollX(), 0, vX, 0, mLimitMinPosition - mEdgeLength, mLimitMaxPosition + mEdgeLength, 0, 0);
    invalidate();
  }

  //重写滑动方法，设置到边界的时候不滑,并显示边缘效果。滑动完输出刻度。
  @Override
  public void scrollTo(@Px int x, @Px int y) {
    KLog.INSTANCE.d("scrollTo x: " + x);
    if (x < mMinPosition) {
      goStartEdgeEffect(x);
      x = mMinPosition;
    }

    if (x > mMaxPosition) {
      goEndEdgeEffect(x);
      x = mMaxPosition;
    }

    if (x >= mLimitMinPosition && x <= mLimitMaxPosition) {
      if (x != getScrollX()) {
        super.scrollTo(x, y);
      }

      mCurrentScale = scrollXtoScale(x);
      if (mRulerCallback != null) {
        mRulerCallback.onScaleChanging(Math.round(mCurrentScale));
      }
    }
  }

  //头部边缘效果处理
  private void goStartEdgeEffect(int x) {
    if (mParent.getCanEdgeEffect()) {
      if (!mOverScroller.isFinished()) {
        mStartEdgeEffect.onAbsorb((int) mOverScroller.getCurrVelocity());
        mOverScroller.abortAnimation();
      } else {
        mStartEdgeEffect.onPull((float) (mMinPosition - x) / (mEdgeLength) * 3 + 0.3f);
        mStartEdgeEffect.setSize(mParent.getCursorHeight(), getWidth());
      }
      postInvalidateOnAnimation();
    }
  }

  //尾部边缘效果处理
  private void goEndEdgeEffect(int x) {
    if (mParent.getCanEdgeEffect()) {
      if (!mOverScroller.isFinished()) {
        mEndEdgeEffect.onAbsorb((int) mOverScroller.getCurrVelocity());
        mOverScroller.abortAnimation();
      } else {
        mEndEdgeEffect.onPull((float) (x - mMaxPosition) / (mEdgeLength) * 3 + 0.3f);
        mEndEdgeEffect.setSize(mParent.getCursorHeight(), getWidth());
      }
      postInvalidateOnAnimation();
    }
  }

  //取消边缘效果动画
  private void releaseEdgeEffects() {
    if (mParent.getCanEdgeEffect()) {
      mStartEdgeEffect.onRelease();
      mEndEdgeEffect.onRelease();
    }
  }

  //直接跳转到当前刻度(手动或初始化设置)
  public void goToScale(float scale) {
    mCurrentScale = Math.round(scale);
    scrollTo(scaleToScrollX(mCurrentScale), 0);
    if (mRulerCallback != null) {
      mRulerCallback.onScaleChanging(mCurrentScale);
    }
  }

  //把滑动偏移量scrollX转化为刻度Scale
  private float scrollXtoScale(int scrollX) {

    return ((float) (scrollX - mMinPosition) / mLength) * mMaxLength + mParent.getMinScale();
  }

  //把Scale转化为ScrollX
  private int scaleToScrollX(float scale) {

    return (int) ((scale - mParent.getMinScale()) / mMaxLength * mLength + mMinPosition);
  }

  //把Scale转化为ScrollX,放大SCALE_TO_PX_FACTOR倍，以免精度丢失问题
  private float scaleToScrollFloatX(float scale) {

    return (((scale - mParent.getMinScale()) / mMaxLength * mLength * SCALE_TO_PX_FACTOR) + mMinPosition * SCALE_TO_PX_FACTOR);
  }

  //把移动后光标对准距离最近的刻度，就是回弹到最近刻度
  @Override
  protected void scrollBackToCurrentScale() {

    scrollBackToCurrentScale(Math.round(mCurrentScale));
  }

  @Override
  protected void scrollBackToCurrentScale(int currentIntScale) {
    float intScrollX = scaleToScrollFloatX(currentIntScale);
    int dx = Math.round((intScrollX - SCALE_TO_PX_FACTOR * getScrollX()) / SCALE_TO_PX_FACTOR);
    if (Math.abs(dx) > minScrollerPx) {
      //渐变回弹
      mOverScroller.startScroll(getScrollX(), getScrollY(), dx, 0, 500);
      invalidate();
    } else {
      //立刻回弹
      scrollBy(dx, 0);
    }
  }

  @Override
  public void refreshSize() {
    mLength = (mParent.getMaxScale() - mParent.getMinScale()) * mParent.getInterval();
    mHalfWidth = getWidth() / 2;
    mMinPosition = -mHalfWidth;
    mMaxPosition = mLength - mHalfWidth;
    mLimitMinPosition = mMinPosition + mParent.getLimitMinScale() * mParent.getInterval();
    mLimitMaxPosition = mMaxPosition - (mParent.getMaxScale() - mParent.getLimitMaxScale()) * mParent.getInterval();
  }

  //获取控件宽高，设置相应信息
  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    refreshSize();
  }
}
