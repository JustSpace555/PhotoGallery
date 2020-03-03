package com.bignerdranch.android.phorogallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class PhotoGalleryViewModel: ViewModel() {

	private val flickrFetchr = FlickrFetchr()
	val galleryItemLiveData: LiveData<List<GalleryItem>>

	init {
		galleryItemLiveData = flickrFetchr.fetchPhotos()
	}

	override fun onCleared() {
		super.onCleared()
		flickrFetchr.cancelRequestInFlight()
	}
}