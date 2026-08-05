package com.remmi.app.core.repository

import com.remmi.app.core.model.models.RemmiModel

interface Repository<T : RemmiModel> {

    fun add(item: T)

    fun remove(id: String)

    fun update(item: T)

    fun get(id: String): T?

    fun getAll(): List<T>

    fun clear()
}