package com.example.myuiroom.tabs.move

import android.util.Log
import android.view.DragEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.myuiroom.R
import com.example.myuiroom.adapters.TaskAdapter
import com.example.myuiroom.viewModels.TaskViewModel

class DragListener(private val listener: Listener) : View.OnDragListener  {

    private var isDropped = false

    override fun onDrag(v: View, event: DragEvent): Boolean {
        when (event.action) {
            DragEvent.ACTION_DROP -> {
                Log.d("MyLog", "ACTION_DROP")
                isDropped = true
                var positionTarget: Int
                val viewSource = event.localState as? View
                val viewId = v.id
                val taskItem = R.id.cardTask // val flItem
                val tvImportant = R.id.tv_important //val tvEmptyListTop
                val tvImportantUrgent = R.id.tv_important_and_urgent // tvEmptyListBottom
                val tvNotImportant = R.id.tv_not_important_and_urgent
                val tvUrgent = R.id.tv_urgent
                val rvImportant = R.id.important  // val rvTop
                val rvImportantUrgent = R.id.important_and_urgent //val rvBottom
                val rvNotImportant = R.id.not_important_and_urgent
                val rvUrgent = R.id.urgent
                when (viewId) {
                    taskItem, rvImportant, rvImportantUrgent, rvNotImportant, rvUrgent ->{
                        val target: RecyclerView = when (viewId) {
                            tvImportant, rvImportant -> v.rootView.findViewById(rvImportant)
                            tvImportantUrgent, rvImportantUrgent -> v.rootView.findViewById(rvImportantUrgent)
                            tvNotImportant, rvNotImportant -> v.rootView.findViewById(rvNotImportant)
                            tvUrgent, rvUrgent -> v.rootView.findViewById(rvUrgent)
                            else -> v.parent as RecyclerView
                        }
                        val type: String = when (viewId) {
                            tvImportant, rvImportant -> "important"
                            tvImportantUrgent, rvImportantUrgent -> "importantAndUrgent"
                            tvNotImportant, rvNotImportant -> "notimportantAndUrgent"
                            tvUrgent, rvUrgent -> "urgent"
                            else -> "notimportantAndUrgent"
                        }
                        var positionTarget: Int = -1 // Default value if cast fails

                        try {
                            positionTarget = v.tag as Int
                        } catch (e: Exception) {

                        }
                        if (viewSource != null) {
                            val source = viewSource.parent as RecyclerView
                            val adapterSource = source.adapter as TaskAdapter
        
                            val positionSource = viewSource.tag as Int

                            val list = adapterSource.getList()[positionSource]
                            val listSource = adapterSource.getList()

                            listSource.removeAt(positionSource)
                            adapterSource.updateList(listSource)
                            adapterSource.notifyDataSetChanged()

                            val adapterTarget = target.adapter as TaskAdapter
                            Log.d("MyLog", "target $target")
                            val customListTarget = adapterTarget.getList()

                            if (positionTarget < 0) {
                                customListTarget.add(list)
                                //  customListTarget.add(positionTarget, list)
                            } else {
                                customListTarget.add(positionTarget, list)
                            }
                            adapterTarget?.updateList(customListTarget)
                            adapterTarget?.notifyDataSetChanged()

                            if(target != source){
                                Log.d("MyLog", "target != source")
                                listener.editTaskType(list, type)
                            }
                        }
                    }
                }
            }
        }
        if (!isDropped && event.localState != null) {
            (event.localState as? View)?.visibility = View.VISIBLE
        }
        return true
    }
}