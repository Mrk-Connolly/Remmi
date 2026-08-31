package com.remmi.app.core.ui.navigation

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.remmi.app.R

data class NavigationItem(val iconRes: Int, val contentDescription: String)

class CurvedBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    /** Path used to draw the custom curved background shape of the navigation bar. */
    private val path = Path()
    
    /** Paint used to fill the navigation bar's background path. */
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    /** The background color of the navigation bar. */
    private var navBarColor = Color.WHITE
    
    /** The background color of the central action button. */
    private var centerButtonColor = Color.WHITE
    
    /** Color applied to icons when they are selected. */
    private var selectedIconColor = Color.BLACK
    
    /** Color applied to icons when they are not selected. */
    private var unselectedIconColor = Color.LTGRAY
    
    /** The corner radius for the bottom edges of the navigation bar. */
    private var navBarCornerRadius = dpToPx(28f)
    
    /** Diameter of the central Floating Action Button. */
    private var centerButtonSize = dpToPx(56f)
    
    /** Vertical depth of the central cradle curve. */
    private var curveDepth = dpToPx(24f)
    
    /** Horizontal spacing between the cradle curve and the central button. */
    private var curveHorizontalPadding = dpToPx(12f)
    
    /** Width of the transition ramp from the edges to the central bar section. */
    private var sideRampWidth = dpToPx(40f)

    /** Index of the currently selected navigation item. */
    private var selectedIndex = 0
    
    /** Callback triggered when a standard navigation item is clicked. */
    private var onItemSelectedListener: ((Int) -> Unit)? = null
    
    /** Callback triggered when the central action button is clicked. */
    private var onCenterActionClickListener: (() -> Unit)? = null

    /** Horizontal container for the navigation icons. */
    private val itemsContainer = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        weightSum = 5f // 4 items + 1 space for center
    }

    /** The central Floating Action Button for global actions. */
    private val centerButton = FloatingActionButton(context).apply {
        size = FloatingActionButton.SIZE_NORMAL
        elevation = 0f
        imageTintList = ColorStateList.valueOf(Color.BLACK)
        setImageResource(R.drawable.ic_nav_apps)
        backgroundTintList = ColorStateList.valueOf(Color.WHITE)
        contentDescription = "Apps"
    }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CurvedBottomNavigationView)
        navBarColor = a.getColor(R.styleable.CurvedBottomNavigationView_navBarColor, Color.WHITE)
        centerButtonColor = a.getColor(R.styleable.CurvedBottomNavigationView_centerButtonColor, Color.WHITE)
        selectedIconColor = a.getColor(R.styleable.CurvedBottomNavigationView_selectedIconColor, Color.BLACK)
        unselectedIconColor = a.getColor(R.styleable.CurvedBottomNavigationView_unselectedIconColor, Color.LTGRAY)
        navBarCornerRadius = a.getDimension(R.styleable.CurvedBottomNavigationView_navBarCornerRadius, dpToPx(28f))
        centerButtonSize = a.getDimension(R.styleable.CurvedBottomNavigationView_centerButtonSize, dpToPx(56f))
        curveDepth = a.getDimension(R.styleable.CurvedBottomNavigationView_curveDepth, dpToPx(24f))
        curveHorizontalPadding = a.getDimension(R.styleable.CurvedBottomNavigationView_curveHorizontalPadding, dpToPx(12f))
        sideRampWidth = a.getDimension(R.styleable.CurvedBottomNavigationView_sideRampWidth, dpToPx(40f))
        a.recycle()

        paint.color = navBarColor
        setWillNotDraw(false)
        
        // Removed complex outline provider to fix visual artifacts (hexagonal backgrounds)
        clipToOutline = false

        setupViews()
    }

    private fun setupViews() {
        val itemsParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        itemsParams.topMargin = curveDepth.toInt() // Align with the flat top edge
        itemsContainer.layoutParams = itemsParams
        addView(itemsContainer)
        
        val fabParams = LayoutParams(centerButtonSize.toInt(), centerButtonSize.toInt())
        fabParams.topToTop = LayoutParams.PARENT_ID
        fabParams.startToStart = LayoutParams.PARENT_ID
        fabParams.endToEnd = LayoutParams.PARENT_ID
        // Position FAB slightly above the bar. 
        // We'll adjust its margin top based on how much it should stick out.
        centerButton.layoutParams = fabParams
        centerButton.setOnClickListener { onCenterActionClickListener?.invoke() }
        addView(centerButton)

        centerButton.backgroundTintList = ColorStateList.valueOf(centerButtonColor)
    }

    fun setItems(items: List<NavigationItem>) {
        itemsContainer.removeAllViews()
        
        items.forEachIndexed { index, item ->
            // Add a spacer for the center button at position 2
            if (index == 2) {
                val spacer = View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
                }
                itemsContainer.addView(spacer)
            }

            val itemView = createNavigationItemView(item, index)
            itemsContainer.addView(itemView)
        }
        
        updateSelection()
    }

    private fun createNavigationItemView(item: NavigationItem, index: Int): View {
        val frame = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
            
            // Completely transparent ripple
            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
            setBackgroundResource(outValue.resourceId)
            
            isClickable = true
            isFocusable = true
            setOnClickListener {
                selectedIndex = index
                updateSelection()
                onItemSelectedListener?.invoke(index)
            }
        }

        val imageView = ImageView(context).apply {
            val size = dpToPx(26f).toInt() // Slightly larger
            layoutParams = FrameLayout.LayoutParams(size, size, android.view.Gravity.CENTER)
            setImageResource(item.iconRes)
            contentDescription = item.contentDescription
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        frame.addView(imageView)

        // Visual adjustment to center in the white part of the bar
        // The bar starts at Y = curveDepth.
        frame.setPadding(0, (curveDepth / 2).toInt(), 0, 0)
        
        return frame
    }

    private fun updateSelection() {
        for (i in 0 until itemsContainer.childCount) {
            val container = itemsContainer.getChildAt(i)
            if (container is FrameLayout) {
                val logicalIndex = if (i < 2) i else i - 1
                val isSelected = logicalIndex == selectedIndex
                
                val icon = container.getChildAt(0) as ImageView

                icon.imageTintList = ColorStateList.valueOf(if (isSelected) selectedIconColor else unselectedIconColor)
                
                icon.animate()
                    .scaleX(if (isSelected) 1.2f else 1.0f)
                    .scaleY(if (isSelected) 1.2f else 1.0f)
                    .alpha(if (isSelected) 1f else 0.7f)
                    .setDuration(300)
                    .setInterpolator(android.view.animation.OvershootInterpolator())
                    .start()
            }
        }
    }

    fun setSelectedIndex(index: Int) {
        if (index in 0..3 && index != selectedIndex) {
            selectedIndex = index
            updateSelection()
        }
    }

    fun updateColors(
        navBarColor: Int,
        centerButtonColor: Int,
        selectedIconColor: Int,
        unselectedIconColor: Int
    ) {
        this.navBarColor = navBarColor
        this.centerButtonColor = centerButtonColor
        this.selectedIconColor = selectedIconColor
        this.unselectedIconColor = unselectedIconColor
        
        paint.color = navBarColor
        centerButton.backgroundTintList = ColorStateList.valueOf(centerButtonColor)
        centerButton.imageTintList = ColorStateList.valueOf(selectedIconColor)
        
        updateSelection()
        invalidate()
    }

    fun setOnItemSelectedListener(listener: (Int) -> Unit) {
        onItemSelectedListener = listener
    }

    fun setOnCenterActionClickListener(listener: () -> Unit) {
        onCenterActionClickListener = listener
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculatePath(w.toFloat(), h.toFloat())
        
        // Adjust FAB vertical position.
        // It should be centered horizontally and stick out by ~24dp.
        val fab = centerButton
        val lp = fab.layoutParams as LayoutParams
        // The top of the nav bar starts at curveDepth? No, the bar has a height.
        // The "cradle" goes down.
        // Let's say the bar starts at Y = curveDepth / 2 or similar.
        // Actually, let's keep it simple: the top line is at Y = 20dp (or curveDepth).
        lp.topMargin = 0 // Will adjust in onLayout or just use layout constraints
    }

    private fun calculatePath(w: Float, h: Float) {
        path.reset()
        
        val radius = centerButtonSize / 2
        val cradleRadius = radius + curveHorizontalPadding
        val centerX = w / 2
        
        // The top line of the bar (the flat part)
        val barTop = curveDepth
        val joinRadius = dpToPx(16f) // Radius for the "join" corners

        // Start at top left join (curved)
        path.moveTo(0f, joinRadius)
        path.quadTo(0f, 0f, joinRadius, 0f)
        
        // Ramp Down from left edge to barTop
        path.cubicTo(
            sideRampWidth * 0.4f, 0f,
            sideRampWidth * 0.6f, barTop,
            sideRampWidth, barTop
        )
        
        // Line to cradle start
        path.lineTo(centerX - cradleRadius, barTop)
        
        // Bézier curve for the central cradle
        path.cubicTo(
            centerX - (cradleRadius * 0.5f), barTop,
            centerX - (cradleRadius * 0.5f), barTop + curveDepth,
            centerX, barTop + curveDepth
        )
        path.cubicTo(
            centerX + (cradleRadius * 0.5f), barTop + curveDepth,
            centerX + (cradleRadius * 0.5f), barTop,
            centerX + cradleRadius, barTop
        )
        
        // Line to right ramp start
        path.lineTo(w - sideRampWidth, barTop)

        // Ramp Up to right top join (curved)
        path.cubicTo(
            w - (sideRampWidth * 0.6f), barTop,
            w - (sideRampWidth * 0.4f), 0f,
            w - joinRadius, 0f
        )
        path.quadTo(w, 0f, w, joinRadius)
        
        // Vertical line down to bottom right
        path.lineTo(w, h - navBarCornerRadius)
        
        // Rounded bottom corners
        path.quadTo(w, h, w - navBarCornerRadius, h)
        path.lineTo(navBarCornerRadius, h)
        path.quadTo(0f, h, 0f, h - navBarCornerRadius)
        
        // Close back to top left join
        path.lineTo(0f, joinRadius)
        
        path.close()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
        super.onDraw(canvas)
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }
}
