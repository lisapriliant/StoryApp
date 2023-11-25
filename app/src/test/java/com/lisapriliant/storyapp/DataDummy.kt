package com.lisapriliant.storyapp

import com.lisapriliant.storyapp.data.response.ListStoryItem

object DataDummy {
    fun generateDummyStoryResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..100) {
            val story = ListStoryItem(
                "https://story-api.dicoding.dev/images/stories/photos-1700819181638_1VU6xiEc.jpg",
                "2023-11-24T09:46:21.641Z",
                "Lisa",
                "mabar dulu gasih",
                109.3394225,
                "story $i",
                -7.4296268
            )
            items.add(story)
        }
        return items
    }
}