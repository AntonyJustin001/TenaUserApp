package tena.health.care.custom.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import tena.health.care.R

class RoundedImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val path = Path()
    private var radius: Float = 0f

    init {
        context.theme.obtainStyledAttributes(attrs,
            R.styleable.HalfRoundedImageView, 0, 0
        ).apply {
            try {
                radius = getDimension(R.styleable.HalfRoundedImageView_radius, 0f)
            } finally {
                recycle()
            }
        }
    }

    fun setRadius(newRadius: Float) {
        radius = newRadius
        invalidate() // Redraw the view with the new radius
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val effectiveRadius = if (radius == 0f) Math.min(w, h) / 2f else radius
        path.reset()

        // For a top half-rounded image
        path.moveTo(0f, effectiveRadius)
        path.quadTo(w / 2f, 0f, w.toFloat(), effectiveRadius) // Curve at the top
        path.lineTo(w.toFloat(), h.toFloat()) // Straight lines for the sides and bottom
        path.lineTo(0f, h.toFloat())
        path.close()

        // You can adjust the path for different directions (bottom, left, right).
    }

    override fun onDraw(canvas: Canvas) {
        canvas.clipPath(path)
        super.onDraw(canvas)
    }
}
