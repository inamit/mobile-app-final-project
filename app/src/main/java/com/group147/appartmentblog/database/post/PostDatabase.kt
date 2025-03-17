import androidx.room.Database
import androidx.room.RoomDatabase
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.database.post.PostDao

@Database(entities = [Post::class], version = 2) // Increment the version number
abstract class PostDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao

    companion object {
        // Singleton instance of the database
        @Volatile
        private var INSTANCE: PostDatabase? = null

        fun getDatabase(context: Context): PostDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PostDatabase::class.java,
                    "post_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}