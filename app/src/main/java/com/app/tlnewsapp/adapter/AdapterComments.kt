package com.app.tlnewsapp.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.app.tlnewsapp.R
import com.app.tlnewsapp.Config
import com.app.tlnewsapp.activities.MyApplication
import com.app.tlnewsapp.models.Comments
import com.app.tlnewsapp.utils.Tools
import com.squareup.picasso.Picasso

import org.ocpsoft.prettytime.PrettyTime

import java.util.ArrayList
import java.util.Date

class AdapterComments// Provide a suitable constructor (depends on the kind of dataset)
(private val ctx: Context, items: ArrayList<Comments>) : RecyclerView.Adapter<AdapterComments.ViewHolder>() {

    private var items: List<Comments> = ArrayList()
    internal var myApplication: MyApplication? = null
    private var mOnItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(view: View, obj: Comments, position: Int, context: Context)
    }

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener) {
        this.mOnItemClickListener = mItemClickListener
    }

    init {
        this.items = items
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        // each data item is just a string in this case
        var user_name: TextView
        var user_image: ImageView
        var comment_date: TextView
        var comment_message: TextView
        var lyt_parent: LinearLayout

        init {
            user_name = v.findViewById(R.id.user_name)
            user_image = v.findViewById(R.id.user_image)
            comment_date = v.findViewById(R.id.comment_date)
            comment_message = v.findViewById(R.id.edt_comment_message)
            lyt_parent = v.findViewById(R.id.lyt_parent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        val v = LayoutInflater.from(parent.context).inflate(R.layout.lsv_item_comments, parent, false)
        val vh = ViewHolder(v)
        myApplication = MyApplication.instance
        return vh
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val c = items[position]

        if (myApplication!!.isLogin && myApplication!!.userId == c.user_id) {
            holder.user_name.text = c.name + " ( " + ctx.resources.getString(R.string.txt_you) + " )"
        } else {
            holder.user_name.text = c.name
        }

        Picasso.with(ctx)
                .load(Config.ADMIN_PANEL_URL + "/upload/avatar/" + c.image.replace(" ", "%20"))
                .resize(200, 200)
                .centerCrop()
                .placeholder(R.drawable.ic_people)
                .into(holder.user_image)


        //holder.comment_date.setText(c.date_time);
        val prettyTime = PrettyTime()
        val timeAgo = Tools.timeStringtoMilis(c.date_time)
        holder.comment_date.text = prettyTime.format(Date(timeAgo))

        holder.comment_message.text = c.content

        holder.lyt_parent.setOnClickListener { view ->
            if (mOnItemClickListener != null) {
                mOnItemClickListener!!.onItemClick(view, c, position, ctx)
            }
        }
    }

    fun setListData(items: List<Comments>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun resetListData() {
        this.items = ArrayList()
        notifyDataSetChanged()
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return items.size
    }

}