package com.lisapriliant.storyapp.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lisapriliant.storyapp.data.response.ListStoryItem
import com.lisapriliant.storyapp.databinding.ItemStoryBinding

class StoryAdapter : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {
    private val listStory = ArrayList<ListStoryItem>()
    private var onItemClickCallback: OnItemClickCallback? = null

    interface OnItemClickCallback {
        fun onItemClicked(data: ListStoryItem, binding: ItemStoryBinding)
    }

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    inner class StoryViewHolder(private val binding: ItemStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listStories: ListStoryItem) {
            binding.apply {
                Glide.with(ivItemPhoto.context)
                    .load(listStories.photoUrl)
                    .centerCrop()
                    .into(ivItemPhoto)
                tvItemName.text = listStories.name
                tvItemDescription.text = listStories.description

                root.setOnClickListener {
                    onItemClickCallback?.onItemClicked(listStories, binding)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun getItemCount(): Int = listStory.size

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = listStory[position]
        holder.bind(story)
    }

    fun setList(newList: List<ListStoryItem>) {
        val diffResult = DiffUtil.calculateDiff(DiffCallback(listStory, newList))
        listStory.clear()
        listStory.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    class DiffCallback(
        private val oldList: List<ListStoryItem>,
        private val newList: List<ListStoryItem>
    ): DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size
    }
}