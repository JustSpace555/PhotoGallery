package com.bignerdranch.android.phorogallery.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

const val API_KEY = "19de580961507ccbe75cf03e34f03a9e"

interface FlickrApi {

	@GET("services/rest/?method=flickr.interestingness.getList" +
			"&api_key=$API_KEY" +
			"&format=json" +
			"&nojsoncallback=1" +
			"&extras=url_s")
	fun fetchPhotos(@Query("page")page: Int): Call<FlickrResponse>
}