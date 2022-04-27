package jp.techacademy.yutaro.taskapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.Sort
import android.content.Intent
import kotlinx.android.synthetic.main.activity_main.*
import androidx.appcompat.app.AlertDialog
import android.app.AlarmManager
import android.app.PendingIntent
import android.widget.SearchView
import io.realm.RealmConfiguration

const val EXTRA_TASK = "jp.techacademy.yutaro.taskapp.TASK"

class MainActivity : AppCompatActivity() {
    private lateinit var mRealm: Realm
    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(element: Realm) {
            reloadListView()
        }
    }

    private lateinit var mTaskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener { view ->
            val intent = Intent(this,InputActivity::class.java)
            startActivity(intent)
        }

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                searchAct(newText)
                return false
            }
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }
        })

        mRealm = Realm.getDefaultInstance()


        mRealm.addChangeListener(mRealmListener)

        mTaskAdapter = TaskAdapter(this)

        listView1.setOnItemClickListener { parent, view, position, id ->
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this,InputActivity::class.java)
            intent.putExtra(EXTRA_TASK,task.id)
            startActivity(intent)
        }

        listView1.setOnItemLongClickListener { parent, view, position, id ->
            val task = parent.adapter.getItem(position) as Task
            val builder = AlertDialog.Builder(this)
            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")

            builder.setPositiveButton("OK"){_,_->
                val results = mRealm.where(Task::class.java).equalTo("id",task.id).findAll()

                    mRealm.beginTransaction()
                    results.deleteAllFromRealm()
                    mRealm.commitTransaction()

                val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)

                reloadListView()
            }
            builder.setNegativeButton("CANCEL", null)
            val dialog = builder.create()
            dialog.show()

            true
        }

        reloadListView()
    }

    private fun reloadListView() {
        val taskRealmResults =
            mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)

        mTaskAdapter.mTaskList = mRealm.copyFromRealm(taskRealmResults)
        listView1.adapter = mTaskAdapter
        mTaskAdapter.notifyDataSetChanged()
    }

    private fun searchAct(newText:String){

        if (newText.length !=0) {
            val taskRealmResults =
                mRealm.where(Task::class.java).contains("category", newText)
                    .findAll().sort("date", Sort.DESCENDING)

            mTaskAdapter.mTaskList = mRealm.copyFromRealm(taskRealmResults)
            listView1.adapter = mTaskAdapter
            mTaskAdapter.notifyDataSetChanged()
        }else{
            reloadListView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()
    }
}