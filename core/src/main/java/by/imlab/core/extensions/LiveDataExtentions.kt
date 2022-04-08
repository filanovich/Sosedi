package by.imlab.core.extensions

import androidx.lifecycle.MutableLiveData

// Set default value for any type of MutableLiveData
fun <T : Any> MutableLiveData<T>.default(initialValue: T) = apply { postValue(initialValue) }

// Set new value for any type of MutableLiveData
fun <T : Any> MutableLiveData<T>.set(newValue: T) = apply { postValue(newValue) }