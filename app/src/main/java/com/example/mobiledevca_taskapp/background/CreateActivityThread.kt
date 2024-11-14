//
package com.example.mobiledevca_taskapp.background

import android.os.Message

class CreateActivityThread(handler: ThreadHandler) : Thread() {

    private var _handler: ThreadHandler = handler

    override fun run() {
        super.run()

        //val m: Message = _handler.obtainMessage()
        //_handler.sendMessage(m)
    }
}