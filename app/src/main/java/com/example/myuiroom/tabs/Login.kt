package com.example.myuiroom.tabs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.myuiroom.R
import com.example.myuiroom.databinding.LoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException


class Login : Fragment() {

    private var _binding: LoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var listener: OnLoginFragmentInteractionListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LoginBinding.inflate(inflater, container, false)

        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val savedValue = sharedPreferences.getString("email", "default value")
        if(savedValue != null) {

            setGooglePlusButtonText(binding.signInButton, savedValue.toString())
        }
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        binding.signInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
        binding.signInButton.setSize(SignInButton.SIZE_WIDE)

        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val savedValue = sharedPreferences.getString("email", "default value")
        if(savedValue != "default value") {
            setGooglePlusButtonText(binding.signInButton, savedValue.toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                handleSignInResult(account)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun handleSignInResult(account: GoogleSignInAccount?) {
        if (account != null) {
            val email = account.email

            binding.signInButton.apply {
                setStyle(SignInButton.SIZE_WIDE, SignInButton.COLOR_DARK)
            }
            setGooglePlusButtonText(binding.signInButton, email)
            val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("email", email.toString())
            editor.apply()
        }
    }
    public fun setGooglePlusButtonText(signInButton: SignInButton, buttonText: String ) {
// Найти TextView внутри SignInButton и установите текст
        val view: View = signInButton.getChildAt(0)
        if (view is TextView) {
            view.setText(buttonText)
        }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnLoginFragmentInteractionListener) {
            listener = context
        } else {
            //  throw RuntimeException("$context must implement OnLoginFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        _binding = null
    }

    interface OnLoginFragmentInteractionListener {
        fun onLoginSuccess(name: String?, email: String?, photoUrl: String)
    }

    companion object {
        private const val TAG = "LoginFragment"
        private const val RC_SIGN_IN = 123
    }

}