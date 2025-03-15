package com.group147.appartmentblog.repositories

import com.google.firebase.firestore.QuerySnapshot
import com.group147.appartmentblog.database.IDao

abstract class AbsAppartmentBlogRepository<T>(private val dao: IDao<T>) {
    protected fun insert(entity: T) {
        dao.insert(entity)
    }

    protected fun update(entity: T) {
        dao.update(entity)
    }

    protected fun delete(entity: T) {
        dao.delete(entity)
    }

    abstract fun streamAllExistingEntities()

    abstract fun handleDocumentChanges(snapshot: QuerySnapshot)
}