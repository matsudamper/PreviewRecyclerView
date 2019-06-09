package net.matsudamper

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.annotation.LayoutRes
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * Orientation Vertical Only
 */
class PreviewRecyclerView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {
    private data class PreviewLayoutData(@LayoutRes val layoutId: Int, val spanCount: Int)

    init {
        if (isInEditMode && attrs != null) {
            tag = getPreviewLayoutData(attrs)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (isInEditMode) {
            @Suppress("UNCHECKED_CAST")
            drawPreview(tag as? SortedMap<Int, PreviewLayoutData> ?: return)
        }

    }

    private fun getPreviewLayoutData(attrs: AttributeSet): SortedMap<Int, PreviewLayoutData> {
        val previewLayouts: SortedMap<Int, PreviewLayoutData> = sortedMapOf()

        repeat(attrs.attributeCount) { i ->
            val result = """^preview_(\d)(_span)*$""".toRegex()
                    .find(attrs.getAttributeName(i))

            if (result != null) {
                val previewIndex = result.groups[1]!!.value.toInt()
                val isSpan = result.groups[2] != null

                previewLayouts[previewIndex] =
                        previewLayouts.getOrDefault(previewIndex, PreviewLayoutData(0, 1)).let {
                            if (isSpan) {
                                it.copy(spanCount = attrs.getAttributeIntValue(i, 1))
                            } else {
                                it.copy(layoutId = attrs.getAttributeResourceValue(i, 1))
                            }
                        }
            }
        }

        return previewLayouts
    }

    private fun drawPreview(previewLayouts: SortedMap<Int, PreviewLayoutData>) {
        val scrollView = ScrollView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }.also { (parent as ViewGroup).addView(it) }

        val previewParent = LinearLayout(context)
                .apply {
                    layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    orientation = LinearLayout.VERTICAL
                }.also { scrollView.addView(it) }

        previewLayouts.values.forEach { previewData ->
            val cell = LinearLayout(context)
                    .apply {
                        layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        orientation = LinearLayout.HORIZONTAL
                    }.also { previewParent.addView(it) }

            repeat(previewData.spanCount) {
                LayoutInflater.from(context).inflate(previewData.layoutId, cell, false)
                        .apply {
                            updateLayoutParams<LinearLayout.LayoutParams> {
                                weight = 1f
                            }
                        }.also { cell.addView(it) }
            }
        }
    }
}
