package com.thbf.android.tictaetoeonline

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*

class Login : AppCompatActivity() {

    private var mAuth: FirebaseAuth?=null
    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()
    }

    fun buLoginEvent(view: View){
        LoginToFirebase(etLogin.text.toString(),etPassword.text.toString())
    }

    private fun LoginToFirebase(login: String, password: String) {
        mAuth!!.createUserWithEmailAndPassword(login,password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(applicationContext,"Successful login",Toast.LENGTH_LONG).show()
                    var currentUser = mAuth!!.currentUser
                    if(currentUser!=null) {
                        myRef.child("Users").child(SplitString(currentUser.email.toString())).setValue(currentUser.uid)
                    }
                    LoadMain()
                }else{
                    Toast.makeText(applicationContext,"Fail login",Toast.LENGTH_LONG).show()
                }
            }
    }

    fun LoadMain(){
        var currentUser = mAuth!!.currentUser

        if(currentUser!=null){
            var intent = Intent(this,MainActivity::class.java)
            intent.putExtra("email",currentUser!!.email)
            intent.putExtra("id",currentUser.uid)
            startActivity(intent)
        }

    }

    override fun onStart() {
        super.onStart()
        LoadMain()
    }

    fun SplitString(str:String):String{
        var split = str.split("@")
        return split[0]
    }
}