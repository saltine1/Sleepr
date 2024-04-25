package com.mistershorr.loginandregistration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.mistershorr.loginandregistration.databinding.ActivitySleepDetailBinding
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter


class SleepDetailActivity : AppCompatActivity() {

    companion object {
        val TAG = "SleepDetailActivity"
        val EXTRA_SLEEP = "sleepytime"
    }

    private lateinit var binding: ActivitySleepDetailBinding
    lateinit var bedTime: LocalDateTime
    lateinit var wakeTime: LocalDateTime

    fun setTime(time: LocalDateTime, timeFormatter: DateTimeFormatter, button: Button) {
        val timePickerDialog = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(time.hour)
            .setMinute(time.minute)
            .build()

        timePickerDialog.show(supportFragmentManager, "bedtime")
        timePickerDialog.addOnPositiveButtonClickListener {
            var selectedTime = LocalDateTime.of(
                time.year,
                time.month,
                time.dayOfMonth,
                timePickerDialog.hour,
                timePickerDialog.minute
            )
            button.text = timeFormatter.format(selectedTime)
            when (button.id) {
                binding.buttonSleepDetailBedTime.id -> {
                    bedTime = selectedTime
                    if (wakeTime.toEpochSecond(UTC) < selectedTime.toEpochSecond(UTC)) {
                        wakeTime = wakeTime.plusDays(1)
                    }
                }

                binding.buttonSleepDetailWakeTime.id -> {
                    if (selectedTime.toEpochSecond(UTC) < bedTime.toEpochSecond(UTC)) {
                        selectedTime = selectedTime.plusDays(1)
                    }
                    wakeTime = selectedTime
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySleepDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // these are default values that should be set when creating a new entry
        // however, if editing an existing entry, those values should be used instead

        bedTime = LocalDateTime.now()
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
        binding.buttonSleepDetailBedTime.text = timeFormatter.format(bedTime)
        wakeTime = bedTime.plusHours(8)
        binding.buttonSleepDetailWakeTime.text = timeFormatter.format(wakeTime)
        val dateFormatter = DateTimeFormatter.ofPattern("EEEE MMM dd, yyyy")
        binding.buttonSleepDetailDate.text = dateFormatter.format(bedTime)

        binding.buttonSleepDetailBedTime.setOnClickListener {
            setTime(bedTime, timeFormatter, binding.buttonSleepDetailBedTime)
        }

        binding.buttonSleepDetailWakeTime.setOnClickListener {
            setTime(wakeTime, timeFormatter, binding.buttonSleepDetailWakeTime)
        }

        binding.buttonSleepDetailDate.setOnClickListener {
            val selection = bedTime.toEpochSecond(ZoneOffset.UTC)
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setSelection(selection * 1000) // requires milliseconds
                .setTitleText("Select a Date")
                .build()

            Log.d(
                TAG,
                "onCreate: after build: ${
                    LocalDateTime.ofEpochSecond(
                        datePicker.selection ?: 0L,
                        0,
                        ZoneOffset.UTC
                    )
                }"
            )
            datePicker.addOnPositiveButtonClickListener { millis ->
                val selectedLocalDate =
                    Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDateTime()
                Toast.makeText(
                    this,
                    "Date is: ${dateFormatter.format(selectedLocalDate)}",
                    Toast.LENGTH_SHORT
                ).show()

                // make sure that waking up the next day if waketime < bedtime is preserved
                var wakeDate = selectedLocalDate

                if (wakeTime.dayOfMonth != bedTime.dayOfMonth) {
                    wakeDate = wakeDate.plusDays(1)
                }

                bedTime = LocalDateTime.of(
                    selectedLocalDate.year,
                    selectedLocalDate.month,
                    selectedLocalDate.dayOfMonth,
                    bedTime.hour,
                    bedTime.minute
                )

                wakeTime = LocalDateTime.of(
                    wakeDate.year,
                    wakeDate.month,
                    wakeDate.dayOfMonth,
                    wakeTime.hour,
                    wakeTime.minute
                )
                binding.buttonSleepDetailDate.text = dateFormatter.format(bedTime)
            }
            datePicker.show(supportFragmentManager, "datepicker")
        }


        val context = binding.buttonSleepDetailCancel.context
        binding.buttonSleepDetailCancel.setOnClickListener {
            val listIntent = Intent(context, SleepListActivity::class.java)
            context.startActivity(listIntent)
        }

        Backendless.UserService.CurrentUser(true, object : AsyncCallback<BackendlessUser?> {
            override fun handleResponse(response: BackendlessUser?) {
                // some additional logic for reloaded user
            }

            override fun handleFault(fault: BackendlessFault) {
                // error handling logic
            }
        })




        binding.buttonSleepDetailSave.setOnClickListener {
            var user = Backendless.UserService.CurrentUser()

            val wakeMillis = wakeTime.toEpochSecond(ZoneId.systemDefault().rules.getOffset(Instant.now()))*1000
            val notes = binding.editTextTextMultiLineSleepDetailNotes.text
            val sleepDataMillis = bedTime.toEpochSecond(ZoneId.systemDefault().rules.getOffset(Instant.now()))*1000
            val sleepRating = binding.ratingBarSleepDetailQuality.rating
            val varBedMillis = wakeTime.toEpochSecond(ZoneId.systemDefault().rules.getOffset(Instant.now()))*1000
            val ownerId = user.getProperty("ownerId")


            val sleep = Sleep(wakeMillis, varBedMillis, sleepDataMillis, sleepRating.toInt(), notes.toString(), ownerId.toString())
            val listIntent = Intent(context, SleepListActivity::class.java)
            listIntent.putExtra("EXTRA_SLEEP", sleep)
            context.startActivity(listIntent)
        }


    }}