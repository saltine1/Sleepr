package com.mistershorr.loginandregistration

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.mistershorr.loginandregistration.databinding.ActivityRegistrationBinding


class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // retrieve any information from the intent using the extras keys
        val username = intent.getStringExtra(LoginActivity.EXTRA_USERNAME) ?: ""
        val password = intent.getStringExtra(LoginActivity.EXTRA_PASSWORD) ?: ""

        // prefill the username & password fields
        // for EditTexts, you actually have to use the setText functions
        binding.editTextRegistrationUsername.setText(username)
        binding.editTextTextPassword.setText(password)

        // register an account and send back the username & password
        // to the login activity to prefill those fields
        binding.buttonRegistrationRegister.setOnClickListener {
            val password = binding.editTextTextPassword.text.toString()
            val confirm = binding.editTextRegistrationConfirmPassword.text.toString()
            val username = binding.editTextRegistrationUsername.text.toString()
            val email = binding.editTextRegistrationEmail.text.toString()
            val name = binding.editTextRegistrationName.text.toString()
            if(RegistrationUtil.validatePassword(password, confirm) &&
                RegistrationUtil.validateUsername(username) && RegistrationUtil.validateEmail(email)
                && RegistrationUtil.validateName(name))  {  // && do the rest of the validations
                // apply lambda will call the functions inside it on the object
                // that apply is called on

                //register user on backendless
                // make resultIntent in handleResponse
                // add toast

                var user = BackendlessUser()
                user.setProperty("name", name)
                user.setProperty("username", username)
                user.setProperty("email", email)
                user.password = password

                Backendless.UserService.register(user, object : AsyncCallback<BackendlessUser?> {
                    override fun handleResponse(registeredUser: BackendlessUser?) {
                        // user has been registered and now can login
                        Toast.makeText(this@RegistrationActivity,"Registration Success", Toast.LENGTH_SHORT).show()
                        val resultIntent = Intent().apply {
                            // apply { putExtra() } is doing the same thing as resultIntent.putExtra()
                            putExtra(
                                LoginActivity.EXTRA_USERNAME,
                                binding.editTextRegistrationUsername.text.toString()
                            )
                            putExtra(LoginActivity.EXTRA_PASSWORD, password)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }

                    override fun handleFault(fault: BackendlessFault) {
                        Log.d("RegistrationActivity", "handleFault: ${fault.message}")
                        Toast.makeText(this@RegistrationActivity,"Registration Failed", Toast.LENGTH_SHORT).show()
                        // an error has occurred, the error code can be retrieved with fault.getCode()
                    }
                })
            }
            else{
                Toast.makeText(this@RegistrationActivity,"Registration Failed", Toast.LENGTH_SHORT).show()
            }
        }

    }
}