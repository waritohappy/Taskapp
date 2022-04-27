package jp.techacademy.yutaro.taskapp
import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class TaskApp :Application(){
        override fun onCreate(){
            super.onCreate()
            Realm.init(this)
            var realmConfig =
                RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build()
            Realm.setDefaultConfiguration(realmConfig)
        }
}