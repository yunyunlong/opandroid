/*
 * ******************************************************************************
 *  *
 *  *  Copyright (c) 2014 , Hookflash Inc.
 *  *  All rights reserved.
 *  *
 *  *  Redistribution and use in source and binary forms, with or without
 *  *  modification, are permitted provided that the following conditions are met:
 *  *
 *  *  1. Redistributions of source code must retain the above copyright notice, this
 *  *  list of conditions and the following disclaimer.
 *  *  2. Redistributions in binary form must reproduce the above copyright notice,
 *  *  this list of conditions and the following disclaimer in the documentation
 *  *  and/or other materials provided with the distribution.
 *  *
 *  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  *
 *  *  The views and conclusions contained in the software and documentation are those
 *  *  of the authors and should not be interpreted as representing official policies,
 *  *  either expressed or implied, of the FreeBSD Project.
 *  ******************************************************************************
 */

package com.openpeer.sdk.model;

import android.content.Intent;

import com.openpeer.javaapi.CallClosedReasons;
import com.openpeer.javaapi.CallStates;
import com.openpeer.javaapi.OPCall;
import com.openpeer.javaapi.OPCallDelegate;
import com.openpeer.javaapi.OPConversationThread;
import com.openpeer.sdk.app.IntentData;
import com.openpeer.sdk.app.OPDataManager;
import com.openpeer.sdk.app.OPHelper;
import com.openpeer.sdk.utils.OPModelUtils;

import java.util.Hashtable;

public class CallManager extends OPCallDelegate {

    Hashtable<String, OPCall> mIdToCalls;//peerId to call map
    Hashtable<Long, OPCall> mUserIdToCalls;//peerId to call map

    private Hashtable<Long, CallStatus> mCallStates;

    private static CallManager instance;

    public static CallManager getInstance() {
        if (instance == null) {
            instance = new CallManager();
        }
        return instance;
    }

    private CallManager() {
    }

    @Override
    public void onCallStateChanged(OPCall call, CallStates state) {
        if (state == CallStates.CallState_Preparing) {
            //Handle racing condition. SImply hangup the existing call for now.
            OPCall oldCall = findCallForPeer(call.getPeerUser().getUserId());
            if (oldCall != null) {
                call.hangup(CallClosedReasons.CallClosedReason_NotAcceptableHere);
            }
            OPConversationThread thread = call.getConversationThread();
            call.setCbcId(OPModelUtils.getWindowIdForThread(thread));
            OPDataManager.getDatastoreDelegate().saveCall(
                call,
                ConversationManager.getInstance().getConversation(thread, true));
            cacheCall(call);
        }

        CallEvent event = new CallEvent(call.getCallID(),
                                        state,
                                        System.currentTimeMillis());

        OPDataManager.getDatastoreDelegate().saveCallEvent(call.getCallID(), event);
        Intent intent = new Intent();

        intent.setAction(IntentData.ACTION_CALL_STATE_CHANGE);
        intent.putExtra(IntentData.ARG_CALL_STATE, state.name());
        intent.putExtra(IntentData.ARG_CALL_ID, call.getCallID());
        intent.putExtra(IntentData.ARG_PEER_USER_ID, call.getPeerUser().getUserId());

        OPHelper.getInstance().sendBroadcast(intent);
        switch (state){
        case CallState_Closed:
            removeCallCache(call);
            break;

        }
    }

    private void cacheCall(OPCall call) {
        if (mIdToCalls == null) {
            mIdToCalls = new Hashtable<>();
        }
        mIdToCalls.put(call.getCallID(), call);
        if (mUserIdToCalls == null) {
            mUserIdToCalls = new Hashtable<>();

        }
        mUserIdToCalls.put(call.getPeerUser().getUserId(), call);
    }

    public CallStatus getMediaStateForCall(long userId) {
        CallStatus state = null;
        if (mCallStates == null) {
            mCallStates = new Hashtable<>();

        } else {
            state = mCallStates.get(userId);
        }
        if (state == null) {
            state = new CallStatus();
            mCallStates.put(userId, state);
        }
        return state;

    }

    public boolean hasCalls() {
        return mIdToCalls != null && mIdToCalls.size() > 0;
    }

    private void removeCallCache(OPCall call) {
        long userId = call.getPeerUser().getUserId();
        if (mIdToCalls != null) {
            mIdToCalls.remove(call.getCallID());
            mUserIdToCalls.remove(call.getPeerUser().getUserId());
            if (mCallStates != null) {
                mCallStates.remove(userId);
            }
            if (mIdToCalls.isEmpty()) {
                mIdToCalls = null;
                mUserIdToCalls = null;
                mCallStates = null;
            }
        }
    }

    public OPCall findCallById(String callId) {
        if (mIdToCalls != null) {
            return mIdToCalls.get(callId);
        }
        return null;
    }

    public OPCall findCallForPeer(long userId) {
        if (mIdToCalls == null) {
            return null;
        }

        return mUserIdToCalls.get(userId);
    }

    public OPCall findCallByCbcId(long cbcId) {
        if (mIdToCalls != null) {
            for (OPCall call : mIdToCalls.values()) {
                if (call.getCbcId() == cbcId) {
                    return call;
                }
            }
        }
        return null;
    }

    public static void clearOnSignout() {
        if (instance != null) {
            instance.mIdToCalls = null;
            instance.mUserIdToCalls = null;
            instance.mCallStates = null;
        }
    }
}