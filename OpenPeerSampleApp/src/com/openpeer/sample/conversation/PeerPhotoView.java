/*******************************************************************************
 *
 *  Copyright (c) 2014 , Hookflash Inc.
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those
 *  of the authors and should not be interpreted as representing official policies,
 *  either expressed or implied, of the FreeBSD Project.
 *******************************************************************************/
package com.openpeer.sample.conversation;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.openpeer.javaapi.OPMessage;
import com.openpeer.sample.PhotoActivity;
import com.openpeer.sample.R;
import com.openpeer.sample.util.DateFormatUtils;
import com.openpeer.sample.view.IViewBinder;
import com.openpeer.sdk.model.HOPConversation;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PeerPhotoView extends RelativeLayout implements IViewBinder<OPMessage> {
    private static final long FREEZING_PERIOD = 60 * 60 * 1000l;
    OPMessage mMessage;
    HOPConversation mSession;
    @InjectView(R.id.user)
    TextView title;
    @InjectView(R.id.time)
    TextView time;
    @InjectView(R.id.imageView)
    ImageView imageView;

    String imageUri;
    String thumbNailUri;

    public PeerPhotoView(Context context) {
        this(context, null, 0);
    }

    public PeerPhotoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.layout_photo_peer, this);
        ButterKnife.inject(this);
    }

    public PeerPhotoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void update(OPMessage data) {
        mMessage = data;
        try {
            JSONObject object = new JSONObject(data.getMessage());
            imageUri = object.getString("uri");
            thumbNailUri = object.getString("thumbNail");

            String status = object.getString("status");
            Picasso.with(getContext()).load(Uri.parse(thumbNailUri)).into(imageView);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        title.setText(mMessage.getFromUser().getName());
        time.setText(DateFormatUtils.getSameDayTime(data.getTime()
                .toMillis(false)));


    }

    /**
     * @param session
     */
    public void setSession(HOPConversation session) {
        mSession = session;
    }

    public OPMessage getMessage() {
        return mMessage;
    }

    @OnClick(R.id.imageView)
    public void onImageClick(View view) {
        PhotoActivity.start(getContext(), imageUri);
    }
}