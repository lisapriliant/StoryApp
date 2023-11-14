package com.lisapriliant.storyapp.ui.customView

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Patterns
import androidx.core.content.ContextCompat
import com.lisapriliant.storyapp.R
import com.google.android.material.textfield.TextInputEditText

class EmailCustomEditText : TextInputEditText {
    private var defaultBackground: Drawable? = null
    private var errorBackground: Drawable? = null
    private var isError: Boolean = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        defaultBackground = ContextCompat.getDrawable(context, R.drawable.default_edit_text_background)
        errorBackground = ContextCompat.getDrawable(context, R.drawable.error_edit_text_background)

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Nothing TODO
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches()) {
                    setError(context.getString(R.string.email_error))
                } else {
                    setError(null)
                }
            }

            override fun afterTextChanged(s: Editable) {
                // Nothing TODO
            }
        })
    }

    private fun setError(errorMessage: String?) {
        error = errorMessage
        isError = !errorMessage.isNullOrEmpty()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        background = if (isError) errorBackground else defaultBackground
    }
}