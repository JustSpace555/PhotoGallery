package com.bignerdranch.android.phorogallery

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "PG.PhotoGalleryFragment"

class PhotoGalleryFragment: Fragment() {

	private lateinit var photoRecyclerView: RecyclerView
	private lateinit var photoGalleryViewModel: PhotoGalleryViewModel
	private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>

	companion object {
		fun newInstance() = PhotoGalleryFragment()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		retainInstance = true

		photoGalleryViewModel = ViewModelProvider(this).get(PhotoGalleryViewModel::class.java)

		val responseHandler = Handler()
		thumbnailDownloader = ThumbnailDownloader(responseHandler) {
			photoHolder, bitmap ->
			val drawable = BitmapDrawable(resources, bitmap)
			photoHolder.bindDrawable(drawable)
		}
		lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {

		val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)
		photoRecyclerView = view.findViewById(R.id.photo_recycler_view)
		photoRecyclerView.layoutManager = GridLayoutManager(context, 3)

		return view
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		ViewTreeObserver.OnGlobalLayoutListener {
			object : ViewTreeObserver.OnGlobalLayoutListener {
				override fun onGlobalLayout() {
					photoRecyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
					photoRecyclerView.layoutManager = GridLayoutManager(
						context, photoRecyclerView.width / 120
					)
				}
			}
		}

		val adapter = PhotoAdapter()
		photoRecyclerView.adapter = adapter

		photoGalleryViewModel.galleryItemLiveData.observe(viewLifecycleOwner, Observer<PagedList<GalleryItem>> {
			galleryItems ->
			adapter.submitList(galleryItems)
		})
	}

	override fun onDestroyView() {
		super.onDestroyView()
		thumbnailDownloader.clearQueue()
	}

	override fun onDestroy() {
		super.onDestroy()
		lifecycle.removeObserver(thumbnailDownloader.fragmentLifecycleObserver)
	}

	private class GalleryItemDiffUtil: DiffUtil.ItemCallback<GalleryItem>() {

		override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean =
			oldItem.title == newItem.title

		override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean =
			oldItem.id == newItem.id || oldItem.url == newItem.url
	}

	private class PhotoHolder(itemImageView: ImageView):
		RecyclerView.ViewHolder(itemImageView) {

		val bindDrawable: (Drawable) -> Unit = itemImageView::setImageDrawable
	}

	private inner class PhotoAdapter:
		PagedListAdapter<GalleryItem, PhotoHolder>(GalleryItemDiffUtil()) {

		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
			val view = layoutInflater.inflate(
				R.layout.list_item_gallery, parent, false
			) as ImageView
			return PhotoHolder(view)
		}

		override fun onBindViewHolder(holder: PhotoHolder, position: Int) {

			val placeholder: Drawable = ContextCompat.getDrawable(
				requireContext(), R.drawable.bill_up_close
			) ?: ColorDrawable()

			holder.bindDrawable(placeholder)

			getItem(position)?.let {
				galleryItem ->
				thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
			}
		}
	}
}