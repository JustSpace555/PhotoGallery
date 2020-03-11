package com.bignerdranch.android.phorogallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.bignerdranch.android.phorogallery.api.FlickrApi
import com.bignerdranch.android.phorogallery.api.FlickrDeserializer
import com.bignerdranch.android.phorogallery.api.PhotoInterceptor
import com.bignerdranch.android.phorogallery.api.PhotoResponse
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ResponseCache
import java.util.concurrent.Executor

private const val TAG = "PG.FlickrFetchr"

class FlickrFetchr {

	private val flickrApi: FlickrApi
	private lateinit var flickrRequest: Call<PhotoResponse>

	private class CustomExecutor: Executor {
		val handler = Handler(Looper.getMainLooper())

		override fun execute(command: Runnable) {
			handler.post(command)
		}
	}

	init {
		val gsonBuilder = GsonBuilder()
		gsonBuilder.registerTypeAdapter(
			PhotoResponse::class.java,
			FlickrDeserializer()
		)

		val client = OkHttpClient.Builder()
			.addInterceptor(PhotoInterceptor())
			.build()

		val retrofit: Retrofit = Retrofit.Builder()
			.baseUrl("https://api.flickr.com/")
			.addConverterFactory(
				GsonConverterFactory.create(gsonBuilder.create())
			)
//			.client(client)
			.build()

		flickrApi = retrofit.create(FlickrApi::class.java)
	}

	fun fetchPhotos(): LiveData<PagedList<GalleryItem>> {

		val executor = CustomExecutor()

		val config = PagedList.Config.Builder().apply {
			setPageSize(30)
			setPrefetchDistance(6)
			setInitialLoadSizeHint(30)
			setEnablePlaceholders(false)
		}.build()

		val dataSourceFactory = object : DataSource.Factory<Int, GalleryItem>() {
			override fun create(): DataSource<Int, GalleryItem> = GalleryItemDataSource(flickrApi)
		}

		return LivePagedListBuilder<Int, GalleryItem>(dataSourceFactory, config)
			.setFetchExecutor(executor)
			.build()
	}

	@WorkerThread
	fun fetchPhoto(url: String): Bitmap? {

		val response: Response<ResponseBody> = flickrApi.fetchUrlBytes(url).execute()
		val bitmap = response.body()?.byteStream()?.use(BitmapFactory::decodeStream)
		Log.i(TAG, "Decoded bitmap = $bitmap from response = $response")
		return bitmap
	}

	fun cancelRequestInFlight() {
		if (::flickrRequest.isInitialized)
			flickrRequest.cancel()
	}
}