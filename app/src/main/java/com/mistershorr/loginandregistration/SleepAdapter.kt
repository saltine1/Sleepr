package com.mistershorr.loginandregistration

import android.content.Intent
import android.icu.util.Calendar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset.UTC
import java.time.ZoneOffset.ofHours
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class SleepAdapter (var dataSet: MutableList<Sleep>) : RecyclerView.Adapter<SleepAdapter.ViewHolder>() {

    companion object {
        val TAG = "SleepAdapter"
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewDate : TextView
        val textViewHours: TextView
        val textViewDuration: TextView
        val layout : ConstraintLayout
        val ratingBarQuality : RatingBar
        val textViewBorrower: TextView
        init {
            textViewDate = view.findViewById(R.id.textView_itemSleep_date)
            textViewDuration = view.findViewById(R.id.textView_itemSleep_duration)
            textViewHours = view.findViewById(R.id.textView_itemSleep_hours)
            layout = view.findViewById(R.id.layout_itemSleep)
            ratingBarQuality = view.findViewById(R.id.ratingBar_itemSleep_sleepQuality)
            textViewBorrower = view.findViewById(R.id.textViewBorrower)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SleepAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sleep, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SleepAdapter.ViewHolder, position: Int) {
        val sleep = dataSet?.get(position)
        val context = holder.layout.context

        val formatter = DateTimeFormatter.ofPattern("yyy-MM-dd")
        val sleepDate = LocalDateTime.ofEpochSecond(sleep!!.sleepDateMillis/1000, 0,
            ZoneId.systemDefault().rules.getOffset(Instant.now()))
        holder.textViewDate.text = formatter.format(sleepDate)

        // calculate the difference in time from bed to wake and convert to hours & minutes
        // use String.format() to display it in HH:mm format in the duration textview
        // hint: you need leading zeroes and a width of 2


        // sets the actual hours slept textview
        val bedTime = LocalDateTime.ofEpochSecond(sleep.bedMillis/1000, 0,
            ZoneId.systemDefault().rules.getOffset(Instant.now()))
        val wakeTime = LocalDateTime.ofEpochSecond(sleep.wakeMillis/1000, 0,
            ZoneId.systemDefault().rules.getOffset(Instant.now()))
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val sleepTime = wakeTime.toEpochSecond(ZoneId.systemDefault().rules.getOffset(Instant.now()))*1000 -  bedTime.toEpochSecond(ZoneId.systemDefault().rules.getOffset(Instant.now()))*1000
        holder.textViewHours.text = "${timeFormatter.format(bedTime)} - ${timeFormatter.format(wakeTime)}"
        holder.textViewDuration.text = timeFormatter.format(LocalDateTime.ofEpochSecond(sleepTime/1000, 0,
            ZoneId.systemDefault().rules.getOffset(Instant.now()))).toString()
        holder.ratingBarQuality.rating = sleep.quality.toFloat()/2


        fun deleteFromBackendless(position: Int) {
            Log.d("SleepAdapter", "deleteFromBackendless: Trying to delete ${dataSet[position]}")
            Backendless.Data.of( Sleep::class.java).remove(sleep, object: AsyncCallback<Long?>{
                override fun handleResponse(response: Long?) {
                    var i = 0
                    while (i < dataSet.size){
                        if (dataSet[i] == sleep){
                            dataSet.removeAt(i)
                            notifyDataSetChanged()
                        }
                    }


                }

                override fun handleFault(fault: BackendlessFault?) {
                    TODO("Not yet implemented")
                }

            })
            // put in the code to delete the item using the callback from Backendless
            // in the handleResponse, we'll need to also delete the item from the sleepList
            // and make sure that the recyclerview is updated
        }

        holder.layout.isLongClickable = true
        holder.layout.setOnLongClickListener {
            // the holder.textViewBorrower is the textView that the PopMenu will be anchored to
            val popMenu = PopupMenu(context, holder.textViewBorrower)
            popMenu.inflate(R.menu.menu_sleep_list_context)
            popMenu.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.menu_sleeplist_delete-> {
                        deleteFromBackendless(position)

                        true
                    }
                    else -> true
                }
            }
            popMenu.show()
            true
        }

        holder.layout.setOnClickListener {
            val intent = Intent(context, SleepDetailActivity::class.java).apply {
                putExtra(SleepDetailActivity.EXTRA_SLEEP, sleep)
            }
            context.startActivity(intent)
        }

    }



    override fun getItemCount(): Int {
        return dataSet!!.size
    }
}
