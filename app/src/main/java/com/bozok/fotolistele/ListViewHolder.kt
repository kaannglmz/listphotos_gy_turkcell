package com.bozok.fotolistele

import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ListViewHolder(itemView: View,var imageClick:(position:Int)->Unit,var imageLongClick:(position:Int)->Unit) :RecyclerView.ViewHolder(itemView) {

    var ivPhoto:ImageView

    init {
        ivPhoto=itemView.findViewById<ImageView>(R.id.ivPhoto)
        itemView.setOnClickListener {
            imageClick(adapterPosition)
        }
        itemView.setOnLongClickListener {
            imageLongClick(adapterPosition);true
        }

    }

    fun bindImage(uri: Uri){

        ivPhoto.setImageURI(uri)

    }

}