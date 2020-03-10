package com.bignerdranch.android.phorogallery.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

private const val TAG = "PG.FlickrDeserializer"

class FlickrDeserializer: JsonDeserializer<PhotoResponse> {

	override fun deserialize(
		json: JsonElement?,
		typeOfT: Type?,
		context: JsonDeserializationContext?
	): PhotoResponse {

		val jsonInTotal = json!!.asJsonObject.getAsJsonObject("photos")
		Log.i(TAG, jsonInTotal.toString())
		return Gson().fromJson(jsonInTotal.toString(), PhotoResponse::class.java)
	}
}