package com.bignerdranch.android.phorogallery.api

import com.bignerdranch.android.phorogallery.GalleryItem
import com.google.gson.annotations.SerializedName

class PhotoResponse {

	@SerializedName("photo")
	lateinit var galleryItems: List<GalleryItem>

	@SerializedName("pages")
	var amountOfPages: Int = 0
}