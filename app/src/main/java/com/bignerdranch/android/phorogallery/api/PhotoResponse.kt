package com.bignerdranch.android.phorogallery.api

import androidx.paging.PagedList
import com.bignerdranch.android.phorogallery.GalleryItem
import com.google.gson.annotations.SerializedName

class PhotoResponse {

	@SerializedName("photo")
	lateinit var galleryItems: List<GalleryItem>

}