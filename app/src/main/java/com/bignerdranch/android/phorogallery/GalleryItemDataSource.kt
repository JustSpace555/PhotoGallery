package com.bignerdranch.android.phorogallery

import android.util.Log
import androidx.paging.PageKeyedDataSource
import com.bignerdranch.android.phorogallery.api.FlickrApi
import com.bignerdranch.android.phorogallery.api.PhotoResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "PG.GalleryDataSource"

class GalleryItemDataSource(private val flickrApi: FlickrApi):
	PageKeyedDataSource<Int, GalleryItem>() {

	private var currentPage = 1
	private var amountOfPages = 5

	override fun loadInitial(
		params: LoadInitialParams<Int>,
		callback: LoadInitialCallback<Int, GalleryItem>
	) {
		flickrApi.fetchPhotos(currentPage).enqueue(object : Callback<PhotoResponse> {

			override fun onFailure(call: Call<PhotoResponse>, t: Throwable) {
				Log.e(TAG, "Failed to fetch photos", t)
			}

			override fun onResponse(call: Call<PhotoResponse>, response: Response<PhotoResponse>) {
				var galleryItems: List<GalleryItem> =
					response.body()?.galleryItems ?: mutableListOf()
				amountOfPages = response.body()?.amountOfPages!!

				galleryItems = galleryItems.filterNot { it.url.isBlank() }
				Log.d(TAG, "LoadInitial")
				Log.d(TAG, "Response received. Amount = ${galleryItems.size}")
				callback.onResult(galleryItems, currentPage - 1, currentPage + 1)
			}
		})
	}

	override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, GalleryItem>) {

		if (currentPage <= 1)
			return

		currentPage--
		flickrApi.fetchPhotos(currentPage).enqueue(object : Callback<PhotoResponse> {

			override fun onFailure(call: Call<PhotoResponse>, t: Throwable) {
				Log.e(TAG, "Failed to fetch photos", t)
			}

			override fun onResponse(call: Call<PhotoResponse>, response: Response<PhotoResponse>) {
				var galleryItems: List<GalleryItem> =
					response.body()?.galleryItems ?: mutableListOf()

				galleryItems = galleryItems.filterNot { it.url.isBlank() }
				Log.d(TAG, "LoadBefore")
				Log.d(TAG, "Response received. Amount = ${galleryItems.size}")
				callback.onResult(galleryItems, currentPage)
			}
		})
	}

	override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, GalleryItem>) {

		if (currentPage >= amountOfPages)
			return

		currentPage++
		flickrApi.fetchPhotos(currentPage).enqueue(object : Callback<PhotoResponse> {

			override fun onFailure(call: Call<PhotoResponse>, t: Throwable) {
				Log.e(TAG, "Failed to fetch photos", t)
			}

			override fun onResponse(call: Call<PhotoResponse>, response: Response<PhotoResponse>) {
				var galleryItems: List<GalleryItem> =
					response.body()?.galleryItems ?: mutableListOf()

				galleryItems = galleryItems.filterNot { it.url.isBlank() }
				Log.d(TAG, "LoadAfter")
				Log.d(TAG, "Response received. Amount = ${galleryItems.size}")
				callback.onResult(galleryItems, currentPage)
			}
		})
	}
}