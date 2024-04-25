package com.mistershorr.loginandregistration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder
import com.mistershorr.loginandregistration.databinding.ActivitySleepListBinding


class SleepListActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySleepListBinding
    private lateinit var adapter: SleepAdapter



    companion object{
        val TAG = "sleeplist"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySleepListBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
    override fun onResume(){
        super.onResume()
        loadDataFromBackendless()
        saveToBackendless()
        Log.d("onResume", "resumed")
    }


    // Functions to temporarily go into the SleepListActivity's onCreate for testing

    fun loadDataFromBackendless() {
        val userId = Backendless.UserService.CurrentUser().userId
        // need the ownerId to match the objectId of the user
        val whereClause = "ownerId = '$userId'"
        val queryBuilder = DataQueryBuilder.create()
        queryBuilder.whereClause = whereClause
        // include the queryBuilder in the find function
        Backendless.Data.of(Sleep::class.java).find(queryBuilder, object : AsyncCallback<MutableList<Sleep>> {
            override fun handleResponse(response: MutableList<Sleep>) {
                Log.d(TAG, "handleResponse: $response")
                // this is where you would set up your recyclerView
                adapter = SleepAdapter(response)
                binding.RecyclerViewSleepList.adapter = adapter
                binding.RecyclerViewSleepList.layoutManager = LinearLayoutManager(this@SleepListActivity)
            }

            override fun handleFault(fault: BackendlessFault) {
                Log.d(TAG, "handleFault: ${fault.message}")
            }
        })

    }

    fun saveToBackendless() {
        // the real use case will be to read from all the editText
        // fields in the detail activity and then use that info
        // to make the object

        // here, we'll just make up an object

        var parcel = intent.getParcelableExtra<Sleep>("EXTRA_SLEEP")
        val wakeMillis = parcel?.wakeMillis
        val notes = parcel?.notes
        val sleepDataMillis = parcel?.sleepDateMillis
        val sleepRating = parcel?.quality
        val varBedMillis = parcel?.bedMillis
        val ownerId = parcel?.ownerId
        val objectId = parcel?.objectId

        if (parcel != null) {
            val sleepObj: HashMap<Any?, Any?> = hashMapOf()
            sleepObj["wakeMillis"] = wakeMillis
            sleepObj["notes"] = notes
            sleepObj["sleepDataMillis"] = sleepDataMillis
            sleepObj["sleepRating"] = sleepRating
            sleepObj["varBedMillis"] = varBedMillis
            sleepObj["ownerId"] = ownerId
            sleepObj["objectId"] = objectId

            // save object asynchronously
            Backendless.Data.of("Sleep").save(sleepObj, object : AsyncCallback<Map<*, *>?> {
                override fun handleResponse(response: Map<*, *>?) {
                }

                override fun handleFault(fault: BackendlessFault) {
                    // an error has occurred, the error code can be retrieved with fault.getCode()
                }
            })
            // if i do not set the objectId, it will make a new object
            // if I do set the objectId to an existing object Id from data table
            // on backendless, it will update the object.

            // include the async callback to save the object here
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.new_log){
            val sleepDetailIntent = Intent(this,
                SleepDetailActivity::class.java)
            startActivity(sleepDetailIntent)
        }
        return true
    }
}
