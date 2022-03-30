package com.bozok.fotolistele

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ListAdapter(var context:Context, var photoList:ArrayList<Uri>, var imageClick:(position:Int)->Unit,var imageLongClick:(position:Int)->Unit):RecyclerView.Adapter<ListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val v=LayoutInflater.from(context).inflate(R.layout.rv_list,parent,false)
        return ListViewHolder(v,imageClick,imageLongClick)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bindImage(photoList.get(position))
    }

    override fun getItemCount(): Int {
        return photoList.size
    }
}