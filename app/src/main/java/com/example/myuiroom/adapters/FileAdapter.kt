package com.example.myuiroom.adapters

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myuiroom.R
import com.example.myuiroom.models.FileModel
import java.io.File

class FileAdapter (private var fileList: List<FileModel>, private val itemClickListener: (FileModel) -> Unit) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(file: FileModel, clickListener: (FileModel) -> Unit) {
            val imageViewThumbnail: ImageView = itemView.findViewById(R.id.imageViewThumbnail)
            val textViewFileName: TextView = itemView.findViewById(R.id.textViewFileName)

            val fileUri = Uri.fromFile(File(file.filePath))
            Log.d("MyLog", "FileAdapter file.filePath: ${file.filePath}")
            Log.d("MyLog", "FileAdapter file.filePath.endsWith: ${file.filePath.endsWith(".jpg", true)}")
            if (file.filePath.endsWith(".jpg", true) || file.filePath.endsWith(".png", true)) {
                Glide.with(itemView.context).load(fileUri).into(imageViewThumbnail)
            } else if (file.filePath.endsWith(".mp4", true)) {
                // Load video thumbnail
                Glide.with(itemView.context).load(fileUri).thumbnail(0.1f).into(imageViewThumbnail)
            } else {
                // Set a default icon for other file types
                imageViewThumbnail.setImageResource(R.drawable.new_document)
            }

            textViewFileName.text = file.filePath.split("/").last()

            itemView.setOnClickListener { clickListener(file) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.file_item, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = fileList[position]
        holder.bind(file, itemClickListener)
    }

    fun updateFiles(files: List<FileModel>) {
        fileList = files
        notifyDataSetChanged()
    }

    override fun getItemCount() = fileList.size


}