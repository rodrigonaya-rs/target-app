package com.rootstrap.android.ui.custom

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class CustomSpinnerAdapter(
    context: Context,
    resource: Int,
    objects: Array<String>
) : ArrayAdapter<String>(context, resource, objects) {

    companion object {
        const val HINT_POSITION = 0
    }

    override fun isEnabled(position: Int) = !isHint(position)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        setHint(position, view)
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        setHint(position, view)
        return view
    }

    private fun setHint(position: Int, view: View?) {
        if (isHint(position))
            setHintStyle(view)
    }

    private fun isHint(position: Int) = position == HINT_POSITION

    private fun setHintStyle(view: View?) {
        (view as TextView).setTextColor(Color.GRAY)
    }
}
