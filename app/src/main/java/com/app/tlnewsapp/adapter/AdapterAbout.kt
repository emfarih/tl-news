package com.app.tlnewsapp.adapter

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import com.app.tlnewsapp.R
import com.app.tlnewsapp.activities.MyApplication
import com.app.tlnewsapp.fragment.FragmentProfile

class AdapterAbout(private val dataList: List<FragmentProfile.Data>, private val context: Context) : RecyclerView.Adapter<AdapterAbout.UserViewHolder>() {
    private var myApplication: MyApplication? = null
    private var mOnItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(view: View, obj: FragmentProfile.Data, position: Int)
    }

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener) {
        this.mOnItemClickListener = mItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.lsv_item_about, null)
        val userViewHolder = UserViewHolder(view)
        myApplication = MyApplication.instance
        return userViewHolder
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

        val data = dataList[position]

        holder.image.setImageResource(data.image)
        holder.title.text = data.title
        holder.sub_title.text = data.sub_title

        if (position == 3) {
            holder.sub_title.visibility = View.GONE
        }

        if (position == 4) {
            holder.sub_title.visibility = View.GONE
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))

            if (myApplication!!.isLogin) {
                holder.relativeLayout.visibility = View.VISIBLE
            } else {
                holder.relativeLayout.visibility = View.GONE
            }

        }

        holder.relativeLayout.setOnClickListener { view ->
            if (mOnItemClickListener != null) {
                mOnItemClickListener!!.onItemClick(view, data, position)
            }
        }

    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var image: ImageView
        internal var title: TextView
        internal var sub_title: TextView
        internal var relativeLayout: RelativeLayout

        init {
            image = itemView.findViewById(R.id.image)
            title = itemView.findViewById(R.id.title)
            sub_title = itemView.findViewById(R.id.sub_title)
            relativeLayout = itemView.findViewById(R.id.lyt_parent)
        }

    }

}