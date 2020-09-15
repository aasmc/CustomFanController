package com.example.customfancontroller

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Enum class that represents speeds of fan
 * @param label is of type Int because its values are string resources and not the actual strings
 */
private enum class FanSpeed(val label: Int) {
    OFF(R.string.fan_off),
    LOW(R.string.fan_low),
    MEDIUM(R.string.fan_medium),
    HIGH(R.string.fan_high);

    /**
     * Changes the current fanSpeed to the next speed in the list
     */
    fun next() = when (this) {
        OFF -> LOW
        LOW -> MEDIUM
        MEDIUM -> HIGH
        HIGH -> OFF
    }
}

/**
 * Constants needed for drawing a dial indicators and labels
 */
private const val RADIUS_OFFSET_LABEL = 30
private const val RADIUS_OFFSET_INDICATOR = -35

/**
 * Custom view class
 * JvmOverloads annotations instructs the compiler to generate Java overloaded methods for the
 * function, in this case the constructor to substitute the default values
 */
class DialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * Below listed variables and Paint object are initialized here (instead of initializing them
     * later at object instantiation) to help speed up the drawing
     * of the view.
     */

    /**
     * Radius of the circle
     * This value is set when the view is drawn on the screen
     */
    private var radius = 0.0f

    /**
     * The active selection
     */

    private var fanSpeed = FanSpeed.OFF

    /**
     * Position variable which will be used to  draw label and indicator circle positions
     * PointF - is a point with floating point coordinates
     */
    private val pointPosition: PointF = PointF(0.0f, 0.0f)

    /**
     * Paint object with a number of basic styles
     */
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    private var fanSpeedLowColor = 0
    private var fanSpeedMediumColor = 0
    private var fanSpeedHighColor = 0

    /**
     * Init block is called after constructor to set additional parameters
     * Here we set isClickable property to true to enable clicks on the view
     * Add custom colors to the dial depending on the speed of the fan
     */
    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.DialView) {
            fanSpeedLowColor = getColor(R.styleable.DialView_fanColor1, 0)
            fanSpeedMediumColor = getColor(R.styleable.DialView_fanColor2, 0)
            fanSpeedHighColor = getColor(R.styleable.DialView_fanColor3, 0)
        }

    }

    override fun performClick(): Boolean {
        // this must go first to enable accessibility events as well as calls to onClickListener
        if (super.performClick()) return true

        fanSpeed = fanSpeed.next()
        contentDescription = resources.getString(fanSpeed.label)
        // invalidated entire view forcing to redraw the view
        invalidate()
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // calculate radius
        radius = (min(w, h) / 2.0 * 0.8).toFloat()
    }

    private fun PointF.computeXYForSpeed(pos: FanSpeed, radius: Float) {
        // angles are in radians
        val startAngle = Math.PI * (9 / 8.0)
        val angle = startAngle + pos.ordinal * (Math.PI / 4)
        x = (radius * cos(angle)).toFloat() + +width / 2
        y = (radius * sin(angle)).toFloat() + +height / 2

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // set dial background to gray if nothing is selected or to the color of the speed
        paint.color = when(fanSpeed) {
            FanSpeed.LOW -> fanSpeedLowColor
            FanSpeed.MEDIUM -> fanSpeedMediumColor
            FanSpeed.HIGH -> fanSpeedHighColor
            FanSpeed.OFF -> Color.GRAY
        }

        // this method uses width and height to calculate center of the circle
        canvas?.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)

        // calculate radius of the marker
        val markerRadius = radius + RADIUS_OFFSET_INDICATOR
        pointPosition.computeXYForSpeed(fanSpeed, markerRadius)
        paint.color = Color.BLACK
        canvas?.drawCircle(pointPosition.x, pointPosition.y, radius / 12, paint)

        // calculate radius of the label
        val labelRadius = radius + RADIUS_OFFSET_LABEL
        for (i in FanSpeed.values()) {
            pointPosition.computeXYForSpeed(i, labelRadius)
            val label = resources.getString(i.label)
            canvas?.drawText(label, pointPosition.x, pointPosition.y, paint)
        }
    }

}





















