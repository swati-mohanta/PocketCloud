package com.xplora.pocketcloud.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xplora.pocketcloud.R

class OnboardingAdapter(
    private val items: List<OnboardingItem>
) : RecyclerView.Adapter<OnboardingAdapter.PageViewHolder>() {

    class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.tvTitle)
        val descText: TextView = view.findViewById(R.id.tvDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding_page, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val item = items[position]
        holder.titleText.text = item.title
        holder.descText.text = item.description
    }

    override fun getItemCount(): Int = items.size
}