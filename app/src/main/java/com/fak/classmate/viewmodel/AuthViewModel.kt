package com.fak.classmate.viewmodel

import androidx.lifecycle.ViewModel
import com.fak.classmate.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class AuthViewModel: ViewModel(){
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    fun login(email: String, password: String, onResult: (Boolean,String?)-> Unit){
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    onResult(true,null)
                }else{
                    onResult(false,it.exception?.localizedMessage)
                }
            }
    }

    //firstname | lastname | email | password
    fun signup(firstname: String,lastname: String, email: String, password: String, onResult: (Boolean,String?)-> Unit){
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val userId = it.result?.user?.uid
                    val userModel = UserModel(firstname,lastname, email,userId!!)
                    firestore.collection("users").document(userId)
                        .set(userModel)
                        .addOnCompleteListener { dbTask->
                            if(dbTask.isSuccessful){
                                onResult(true,null)
                            }else{
                                onResult(false,"Something went wrong")
                            }
                        }
                }else{
                    onResult(false,it.exception?.localizedMessage)
                }
            }
    }
}