package hsicen.ruler

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnPreDrawListener
import com.hsicen.extension.extensions.color
import com.hsicen.extension.extensions.dp2px
import com.hsicen.extension.extensions.drawableRes
import com.hsicen.extension.extensions.sp2px
import hsicen.ruler.inner.*

/**
 * 作者：hsicen  5/25/21 19:19
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：用于包着尺子的外壳，用于画选取光标、外壳
 */
class BooheeRuler @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
  //尺子两端的padding
  private var mPaddingStartAndEnd = 0
  private var mPaddingLeft = 0
  private var mPaddingTop = 0
  private var mPaddingRight = 0
  private var mPaddingBottom = 0

  //尺子背景
  private var mStyle = CENTER_HORIZONTAL
  private var mCursorDrawable = drawableRes(R.drawable.cursor_shape)
  private var mRulerBackGroundColor = color(R.color.black)
  private val mInnerRuler by lazy {
    when (mStyle) {
      TOP_HEAD -> {
        TopHeadRuler(context, this).also {
          paddingHorizontal()
        }
      }
      BOTTOM_HEAD -> {
        BottomHeadRuler(context, this).apply {
          paddingHorizontal()
        }
      }
      LEFT_HEAD -> {
        LeftHeadRuler(context, this).also {
          paddingVertical()
        }
      }
      RIGHT_HEAD -> {
        RightHeadRuler(context, this).also {
          paddingVertical()
        }
      }
      else -> {
        CenterHorizontalRuler(context, this).also {
          paddingHorizontal()
        }
      }
    }
  }

  var minScale = 0 //最小最大刻度值(以0.1kg为单位)
  var maxScale = 40
  var limitMinScale = minScale
  var limitMaxScale = maxScale
  var cursorWidth = 2.dp2px //光标宽度、高度
  var cursorHeight = 42.dp2px
  var smallScaleLength = 8.dp2px //大小刻度的长度
  var bigScaleLength = 8.dp2px
  var smallScaleWidth = 2.dp2px //大小刻度的粗细
  var bigScaleWidth = 2.dp2px
  var textSize = 10.sp2px  //数字字体大小
  var textMarginHead = 1.dp2px //数字Text距离顶部高度
  var interval = 10.dp2px //刻度间隔
  var textColor = color(R.color.white) //数字Text颜色
  var scaleColor = color(R.color.white4) //刻度颜色
  var canEdgeEffect = false //是否启用边缘效应
  var count = 10 //一格大刻度多少格小刻度
  var edgeColor = color(R.color.colorForgiven)  //边缘颜色

  //初始的当前刻度
  var currentScale = 10f
    set(value) {
      field = value
      mInnerRuler.currentScale = value
    }

  //刻度乘积因子
  var factor = 0.1f
    set(value) {
      field = value
      mInnerRuler.postInvalidate()
    }

  //轮廓宽度
  var outLineWidth = 1.dp2px
    set(value) {
      field = value
      mInnerRuler.postInvalidate()
    }

  init {
    attrs?.let {
      val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.BooheeRuler, 0, 0)
      minScale = typedArray.getInteger(R.styleable.BooheeRuler_minScale, minScale)
      maxScale = typedArray.getInteger(R.styleable.BooheeRuler_maxScale, maxScale)
      cursorWidth = typedArray.getDimensionPixelSize(R.styleable.BooheeRuler_cursorWidth, cursorWidth)
      cursorHeight = typedArray.getDimensionPixelSize(R.styleable.BooheeRuler_cursorHeight, cursorHeight)
      smallScaleWidth = typedArray.getDimensionPixelSize(R.styleable.BooheeRuler_smallScaleWidth, smallScaleWidth)
      smallScaleLength = typedArray.getDimensionPixelSize(R.styleable.BooheeRuler_smallScaleLength, smallScaleLength)
      bigScaleWidth = typedArray.getDimensionPixelSize(R.styleable.BooheeRuler_bigScaleWidth, bigScaleWidth)
      bigScaleLength = typedArray.getDimensionPixelSize(R.styleable.BooheeRuler_bigScaleLength, bigScaleLength)
      textSize = typedArray.getDimensionPixelSize(R.styleable.BooheeRuler_numberTextSize, textSize)
      textMarginHead = typedArray.getDimensionPixelSize(R.styleable.BooheeRuler_textMarginHead, textMarginHead)
      interval = typedArray.getDimensionPixelSize(R.styleable.BooheeRuler_scaleInterval, interval)
      textColor = typedArray.getColor(R.styleable.BooheeRuler_numberTextColor, textColor)
      scaleColor = typedArray.getColor(R.styleable.BooheeRuler_scaleColor, scaleColor)
      currentScale = typedArray.getFloat(R.styleable.BooheeRuler_currentScale, ((maxScale + minScale) / 2).toFloat())
      count = typedArray.getInt(R.styleable.BooheeRuler_count, count)
      mCursorDrawable = typedArray.getDrawable(R.styleable.BooheeRuler_cursorDrawable) ?: mCursorDrawable
      mPaddingStartAndEnd = typedArray.getDimensionPixelSize(R.styleable.BooheeRuler_paddingStartAndEnd, mPaddingStartAndEnd)
      mStyle = typedArray.getInt(R.styleable.BooheeRuler_rulerStyle, mStyle)
      mRulerBackGroundColor = typedArray.getColor(R.styleable.BooheeRuler_rulerBackGround, mRulerBackGroundColor)
      canEdgeEffect = typedArray.getBoolean(R.styleable.BooheeRuler_canEdgeEffect, canEdgeEffect)
      edgeColor = typedArray.getColor(R.styleable.BooheeRuler_edgeColor, edgeColor)
      factor = typedArray.getFloat(R.styleable.BooheeRuler_factor, factor)
      outLineWidth = typedArray.getDimensionPixelOffset(R.styleable.BooheeRuler_outlineWidth, outLineWidth)
      typedArray.recycle()
    }

    val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    mInnerRuler.layoutParams = layoutParams
    addView(mInnerRuler)

    initDrawable()
    initRulerBackground()
  }

  private fun initDrawable() {
    viewTreeObserver.addOnPreDrawListener(object : OnPreDrawListener {
      override fun onPreDraw(): Boolean {
        viewTreeObserver.removeOnPreDrawListener(this)
        mCursorDrawable?.let { mCursor ->
          when (mStyle) {
            TOP_HEAD -> mCursor.setBounds((width - cursorWidth) / 2, 0, (width + cursorWidth) / 2, cursorHeight)
            BOTTOM_HEAD -> mCursor.setBounds((width - cursorWidth) / 2, height - cursorHeight, (width + cursorWidth) / 2, height)
            LEFT_HEAD -> mCursor.setBounds(0, (height - cursorHeight) / 2, cursorWidth, (height + cursorHeight) / 2)
            RIGHT_HEAD -> mCursor.setBounds(width - cursorWidth, (height - cursorHeight) / 2, width, (height + cursorHeight) / 2)
            CENTER_HORIZONTAL -> {
              val left = (width - cursorWidth) / 2
              val top = 3.dp2px
              val right = (width + cursorWidth) / 2
              val bottom = top + cursorHeight
              mCursor.setBounds(left, top, right, bottom)
            }
          }
        }
        return false
      }
    })
  }

  private fun initRulerBackground() {
    mInnerRuler.setBackgroundColor(mRulerBackGroundColor)
  }

  private fun paddingHorizontal() {
    mPaddingLeft = mPaddingStartAndEnd
    mPaddingRight = mPaddingStartAndEnd
    mPaddingTop = 0
    mPaddingBottom = 0
  }

  private fun paddingVertical() {
    mPaddingTop = mPaddingStartAndEnd
    mPaddingBottom = mPaddingStartAndEnd
    mPaddingLeft = 0
    mPaddingRight = 0
  }

  override fun dispatchDraw(canvas: Canvas) {
    super.dispatchDraw(canvas)

    mCursorDrawable?.draw(canvas)
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    initDrawable()
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)

    //手动设置刻度尺宽高
    setMeasuredDimension(measuredWidth, measuredHeight)
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    if (changed) {
      val left = mPaddingLeft
      val top = mPaddingTop
      val right = r - l - mPaddingRight
      val bottom = b - t - mPaddingBottom

      mInnerRuler.layout(left, top, right, bottom)
    }
  }

  fun refreshRuler() {
    initDrawable()
    mInnerRuler.init(context)
    mInnerRuler.refreshSize()
  }

  fun setCallback(rulerCallback: RulerCallback) {
    mInnerRuler.setRulerCallback(rulerCallback)
  }

  companion object {
    const val TOP_HEAD = 1
    const val BOTTOM_HEAD = 2
    const val LEFT_HEAD = 3
    const val RIGHT_HEAD = 4
    const val CENTER_HORIZONTAL = 5
  }
}