package com.fak.classmate.viewmodel

import androidx.lifecycle.ViewModel
import com.fak.classmate.model.UserModel
import com.fak.classmate.model.ValidationState
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

    // Validation Functions
    fun validateFirstname(name: String): ValidationState {
        return when {
            name.isBlank() -> ValidationState(false, "First name is required")
            name.length < 2 -> ValidationState(false, "First name must be at least 2 characters")
            !name.matches(Regex("^[a-zA-Z\\s]+$")) -> ValidationState(false, "First name can only contain letters")
            else -> ValidationState(true, "")
        }
    }

    fun validateLastname(name: String): ValidationState {
        return when {
            name.isBlank() -> ValidationState(false, "Last name is required")
            name.length < 2 -> ValidationState(false, "Last name must be at least 2 characters")
            !name.matches(Regex("^[a-zA-Z\\s]+$")) -> ValidationState(false, "Last name can only contain letters")
            else -> ValidationState(true, "")
        }
    }

    fun validateEmail(email: String): ValidationState {
        return when {
            email.isBlank() -> ValidationState(false, "Email is required")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                ValidationState(false, "Please enter a valid email address")
            else -> ValidationState(true, "")
        }
    }

    fun validatePassword(password: String): ValidationState {
        return when {
            password.isBlank() -> ValidationState(false, "Password is required")
            password.length < 6 -> ValidationState(false, "Password must be at least 6 characters")
            !password.matches(Regex(".*[A-Z].*")) -> ValidationState(false, "Password must contain at least one uppercase letter")
            !password.matches(Regex(".*[a-z].*")) -> ValidationState(false, "Password must contain at least one lowercase letter")
            !password.matches(Regex(".*\\d.*")) -> ValidationState(false, "Password must contain at least one number")
            else -> ValidationState(true, "")
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): ValidationState {
        return when {
            confirmPassword.isBlank() -> ValidationState(false, "Please confirm your password")
            password != confirmPassword -> ValidationState(false, "Passwords do not match")
            else -> ValidationState(true, "")
        }
    }

    fun isFormValid(
        firstnameValid: Boolean,
        lastnameValid: Boolean,
        emailValid: Boolean,
        passwordValid: Boolean,
        confirmPasswordValid: Boolean
    ): Boolean {
        return firstnameValid && lastnameValid && emailValid && passwordValid && confirmPasswordValid
    }
}