package com.bignerdranch.android.phorogallery

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.bignerdranch.android.phorogallery.api.FlickrApi
import com.bignerdranch.android.phorogallery.api.FlickrResponse
import com.bignerdranch.android.phorogallery.api.PhotoResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

private const val TAG = "PG.FlickrFetchr"

class FlickrFetchr {

	private val flickrApi: FlickrApi
	private lateinit var flickrRequest: Call<FlickrResponse>

	init {
		val retrofit: Retrofit = Retrofit.Builder()
			.baseUrl("https://api.flickr.com/")
			.addConverterFactory(GsonConverterFactory.create())
			.build()

		flickrApi = retrofit.create(FlickrApi::class.java)
	}

	fun fetchPhotos(): LiveData<PagedList<GalleryItem>> {

		val config = PagedList.Config.Builder().apply {
			setPageSize(30)
			setEnablePlaceholders(false)
		}.build()

		val dataSourceFactory = object : DataSource.Factory<Int, GalleryItem>() {
			override fun create(): DataSource<Int, GalleryItem> = GalleryItemDataSource(flickrApi)
		}

		return LivePagedListBuilder<Int, GalleryItem>(dataSourceFactory, config).build()
	}

	fun cancelRequestInFlight() {
		if (::flickrRequest.isInitialized)
			flickrRequest.cancel()
	}
}