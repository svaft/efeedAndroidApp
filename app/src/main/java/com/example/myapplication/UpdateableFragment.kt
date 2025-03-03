package com.example.myapplication

import android.graphics.Color

interface UpdateableFragment {

    fun update()
    fun fabText():String
    fun buildCmd():String
    fun eom(){}
    fun processEvent(e:String){}
    fun processProgram(){}
    fun programMainLP(){}
    fun programMainClick(){
//        if (ma.cmdRecorded) {
//            if (ma.cmdStarted) {
//                ma.putLog("stop current move")
//                ma.sendCommand("!S") // stop current move
//            } else {
//                ma.cmdStarted = true
//                ma.putLog("repeat or zero")
//                ma.sendCommand("!3") //repeat last command or quick move to initial position
//                ma.b.btMain.setText(R.string.stop)
//            }
//        } else { // command not recorded yet
//            if (ma.cmdStarted) { // command started but not recorded for future replay
//                ma.cmdStarted = false
//                ma.cmdRecorded = true
//                ma.b.btMain.setBackgroundColor(Color.RED)
//                ma.putLog("stop&save last move")
//                ma.sendCommand("!2")
//                ma.pauseProgramFlag = true
//                ma.b.btPause.setBackgroundColor(Color.RED)
//
////                        if (viewModel.threadMode.value == true)
////                            b.btMain.setText(R.string.thread)
////                        else
////                            b.btMain.setText(R.string.feed)
//            }
//            else { // build command with active TAB config and send it to lathe
//                ma.mfUpdIf.update()
//                ma.b.viewPager.setPagingEnabled(false)
//                ma.disableUnselectedTabs()
//
//                val cmd = buildCmd()
//                ma.sendCommand(cmd)
//                ma.cmdStarted = true
//                ma.putLog(cmd)
//                ma.b.btMain.setText(R.string.sns)
//            }
//        }

    }
}