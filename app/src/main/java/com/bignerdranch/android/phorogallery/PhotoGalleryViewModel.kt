package com.bignerdranch.android.phorogallery

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList

private const val TAG = "PG.GalleryViewModel"

class PhotoGalleryViewModel: ViewModel() {

	private val flickrFetchr = FlickrFetchr()
	val galleryItemLiveData: LiveData<PagedList<GalleryItem>>

	init {
		galleryItemLiveData = flickrFetchr.fetchPhotos()
	}

	override fun onCleared() {
		super.onCleared()
		flickrFetchr.cancelRequestInFlight()
	}
}