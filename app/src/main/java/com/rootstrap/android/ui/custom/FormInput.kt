package com.rootstrap.android.ui.custom

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import com.rootstrap.android.R
import com.rootstrap.android.databinding.ViewFormInputBinding
import com.rootstrap.android.util.extensions.isEmail
import com.rootstrap.android.util.extensions.isNotEmpty
import com.rootstrap.android.util.extensions.value
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

        binding.inputEditText.doAfterTextChanged { clearError() }
        setAttributes(attrs)
    }

    private fun setAttributes(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FormInput)

        if (typedArray.hasValue(R.styleable.FormInput_android_title))
            binding.titleTextView.text = typedArray.getString(R.styleable.FormInput_android_title)

        if (typedArray.hasValue(R.styleable.FormInput_android_hint))
            binding.inputEditText.hint = typedArray.getString(R.styleable.FormInput_android_hint)?.toUpperCase(Locale.getDefault())

        if (typedArray.hasValue(R.styleable.FormInput_android_inputType))
            binding.inputEditText.inputType = typedArray.getInt(R.styleable.FormInput_android_inputType, InputType.TYPE_CLASS_TEXT)

        if (typedArray.hasValue(R.styleable.FormInput_android_imeOptions))
            binding.inputEditText.imeOptions = typedArray.getInt(R.styleable.FormInput_android_imeOptions, EditorInfo.IME_ACTION_NEXT)

        typedArray.recycle()
    }

    fun setInputClickable(onClick: () -> Unit) {
        binding.inputEditText.setOnClickListener { onClick.invoke() }
        binding.inputEditText.isFocusable = false
    }

    fun setOnEditorActionListener(listener: TextView.OnEditorActionListener) {
        binding.inputEditText.setOnEditorActionListener(listener)
    }

    override fun clearFocus() {
        binding.inputEditText.clearFocus()
    }

    override fun isEnabled(): Boolean {
        return binding.inputEditText.isEnabled
    }

    override fun setEnabled(enabled: Boolean) {
        binding.inputEditText.isEnabled = enabled
    }

    fun setText(text: String) {
        binding.inputEditText.setText(text)
        clearError()
    }

    fun value() = binding.inputEditText.value()

    fun validateNotEmpty(errorMessageResource: Int): Boolean {
        if (!binding.inputEditText.isNotEmpty()) {
            setError(errorMessageResource)
            return false
        }

        clearError()
        return true
    }

    fun validateIsEmail(errorMessageResource: Int): Boolean {
        if (binding.inputEditText.isNotEmpty() && !binding.inputEditText.value().isEmail()) {
            setError(errorMessageResource)
            return false
        }

        clearError()
        return true
    }

    fun validateLength(minLength: Int, errorMessageResource: Int): Boolean {
        if (binding.inputEditText.isNotEmpty() && binding.inputEditText.text.length < minLength) {
            setError(errorMessageResource)
            return false
        }

        clearError()
        return true
    }

    fun validateSameContent(compareTo: FormInput, errorMessageResource: Int): Boolean {
        if (binding.inputEditText.isNotEmpty() && binding.inputEditText.value() != compareTo.value()) {
            setError(errorMessageResource)
            return false
        }

        clearError()
        return true
    }

    fun getError(): String? {
        if (binding.errorTextView.visibility == View.VISIBLE)
            return binding.errorTextView.text.toString()

        return null
    }

    private fun setError(errorResource: Int) {
        binding.inputEditText.setBackgroundResource(R.drawable.bg_form_input_edit_text_error)
        binding.errorTextView.visibility = VISIBLE
        binding.errorTextView.text = context.getString(errorResource)
    }

    private fun clearError() {
        binding.inputEditText.setBackgroundResource(R.drawable.bg_form_input_edit_text)
        binding.errorTextView.visibility = INVISIBLE
    }
}
