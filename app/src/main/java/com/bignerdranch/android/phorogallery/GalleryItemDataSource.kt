package com.bignerdranch.android.phorogallery

import android.util.Log
import androidx.paging.PageKeyedDataSource
import com.bignerdranch.android.phorogallery.api.FlickrApi
import com.bignerdranch.android.phorogallery.api.FlickrResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "PG.GalleryDataSource"

class GalleryItemDataSource(private val flickrApi: FlickrApi):
	PageKeyedDataSource<Int, GalleryItem>() {

	private var currentPage = 1

	private fun getPrevPage() = if (currentPage > 1)
		currentPage - 1
	else
		currentPage

	override fun loadInitial(
		params: LoadInitialParams<Int>,
		callback: LoadInitialCallback<Int, GalleryItem>
	) {
		flickrApi.fetchPhotos(currentPage).enqueue(object : Callback<FlickrResponse> {

			override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
				Log.e(TAG, "Failed to fetch photos", t)
			}

			override fun onResponse(call: Call<FlickrResponse>, response: Response<FlickrResponse>) {
				var galleryItems: List<GalleryItem> =
					response.body()?.photos?.galleryItems ?: mutableListOf()

				galleryItems = galleryItems.filterNot { it.url.isBlank() }
				Log.d(TAG, "LoadInitial")
				Log.d(TAG, "Response received. Amount = ${galleryItems.size}")
				callback.onResult(galleryItems, getPrevPage(), currentPage + 1)
			}
		})
	}

	override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, GalleryItem>) {
		currentPage = getPrevPage()
		flickrApi.fetchPhotos(currentPage).enqueue(object : Callback<FlickrResponse> {

			override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
				Log.e(TAG, "Failed to fetch photos", t)
			}

			override fun onResponse(call: Call<FlickrResponse>, response: Response<FlickrResponse>) {
				var galleryItems: List<GalleryItem> =
					response.body()?.photos?.galleryItems ?: mutableListOf()

				galleryItems = galleryItems.filterNot { it.url.isBlank() }
				Log.d(TAG, "LoadBefore")
				Log.d(TAG, "Response received. Amount = ${galleryItems.size}")
				callback.onResult(galleryItems, currentPage)
			}
		})
	}

	override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, GalleryItem>) {
		flickrApi.fetchPhotos(++currentPage).enqueue(object : Callback<FlickrResponse> {

			override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
				Log.e(TAG, "Failed to fetch photos", t)
			}

			override fun onResponse(call: Call<FlickrResponse>, response: Response<FlickrResponse>) {
				var galleryItems: List<GalleryItem> =
					response.body()?.photos?.galleryItems ?: mutableListOf()

				galleryItems = galleryItems.filterNot { it.url.isBlank() }
				Log.d(TAG, "LoadAfter")
				Log.d(TAG, "Response received. Amount = ${galleryItems.size}")
				callback.onResult(galleryItems, currentPage)
			}
		})
	}
}