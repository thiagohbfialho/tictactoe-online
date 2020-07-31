package com.thbf.android.tictaetoeonline

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {

    //Database
    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference

    //Analytics
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    //Player
    var myEmail:String?=null
    var activePlayer:Int?=null
    var playerRequest:Boolean?=false
    var guestEmail:String?=null

    //Game
    var jogoAtivo:Boolean = false
    var player1 = ArrayList<Int>()
    var player2 = ArrayList<Int>()
    var player1WinsCount = 0
    var player2WinsCount = 0
    var sessionID:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        var b: Bundle? = intent.extras
        myEmail = b!!.getString("email")

        incomingCalls()
    }

    fun startGame(){
        activePlayer = 1

        player1.clear()
        player2.clear()

        for(index in 1..9){
            var buSelected:Button = when(index){
                1 -> button1
                2 -> button2
                3 -> button3
                4 -> button4
                5 -> button5
                6 -> button6
                7 -> button7
                8 -> button8
                9 -> button9
                else -> {button1}
            }
            buSelected.text = ""
            buSelected.setBackgroundResource(R.color.white)
            buSelected.isEnabled = true
        }
        updateScore()
        jogoAtivo = true
    }

    fun cleanGame(){
        myRef.child("PlayerOnline").removeValue()
    }

    fun buClick(view:View){
        val buSelect = view as Button

        var cellId = 0
        when(buSelect.id){
            R.id.button1 -> cellId = 1
            R.id.button2 -> cellId = 2
            R.id.button3 -> cellId = 3
            R.id.button4 -> cellId = 4
            R.id.button5 -> cellId = 5
            R.id.button6 -> cellId = 6
            R.id.button7 -> cellId = 7
            R.id.button8 -> cellId = 8
            R.id.button9 -> cellId = 9
        }

        //playGame(cellId,buSelect)
        if((playerRequest!! && activePlayer==1) || (!playerRequest!! && activePlayer==2)){
            myRef.child("PlayerOnline").child(sessionID!!).child(cellId.toString()).setValue(myEmail)
        }

    }

    fun playGame(cellId: Int, buSelected:Button){

        if(activePlayer == 1){
            buSelected.text = "X"
            buSelected.setBackgroundResource(R.color.blue)
            player1.add(cellId)
            activePlayer = 2
        }else{
            buSelected.text = "O"
            buSelected.setBackgroundResource(R.color.darkGreen)
            player2.add(cellId)
            activePlayer = 1
        }

        buSelected.isEnabled = false
        //if(!checkWinner()) {
            //if(activePlayer==2) autoPlay()
        //}
        checkWinner()

    }

    fun checkWinner():Boolean{

        var winner = -1

        //row 1
        if(player1.contains(1) && player1.contains(2) && player1.contains(3)){
            winner = 1
        }
        if(player2.contains(1) && player2.contains(2) && player2.contains(3)){
            winner = 2
        }

        //row 2
        if(player1.contains(4) && player1.contains(5) && player1.contains(6)){
            winner = 1
        }
        if(player2.contains(4) && player2.contains(5) && player2.contains(6)){
            winner = 2
        }

        //row 3
        if(player1.contains(7) && player1.contains(8) && player1.contains(9)){
            winner = 1
        }
        if(player2.contains(7) && player2.contains(8) && player2.contains(9)){
            winner = 2
        }

        //col 1
        if(player1.contains(1) && player1.contains(4) && player1.contains(7)){
            winner = 1
        }
        if(player2.contains(1) && player2.contains(4) && player2.contains(7)){
            winner = 2
        }

        //col 2
        if(player1.contains(2) && player1.contains(5) && player1.contains(8)){
            winner = 1
        }
        if(player2.contains(2) && player2.contains(5) && player2.contains(8)){
            winner = 2
        }

        //col 3
        if(player1.contains(3) && player1.contains(6) && player1.contains(9)){
            winner = 1
        }
        if(player2.contains(3) && player2.contains(6) && player2.contains(9)){
            winner = 2
        }

        if(winner ==1 || winner == 2){
            if(winner==1){
                player1WinsCount++
            }else{
                player2WinsCount++
            }
            jogoAtivo = false
            cleanGame()
            Toast.makeText(this, "Player $winner win the game! ",Toast.LENGTH_SHORT).show()
            startGame()
            return true
        }
        return false
    }

    fun autoPlay(cellId: Int){

        var buSelected:Button
        buSelected = when(cellId){
            1 -> button1
            2 -> button2
            3 -> button3
            4 -> button4
            5 -> button5
            6 -> button6
            7 -> button7
            8 -> button8
            9 -> button9
            else -> {button1}
        }

        playGame(cellId,buSelected)
    }



    fun updateScore(){
        scoreP1.text = "${player1WinsCount.toString()}"
        scoreP2.text = "${player2WinsCount.toString()}"
    }

    fun buRequestEvent(view: View){

        cleanGame()

        playerRequest = true
        tvNameP1.text = SplitString(myEmail!!)
        guestEmail = SplitString(etEmail.text.toString())
        myRef.child("Users").child(guestEmail!!).child("Request").push().setValue(myEmail)

        playerOnline(SplitString(myEmail!!)+guestEmail)
        etEmail.setText("")
    }

    fun buAcceptEvent(view: View){

        guestEmail = SplitString(etEmail.text.toString())
        myRef.child("Users").child(guestEmail!!).child("Request").push().setValue(myEmail)

        playerOnline(guestEmail!!+SplitString(myEmail!!))

        tvNameP1.text = guestEmail
        tvNameP2.text = SplitString(myEmail!!)
        etEmail.setText("")
        startGame()

    }

    fun playerOnline(sessionID:String){
        this.sessionID = sessionID
        myRef.child("PlayerOnline").child(sessionID)
            .addValueEventListener(object:ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try {
                        if(!dataSnapshot.exists()){
                            startGame()
                        }

                        for (postSnapshot in dataSnapshot.children) {
                            val keySnap = postSnapshot.getKey()
                            val valueSnap = postSnapshot.getValue()

                            if (valueSnap!=myEmail){
                                activePlayer = if(playerRequest!!) 2 else 1
                            }else{
                                activePlayer = if(playerRequest!!) 1 else 2
                            }

                            if(jogoAtivo) autoPlay(keySnap!!.toInt())

                        }

                    }catch (ex:Exception){}
                }

                override fun onCancelled(p0: DatabaseError) {
                }

            })
    }

    fun incomingCalls(){
        myRef.child("Users").child(SplitString(myEmail!!)).child("Request")
            .addValueEventListener(object:ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try {
                        val td = dataSnapshot!!.value as HashMap<String,Any>
                        if (td!=null){
                            var value:String
                            for (Key in td.keys){
                                value = td[Key] as String

                                myRef.child("Users").child(SplitString(myEmail!!)).child("Request").setValue(true)

                                if(SplitString(value).equals(guestEmail)){
                                    tvNameP2.text = guestEmail
                                    startGame()
                                }else{
                                    etEmail.setText(value)
                                }

                                break
                            }
                        }
                    }catch (ex:Exception){

                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                }

            })
    }

    fun SplitString(str:String):String{
        var split = str.split("@")
        return split[0]
    }
}