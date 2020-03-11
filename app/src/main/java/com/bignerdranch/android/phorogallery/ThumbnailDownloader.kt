package com.bignerdranch.android.phorogallery

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import android.util.LruCache
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "PG.ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0

class ThumbnailDownloader<in T> (
	private val responseHandler: Handler,
	private val onThumbnailDownloaded: (T, Bitmap) -> Unit
): HandlerThread(TAG) {

	private var hasQuit = false
	private lateinit var requestHandler: Handler
	private lateinit var cache: LruCache<String, Bitmap>
	private val requestMap = ConcurrentHashMap<T, String>()
	private val flickrFetchr = FlickrFetchr()



	val fragmentLifecycleObserver: LifecycleObserver = object: LifecycleObserver {

		@OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
		fun setup() {
			Log.i(TAG, "Starting background thread")
			start()
			cache = LruCache(5 * 1024 * 1024)
			looper
		}

		@OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
		fun tearDown() {
			Log.i(TAG, "Destroying background thread")
			quit()
		}
	}



	@Suppress("UNCHECKED_CAST")
	@SuppressLint("HandlerLeak")
	override fun onLooperPrepared() {
		requestHandler = object : Handler() {
			override fun handleMessage(msg: Message) {
				if (msg.what == MESSAGE_DOWNLOAD) {
					val target = msg.obj as T
					Log.i(TAG, "Got a request for URL: ${requestMap[target]}")
					handleRequest(target)
				}
				super.handleMessage(msg)
			}
		}
		super.onLooperPrepared()
	}



	private fun handleRequest(target: T) {
		val url = requestMap[target] ?: return

		if (cache[url] == null)
			cache.put(url, flickrFetchr.fetchPhoto(url) ?: return)
		val bitmap = cache[url]

		responseHandler.post(Runnable {
			if (requestMap[target] != url || hasQuit)
				return@Runnable
			requestMap.remove(target)
			onThumbnailDownloaded(target, bitmap)
		})
	}



	fun queueThumbnail(target: T, url: String) {
		Log.i(TAG, "Got a URL: $url")
		requestMap[target] = url
		requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget()
	}

	fun clearQueue() {
		Log.i(TAG, "Clearing all requests from queue")
		requestHandler.removeMessages(MESSAGE_DOWNLOAD)
		requestMap.clear()
	}



	override fun quit(): Boolean {
		hasQuit = true
		return super.quit()
	}
}