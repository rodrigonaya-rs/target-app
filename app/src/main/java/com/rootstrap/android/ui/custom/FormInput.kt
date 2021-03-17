package com.rootstrap.android.ui.custom

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintLayout
import com.rootstrap.android.R
import com.rootstrap.android.databinding.ViewFormInputBinding
import java.util.Locale

class FormInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding: ViewFormInputBinding
    init {
        val layoutInflater = LayoutInflater.from(context)
        layoutInflater.inflate(R.layout.view_form_input, this, true)
        binding = ViewFormInputBinding.inflate(layoutInflater, this, true)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FormInput)

        if (typedArray.hasValue(R.styleable.FormInput_android_title))
            binding.titleTextView.text = typedArray.getString(R.styleable.FormInput_android_title)

        if (typedArray.hasValue(R.styleable.FormInput_android_hint))
            binding.inputEditText.hint = typedArray.getString(R.styleable.FormInput_android_hint)?.toUpperCase(Locale.getDefault())

        if (typedArray.hasValue(R.styleable.FormInput_android_inputType))
            binding.inputEditText.inputType = typedArray.getInt(R.styleable.FormInput_android_inputType, InputType.TYPE_CLASS_TEXT)

        if (typedArray.hasValue(R.styleable.FormInput_android_imeOptions))
            binding.inputEditText.imeOptions = typedArray.getInt(R.styleable.FormInput_android_imeOptions, EditorInfo.IME_ACTION_NONE)

        typedArray.recycle()
    }

    fun setError(errorResource: Int) {
        binding.errorTextView.visibility = VISIBLE
        binding.errorTextView.text = context.getString(errorResource)
    }

    fun clearError() {
        binding.errorTextView.visibility = INVISIBLE
    }
}
