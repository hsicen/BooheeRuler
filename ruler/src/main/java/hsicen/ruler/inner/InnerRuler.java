package hsicen.ruler.inner;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.EdgeEffect;
import android.widget.OverScroller;

import hsicen.ruler.BooheeRuler;
import hsicen.ruler.RulerCallback;


/**
 * 作者：hsicen  5/25/21 19:01
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：刻度尺基类
 */
public abstract class InnerRuler extends View {
  protected final static int INVALID_ID = -1;//非法触控id
  protected Context mContext;
  protected BooheeRuler mParent;

  //加入放大倍数来防止精度丢失而导致无限绘制
  protected static final int SCALE_TO_PX_FACTOR = 100;
  //惯性回滚最小偏移值，小于这个值就应该直接滑动到目的点
  protected static final int MIN_SCROLLER_DP = 1;
  protected float minScrollerPx = MIN_SCROLLER_DP;

  protected Paint mSmallScalePaint, mBigScalePaint, mTextPaint, mOutLinePaint;
  //当前刻度值
  protected float mCurrentScale = 10f;
  //最大刻度数
  protected int mMaxLength = 0;
  //长度、最小可滑动值、最大可滑动值
  protected int mLength, mMinPosition = 0, mMaxPosition = 0;
  //控制滑动
  protected OverScroller mOverScroller;
  //一格大刻度多少格小刻度
  protected int mCount = 10;
  //提前刻画量
  protected int mDrawOffset;
  //速度获取
  protected VelocityTracker mVelocityTracker;
  //惯性最大最小速度
  protected int mMaximumVelocity, mMinimumVelocity;
  //回调接口
  protected RulerCallback mRulerCallback;
  //边界效果
  protected EdgeEffect mStartEdgeEffect, mEndEdgeEffect;
  //边缘效应长度
  protected int mEdgeLength;
  protected int mActivePointerId = INVALID_ID;//记录首个触控点的id 避免多点触控引起的滚动
  protected int mLimitMinPosition = 0, mLimitMaxPosition = 0;

  protected boolean touchComplete = true;
  protected boolean scrollComplete = true;
  protected boolean touchMove = false;
  protected long lastScrollUpdate = -1;
  protected long delayMillis = 50;
  private final Runnable scrollerTask = new Runnable() {
    @Override
    public void run() {
      long currentTime = System.currentTimeMillis();
      if (currentTime - lastScrollUpdate > delayMillis) {
        lastScrollUpdate = -1;
        scrollComplete = true;
        if (touchComplete) scrollComplete();
      } else {
        postDelayed(this, delayMillis);
      }
    }
  };

  protected void scrollComplete() {
    if (mRulerCallback != null) {
      mRulerCallback.afterScaleChanged(mCurrentScale);
    }
  }

  public InnerRuler(Context context, BooheeRuler booheeRuler) {
    super(context);
    mParent = booheeRuler;
    init(context);
  }

  public void init(Context context) {
    mContext = context;

    mMaxLength = mParent.getMaxScale() - mParent.getMinScale();
    mCurrentScale = mParent.getCurrentScale();
    mCount = mParent.getCount();
    mDrawOffset = mCount * mParent.getInterval() / 2;

    minScrollerPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MIN_SCROLLER_DP, context.getResources().getDisplayMetrics());

    initPaints();

    mOverScroller = new OverScroller(mContext);

    //mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    //配置速度
    mVelocityTracker = VelocityTracker.obtain();
    mMaximumVelocity = ViewConfiguration.get(context)
      .getScaledMaximumFlingVelocity();
    mMinimumVelocity = ViewConfiguration.get(context)
      .getScaledMinimumFlingVelocity();

    initEdgeEffects();

    //第一次进入，跳转到设定刻度
    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        goToScale(mCurrentScale);
      }
    });
  }

  //初始化画笔
  private void initPaints() {
    mSmallScalePaint = new Paint();
    mSmallScalePaint.setStrokeWidth(mParent.getSmallScaleWidth());
    //提供设置颜色
    mSmallScalePaint.setColor(Color.parseColor("#4DFFFFFF"));
    mSmallScalePaint.setStrokeCap(Paint.Cap.ROUND);

    mBigScalePaint = new Paint();
    //提供设置颜色
    mBigScalePaint.setColor(Color.parseColor("#FFFFFFFF"));
    mBigScalePaint.setStrokeWidth(mParent.getBigScaleWidth());
    mBigScalePaint.setStrokeCap(Paint.Cap.ROUND);

    mTextPaint = new Paint();
    mTextPaint.setAntiAlias(true);
    mTextPaint.setColor(mParent.getTextColor());
    mTextPaint.setTextSize(mParent.getTextSize());
    mTextPaint.setTextAlign(Paint.Align.CENTER);
    mOutLinePaint = new Paint();
    mOutLinePaint.setStrokeWidth(mParent.getOutLineWidth());
    mOutLinePaint.setAntiAlias(true);
    mOutLinePaint.setColor(mParent.getScaleColor());
  }

  //初始化边缘效果
  public void initEdgeEffects() {
    if (mParent.getCanEdgeEffect()) {
      if (mStartEdgeEffect == null || mEndEdgeEffect == null) {
        mStartEdgeEffect = new EdgeEffect(mContext);
        mEndEdgeEffect = new EdgeEffect(mContext);
        mStartEdgeEffect.setColor(mParent.getEdgeColor());
        mEndEdgeEffect.setColor(mParent.getEdgeColor());
        mEdgeLength = mParent.getCursorHeight() + mParent.getInterval() * mParent.getCount();
      }
    }
  }

  @Override
  public void computeScroll() {
    if (mOverScroller.computeScrollOffset()) {
      scrollTo(mOverScroller.getCurrX(), mOverScroller.getCurrY());
      //这是最后OverScroller的最后一次滑动，如果这次滑动完了mCurrentScale不是整数，则把尺子移动到最近的整数位置

      if (!mOverScroller.computeScrollOffset()) {
        int currentIntScale = Math.round(mCurrentScale);
        if ((Math.abs(mCurrentScale - currentIntScale) > 0.001f)) {
          //Fling完进行一次检测回滚
          scrollBackToCurrentScale(currentIntScale);
        }
      }
      postInvalidate();
    }

    scrollComplete = false;
    if (lastScrollUpdate == -1) {
      postDelayed(scrollerTask, delayMillis);
    }
    lastScrollUpdate = System.currentTimeMillis();
  }

  protected abstract void scrollBackToCurrentScale();

  protected abstract void scrollBackToCurrentScale(int currentIntScale);

  protected abstract void goToScale(float scale);

  public abstract void refreshSize();

  //设置尺子当前刻度
  public void setCurrentScale(float currentScale) {
    this.mCurrentScale = currentScale;
    goToScale(mCurrentScale);
  }

  public void setRulerCallback(RulerCallback RulerCallback) {
    this.mRulerCallback = RulerCallback;
  }

  public float getCurrentScale() {
    return mCurrentScale;
  }
}
